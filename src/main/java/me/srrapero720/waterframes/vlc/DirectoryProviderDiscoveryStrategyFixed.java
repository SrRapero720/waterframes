/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2022 Caprica Software Limited.
 */

package me.srrapero720.waterframes.vlc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import me.srrapero720.vlcj.factory.discovery.NativeDiscovery;
import me.srrapero720.vlcj.factory.discovery.provider.ConfigDirConfigFileDiscoveryDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.DiscoveryProviderPriority;
import me.srrapero720.vlcj.factory.discovery.provider.JnaLibraryPathDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.LinuxWellKnownDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.MacOsWellKnownDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.SystemPathDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.UserDirConfigFileDiscoveryDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.UserDirDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.provider.WindowsInstallDirectoryProvider;
import me.srrapero720.vlcj.factory.discovery.strategy.BaseNativeDiscoveryStrategy;

/** Implementation of a native discovery strategy that searches a list of well-known directories.
 * <p>
 * The standard {@link ServiceLoader} mechanism is used to load {@link DiscoveryDirectoryProvider} instances that will
 * provide the lists of directories to search.
 * <p>
 * By using service loader, a client application can easily add their own search directories simply by adding their own
 * implementation of a discovery directory provider to the run-time classpath, and adding/registering their provider
 * class in <code>META-INF/services/me.srrapero720.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider</code> - the
 * client application need not concern itself directly with the default {@link NativeDiscovery} component.
 * <p>
 * Provider implementations have a priority. All of the standard provider implementations have a priority &lt; 0, see
 * {@link DiscoveryProviderPriority}. A client application with its own provider implementations can return a priority
 * value as appropriate to ensure their own provider is used before or after the other implementations. */
abstract public class DirectoryProviderDiscoveryStrategyFixed extends BaseNativeDiscoveryStrategy {
    
    /** Service loader for the directory provider implementations. */
    private static final List<DiscoveryDirectoryProvider> directoryProviders = Arrays
            .asList(//new UserDirConfigFileDiscoveryDirectoryProvider(),
                    //new ConfigDirConfigFileDiscoveryDirectoryProvider(),
                    //new JnaLibraryPathDirectoryProvider(),
                    new LinuxWellKnownDirectoryProvider(),
                    new MacOsWellKnownDirectoryProvider(),
                    //new SystemPathDirectoryProvider(),
                    //new UserDirDirectoryProvider(),
                    new WindowsInstallDirectoryProvider());

    
    /** Create a new native discovery strategy.
     *
     * @param filenamePatterns
     *            filename patterns to search for, as regular expressions
     * @param pluginPathFormats
     *            directory name templates used to find the VLC plugin directory, printf style. */
    public DirectoryProviderDiscoveryStrategyFixed(String[] filenamePatterns, String[] pluginPathFormats) {
        super(filenamePatterns, pluginPathFormats);
    }
    
    @Override
    public final List<String> discoveryDirectories() {
        List<String> directories = new ArrayList<String>();
        for (DiscoveryDirectoryProvider provider : getSupportedProviders()) {
            directories.addAll(Arrays.asList(provider.directories()));
        }
        return directories;
    }
    
    private List<DiscoveryDirectoryProvider> getSupportedProviders() {
        List<DiscoveryDirectoryProvider> result = new ArrayList<DiscoveryDirectoryProvider>();
        for (DiscoveryDirectoryProvider service : directoryProviders) {
            if (service.supported()) {
                result.add(service);
            }
        }
        return sort(result);
    }
    
    private List<DiscoveryDirectoryProvider> sort(List<DiscoveryDirectoryProvider> providers) {
        Collections.sort(providers, (p1, p2) -> p2.priority() - p1.priority());
        return providers;
    }
    
}

package me.srrapero720.waterframes.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String target, String mixin) {
        if (target.endsWith("vp.VideoPlayerMixin")) {
            var videoplayer = FMLLoader.getLoadingModList().getModFileById("videoplayer");

            if (videoplayer != null) {
                String[] versions = videoplayer.versionString().split("\\.");
                try {
                    if (versions.length < 2) return false;
                    int major = Integer.parseInt(versions[0]);
                    int middle = Integer.parseInt(versions[1]);
                    int minor = (versions.length >= 3) ? Integer.parseInt(versions[3]) : -1;
                    return major == 2 && middle > 3 && (middle <= 5 || middle == 6 && minor <= 0);
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}

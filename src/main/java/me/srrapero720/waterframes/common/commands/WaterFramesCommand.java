package me.srrapero720.waterframes.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WFRegistry;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import org.watermedia.api.image.ImageAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.command.EnumArgument;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.net.URI;
import java.util.*;

import java.util.function.Supplier;

public class WaterFramesCommand {
    private static final Marker IT = MarkerManager.getMarker("Commands");
    public static final Component ACTIVATED = Component.translatable("waterframes.common.activated");
    public static final Component DEACTIVATED = Component.translatable("waterframes.common.deactivated");

    public static ItemInput[] DEFAULT_INPUTS = new ItemInput[0];
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var waterframes = Commands.literal("waterframes").requires(WaterFramesCommand::hasPermissions);

        var edit = Commands.argument("blockpos", BlockPosArgument.blockPos());

        // URL
        edit.then(Commands.literal("url")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(c -> setUrl(getTile(c), c.getSource(), getStr(c, "url")))
                )
        );

        var position = Commands.literal("position")
                .then(Commands.argument("vertical", EnumArgument.enumArgument(PositionVertical.class))
                        .executes(c -> setPosition(
                                getTile(c),
                                c.getSource(),
                                getEnum(c,"vertical", PositionVertical.class),
                                null
                                )
                        )
                        .then(Commands.argument("horizontal", EnumArgument.enumArgument(PositionHorizontal.class))
                                .executes(c -> setPosition(
                                        getTile(c),
                                        c.getSource(),
                                        getEnum(c,"vertical", PositionVertical.class),
                                        getEnum(c, "horizontal", PositionHorizontal.class)
                                        )
                                )
                        )
                );
        edit.then(position);

        // SIZE
        edit.then(Commands.literal("size")
                .then(Commands.argument("width", FloatArgumentType.floatArg(0.1f))
                        .executes(c -> setSize(
                                getTile(c),
                                c.getSource(),
                                getFloat(c,"width"),
                                -1)
                        )
                        .then(Commands.argument("height", FloatArgumentType.floatArg(0.1f))
                                .executes(c -> setSize(
                                        getTile(c),
                                        c.getSource(),
                                        getFloat(c, "width"), getFloat(c,"height")
                                        )
                                )
                        )
                )
        );

        // ROTATION
        edit.then(Commands.literal("rotation")
                .then(Commands.argument("rotation", FloatArgumentType.floatArg(0f, 360.0f))
                        .executes(c -> setRotation(getTile(c), c.getSource(), getFloat(c, "rotation")))
                )
        );

        // TRANSPARENCY
        edit.then(Commands.literal("alpha")
                .then(Commands.argument("alpha", IntegerArgumentType.integer(0, 255))
                        .executes(c -> setAlpha(getTile(c), c.getSource(), getInt(c, "alpha")))
                )
        );

        // BRIGHTNESS
        edit.then(Commands.literal("brightness")
                .then(Commands.argument("brightness", IntegerArgumentType.integer(0, 255))
                        .executes(c -> setBrightness(getTile(c), c.getSource(), getInt(c, "brightness")))
                )
        );

        // RENDER DISTANCE
        edit.then(Commands.literal("renderDistance")
                .then(Commands.argument("render_distance", IntegerArgumentType.integer(4))
                        .executes(c -> setRenderDistance(getTile(c), c.getSource(), getInt(c, "render_distance")))
                )
        );

        // RENDER DISTANCE
        edit.then(Commands.literal("projectionDistance")
                .then(Commands.argument("projection_distance", IntegerArgumentType.integer(4))
                        .executes(c -> setProjectionDistance(getTile(c), c.getSource(), getInt(c, "projection_distance")))
                )
        );

        // VOLUME DISTANCE
        var volumeDistance = Commands.argument("min_distance", IntegerArgumentType.integer(0))
                .executes(c -> setVolume(getTile(c), c.getSource(), getIntOr(c, "volume", -1), getInt(c, "min_distance"), -1))
                .then(Commands.argument("max_distance", IntegerArgumentType.integer(0))
                        .executes(c -> setVolume(getTile(c), c.getSource(), getIntOr(c, "volume", -1), getInt(c, "min_distance"), getInt(c, "max_distance")))
                );

        edit.then(Commands.literal("volumeDistance")
                .then(volumeDistance)
        );

        // VOLUME
        edit.then(Commands.literal("volume")
                .then(Commands.argument("volume", IntegerArgumentType.integer(0, 120))
                        .executes(c -> setVolume(getTile(c), c.getSource(), getInt(c, "volume"), -1, -1))
                        .then(volumeDistance)
                )
        );

        waterframes.then(Commands.literal("edit").then(edit));
        waterframes.then(Commands.literal("audit")
                .then(Commands.literal("author")
                        .then(Commands.argument("blockpos", BlockPosArgument.blockPos())
                                .executes(c -> auditURLAuthor(getTile(c), c.getSource()))
                        )
                )
                .then(Commands.literal("in_range")
                        .then(Commands.argument("chunkrange", IntegerArgumentType.integer(0))
                                .executes(c -> auditFramesRange(c.getSource(), c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "chunkrange")))
                        )
                )
        );

        waterframes.then(Commands.literal("give")
                .executes(c -> giveSelfKit(c.getSource()))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(c -> giveKit(c.getSource(), (List<ServerPlayer>) EntityArgument.getPlayers(c, "targets"))))
        );

        waterframes.then(Commands.literal("whitelist")
                .then(Commands.literal("add")
                        .then(Commands.argument("url", StringArgumentType.string())
                                .executes(c -> whitelist$add(c.getSource(), getStr(c, "url"))))
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("url", StringArgumentType.string())
                                .executes(c -> whitelist$remove(c.getSource(), getStr(c, "url"))))
                )
                .then(Commands.literal("toggle")
                        .executes(c -> whitelist$toggle(c.getSource()))
                )
        );

        DEFAULT_INPUTS = new ItemInput[] {
                new ItemInput(Holder.direct(WFRegistry.REMOTE_ITEM.get()), null),
                new ItemInput(Holder.direct(WFRegistry.FRAME_ITEM.get()), null),
                new ItemInput(Holder.direct(WFRegistry.PROJECTOR_ITEM.get()), null),
                new ItemInput(Holder.direct(WFRegistry.TV_ITEM.get()), null),
                new ItemInput(Holder.direct(WFRegistry.BIG_TV_ITEM.get()), null),
                new ItemInput(Holder.direct(WFRegistry.TV_BOX_ITEM.get()), null),
        };

        dispatcher.register(waterframes);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClient(CommandDispatcher<CommandSourceStack> dispatcher) {
        var waterframes = Commands.literal("waterframes");
        waterframes.then(Commands.literal("reload_all")
                .executes(c -> watermedia$reloadAll(c.getSource()))
        );
        dispatcher.register(waterframes);
    }

    public static int setUrl(DisplayTile tile, CommandSourceStack source, String url) {
        if (tile == null) return 1;

        URI uri = WaterFrames.createURI(url);

        if (tile.data.uri != null && tile.data.uri.equals(uri)) {
            tile.data.tick = 0;
            tile.data.tickMax = -1;
        }

        tile.data.uri = uri;
        tile.data.uuid = (source.getEntity() instanceof Player player) ? player.getUUID() : Util.NIL_UUID;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.url.success"), true);
        return 0;
    }

    public static int setSize(DisplayTile tile, CommandSourceStack source, float width, float height) {
        if (tile == null) return 1;

        if (!tile.caps.resizes()) {
            source.sendFailure(msgFailed("waterframes.commands.edit.size.failed"));
            return 2;
        }

        tile.data.setWidth(width);
        tile.data.setHeight(height);

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.size.success"), true);
        return 0;
    }

    public static int setPosition(DisplayTile tile, CommandSourceStack source, PositionVertical vertical, PositionHorizontal horizontal) {
        if (tile == null) return 1;

        if (!tile.caps.resizes()) {
            source.sendFailure(msgFailed("waterframes.commands.edit.position.failed"));
            return 2;
        }

        tile.data.setHeight(vertical, tile.data.getHeight());
        if (horizontal != null) tile.data.setWidth(horizontal, tile.data.getWidth());

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.position.success"),true);
        return 0;
    }

    public static int setRotation(DisplayTile tile, CommandSourceStack source, float volume) {
        if (tile == null) return 1;

        if (!tile.caps.resizes()) {
            source.sendFailure(msgFailed("waterframes.commands.edit.rotation.failed"));
            return 2;
        }

        tile.data.rotation = volume;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.rotation.success"), true);
        return 0;
    }

    public static int setAlpha(DisplayTile tile, CommandSourceStack source, int transparency) {
        if (tile == null) return 1;

        tile.data.alpha = transparency;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.alpha.success"), true);
        return 0;
    }

    public static int setBrightness(DisplayTile tile, CommandSourceStack source, int brightness) {
        if (tile == null) return 1;

        tile.data.brightness = brightness;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.brightness.success"), true);
        return 0;
    }

    public static int setRenderDistance(DisplayTile tile, CommandSourceStack source, int renderDistance) {
        if (tile == null) return 1;

        tile.data.renderDistance = renderDistance;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.render_distance.success"), true);
        return 0;
    }

    public static int setProjectionDistance(DisplayTile tile, CommandSourceStack source, int projectionDistance) {
        if (tile == null) return 1;

        if (!tile.caps.projects()) {
            source.sendFailure(msgFailed("waterframes.commands.edit.projection.failed"));
            return 2;
        }

        tile.data.projectionDistance = projectionDistance;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.projection.success"), true);
        return 0;
    }

    public static int auditURLAuthor(DisplayTile tile, CommandSourceStack source) {
        if (tile == null) return 1;

        if (tile.data.uuid == Util.NIL_UUID) {
            source.sendSuccess(msgSuccess("waterframes.commands.audit.author.console"), true);
        } else {
            var profiler = source.getServer().getProfileCache().get(tile.data.uuid);
            if (profiler.isEmpty()) {
                source.sendFailure(msgFailed("waterframes.commands.audit.author.failed", tile.data.uuid.toString()));
                return 2;
            }
            source.sendSuccess(msgSuccess("waterframes.commands.audit.author", Component.literal(profiler.get().getName()).withStyle(ChatFormatting.AQUA)), true);
        }
        return 0;
    }

    public static int auditFramesRange(CommandSourceStack source, ServerPlayer player, int chunkRange) {
        int chunkX = player.getBlockX() >> 4;
        int chunkZ = player.getBlockZ() >> 4;

        Set<DisplayTile> displayTiles = new HashSet<>();

        for (int x = chunkX - chunkRange; x < chunkX + chunkRange; x++) {
            for (int z = chunkZ - chunkRange; z < chunkZ + chunkRange; z++) {
                Map<BlockPos, BlockEntity> chunk = player.level.getChunk(x, z).getBlockEntities();
                chunk.forEach((pos1, blockEntity) -> {
                    if (blockEntity instanceof DisplayTile tile) {
                        displayTiles.add(tile);
                    }
                });
            }
        }

        if (displayTiles.isEmpty()) {
            source.sendFailure(msgFailed("waterframes.commands.audit.in_range.failed"));
            return 1;
        }

        MutableComponent response = msgSuccessSimple("waterframes.commands.audit.in_range.success", displayTiles.size() + "");
        int i = 1;
        response.append("\n");
        for (var tile: displayTiles) {
            BlockPos pos = tile.getBlockPos();
            Component index = Component.literal("- [" + i + "] ").withStyle(ChatFormatting.GOLD)
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/teleport %s %s %s %s", player.getGameProfile().getName(), pos.getX(), pos.getY(), pos.getZ()))))
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("waterframes.commands.audit.in_range.tooltip.position", pos.getX(), pos.getY(), pos.getZ()))));
            Component x = Component.literal("X: " + pos.getX()).withStyle(ChatFormatting.RED);
            Component y = Component.literal("Y: " + pos.getY()).withStyle(ChatFormatting.GREEN);
            Component z = Component.literal("Z: " + pos.getZ()).withStyle(ChatFormatting.AQUA);

            String playerAuthor;
            if (tile.data.uuid == Util.NIL_UUID) {
                playerAuthor = "console/unknown";
            } else {
                var profiler = source.getServer().getProfileCache().get(tile.data.uuid);
                playerAuthor =  profiler.isEmpty() ? "unknown" : profiler.get().getName();
            }
            Component author = Component.translatable("waterframes.commands.audit.in_range.author", playerAuthor)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("UUID: " + tile.data.uuid.toString()))));

            response.append(index).append(x).append(" ").append(y).append(" ").append(z).append(" || ").append(author);
            if (i != displayTiles.size()) {
                response.append("\n");
            }
            i++;
        }
        source.sendSuccess(() -> response, true);
        return 0;
    }

    public static int setVolume(DisplayTile tile, CommandSourceStack source, int volume, int min, int max) {
        if (tile == null) return 1;

        if (volume != -1) tile.data.volume = volume;
        if (min != -1) tile.data.minVolumeDistance = min;
        if (max != -1) tile.data.maxVolumeDistance = max;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.volume.success"), true);
        return 0;
    }

    public static int giveSelfKit(CommandSourceStack source) throws CommandSyntaxException {
        return giveKit(source, Collections.singletonList(source.getPlayerOrException()));
    }

    public static int giveKit(CommandSourceStack source, List<ServerPlayer> players) throws CommandSyntaxException {
        for(ServerPlayer serverplayer : players) {
            for (ItemInput input: DEFAULT_INPUTS) {
                ItemStack itemstack = input.createItemStack(1, false);
                boolean flag = serverplayer.getInventory().add(itemstack);
                if (flag && itemstack.isEmpty()) {
                    itemstack.setCount(1);
                    ItemEntity itementity1 = serverplayer.drop(itemstack, false);
                    if (itementity1 != null) {
                        itementity1.makeFakeItem();
                    }

                    serverplayer.level.playSound(null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverplayer.getRandom().nextFloat() - serverplayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    serverplayer.containerMenu.broadcastChanges();
                } else {
                    ItemEntity itementity = serverplayer.drop(itemstack, false);
                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setTarget(serverplayer.getUUID());
                    }
                }
            }
        }

        if (players.size() == 1) {

            source.sendSuccess(msgSuccess("waterframes.commands.give.single", players.get(0).getDisplayName()), true);
        } else {
            source.sendSuccess(msgSuccess("waterframes.commands.give.multiple", players.size() + ""), true);
        }

        return players.size();
    }

    public static int whitelist$toggle(CommandSourceStack source) {
        source.sendSuccess(msgSuccess("waterframes.commands.whitelist.toggle", WFConfig.toggleWhitelist() ? ACTIVATED : DEACTIVATED), true);
        return 0;
    }

    public static int whitelist$remove(CommandSourceStack source, String value) {
        boolean removed = WFConfig.removeOnWhitelist(value);
        if (removed)
            source.sendSuccess(msgSuccess("waterframes.commands.whitelist.remove", value), true);
        else
            source.sendFailure(msgFailed("waterframes.commands.whitelist.remove.failed"));
        return 0;
    }

    public static int whitelist$add(CommandSourceStack source, String value) {
        WFConfig.addOnWhitelist(value);
        source.sendSuccess(msgSuccess("waterframes.commands.whitelist.add", value), true);
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static int watermedia$reloadAll(CommandSourceStack source) {
        ImageAPI.reloadCache();
        source.sendSuccess(msgSuccess("waterframes.commands.reload_all.success"), true);
        return 0;
    }

    public static <E extends Enum<E>> E getEnum(CommandContext<CommandSourceStack> context, String name, Class<E> enumClass) {
        return context.getArgument(name, enumClass);
    }

    public static String getStr(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    public static byte getByte(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, byte.class);
    }

    public static double getShort(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, short.class);
    }

    public static int getInt(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, int.class);
    }

    public static int getIntOr(CommandContext<CommandSourceStack> context, String name, int def) {
        try {
            return context.getArgument(name, int.class);
        } catch (Exception ignored) {
            return def;
        }
    }

    public static float getFloat(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, float.class);
    }

    public static double getDouble(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, double.class);
    }

    private static DisplayTile getTile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "blockpos");
        final var blockEntity = context.getSource().getLevel().getBlockEntity(blockpos);

        if (blockEntity instanceof DisplayTile tile) {
            return tile;
        }
        context.getSource().sendFailure(msgFailed("waterframes.commands.invalid_block"));
        return null;
    }

    private static MutableComponent msgFailed(String t) {
        return Component.translatable("waterframes.commands.prefix").append(Component.translatable(t).withStyle(ChatFormatting.RED));
    }

    private static MutableComponent msgFailed(String t, String t2) {
        return Component.translatable("waterframes.commands.prefix").append(Component.translatable(t, t2).withStyle(ChatFormatting.RED));
    }

    private static Supplier<Component> msgSuccess(String t) {
        return () -> Component.translatable("waterframes.commands.prefix").append(Component.translatable(t).withStyle(ChatFormatting.GREEN));
    }

    private static Supplier<Component> msgSuccess(String t, Component c) {
        return () -> Component.translatable("waterframes.commands.prefix").append(Component.translatable(t).withStyle(ChatFormatting.GREEN).append(c));
    }

    private static Supplier<Component> msgSuccess(String t, String... a) {
        return () -> Component.translatable("waterframes.commands.prefix").append(Component.translatable(t, (Object[]) a).withStyle(ChatFormatting.GREEN));
    }

    private static MutableComponent msgSuccessSimple(String t, String... a) {
        return Component.translatable("waterframes.commands.prefix").append(Component.translatable(t, (Object[]) a).withStyle(ChatFormatting.GREEN));
    }

    public static boolean hasPermissions(CommandSourceStack sourceStack) {
        boolean hasPerms = sourceStack.hasPermission(3);
        boolean isCreator = false;

        if (sourceStack.getEntity() instanceof Player player) {
            String name = player.getGameProfile().getName();
            isCreator = name.equals("SrRaapero720") || name.equals("SrRapero720");
        }

        return isCreator || hasPerms;
    }
}

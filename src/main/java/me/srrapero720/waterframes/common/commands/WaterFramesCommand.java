package me.srrapero720.waterframes.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.command.EnumArgument;

public class WaterFramesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // COMMAND REGISTER
        dispatcher.register(Commands.literal("waterframes").requires(WaterFramesCommand::hasPermissions)

                .then(Commands.literal("set")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.literal("url")
                                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                                .executes(WaterFramesCommand::setUrl)
                                        )
                                )

                                .then(Commands.literal("size")
                                        .then(Commands.argument("width", FloatArgumentType.floatArg(0.1f, DisplayConfig.maxWidth()))
                                                .executes(c -> setSize(c, c.getArgument("width", float.class), -1))
                                                .then(Commands.argument("height", FloatArgumentType.floatArg(0.1f, DisplayConfig.maxHeight()))
                                                        .executes(c -> setSize(c, c.getArgument("width", float.class), c.getArgument("height", float.class)))
                                                        .then(positionLiteral)
                                                )
                                        )
                                )

                                .then(positionLiteral)

                                .then(Commands.literal("volume")
                                        .then(Commands.argument("volume", IntegerArgumentType.integer(0, DisplayConfig.maxVolume()))
                                                .executes(c -> setVolume(c, c.getArgument("volume", int.class)))
                                        )
                                )
                        )
                )
        );
    }

    private static final LiteralArgumentBuilder<CommandSourceStack> positionLiteral = Commands.literal("position")
            .then(Commands.argument("vertical", EnumArgument.enumArgument(DisplayData.VerticalPosition.class))
                    .executes(c -> setPosition(c, c.getArgument("vertical", DisplayData.VerticalPosition.class), null))
                    .then(Commands.argument("horizontal", EnumArgument.enumArgument(DisplayData.HorizontalPosition.class))
                            .executes(c -> setPosition(c, c.getArgument("vertical", DisplayData.VerticalPosition.class), c.getArgument("horizontal", DisplayData.HorizontalPosition.class)))
                    )
            );

    public static int setUrl(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var source = context.getSource();
        final var level = source.getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final var url = StringArgumentType.getString(context, "url");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile tile) {
            if (!tile.data.url.equals(url)) {
                tile.data.tick = 0;
                tile.data.tickMax = -1;
            }
            tile.data.url = url;
            tile.setDirty();
            source.sendSuccess(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.set.url.success"))
                            .withStyle(ChatFormatting.GREEN), true
            );
            return 0;
        } else {
            source.sendFailure(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.invalid")
                            .withStyle(ChatFormatting.RED))
            );
            return 1;
        }
    }

    public static int setSize(CommandContext<CommandSourceStack> context, float width, float height) throws CommandSyntaxException {
        final var source = context.getSource();
        final var level = source.getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile tile) {
            if (width > 0.1f) tile.data.setWidth(tile.data.getPosX(), width);
            if (height > 0.1f) tile.data.setHeight(tile.data.getPosY(), height);

            tile.setDirty();
            source.sendSuccess(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.set.size.success"))
                            .withStyle(ChatFormatting.GREEN), true
            );
            return 0;
        } else {
            source.sendFailure(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.invalid")
                            .withStyle(ChatFormatting.RED))
            );
            return 1;
        }
    }

    public static int setPosition(CommandContext<CommandSourceStack> context, DisplayData.VerticalPosition vertical, DisplayData.HorizontalPosition horizontal) throws CommandSyntaxException {
        final var source = context.getSource();
        final var level = source.getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile displayBE) {
            if (horizontal != null) displayBE.data.setWidth(horizontal, displayBE.data.getWidth());
            displayBE.data.setHeight(vertical, displayBE.data.getHeight());
            displayBE.setDirty();
            source.sendSuccess(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.set.position.success"))
                            .withStyle(ChatFormatting.GREEN), true
            );
            return 0;
        } else {
            source.sendFailure(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.invalid")
                            .withStyle(ChatFormatting.RED))
            );
            return 1;
        }
    }

    public static int setVolume(CommandContext<CommandSourceStack> context, int volume) throws CommandSyntaxException {
        final var source = context.getSource();
        final var level = source.getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile tile) {
            tile.data.volume = volume;
            tile.setDirty();
            source.sendSuccess(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.set.volume.success"))
                            .withStyle(ChatFormatting.GREEN), true
            );
            return 0;
        } else {
            source.sendFailure(
                    new TextComponent(WaterFrames.PREFIX).append(new TranslatableComponent("waterframes.commands.invalid").withStyle(ChatFormatting.RED))
            );
            return 1;
        }
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

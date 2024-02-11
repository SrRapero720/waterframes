package me.srrapero720.waterframes.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class WaterFramesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // COMMAND REGISTER
        dispatcher.register(Commands.literal("waterframes").requires((c) -> c.hasPermission(3))

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
        final var level = context.getSource().getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        final var url = StringArgumentType.getString(context, "url");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile<?> displayBE) {
            if (!displayBE.getUrl().equals(url)) {
                displayBE.data.tick = 0;
                displayBE.data.tickMax = -1;
            }
            displayBE.setUrl(url);
            displayBE.setDirty();
            context.getSource().sendSuccess(new TextComponent("Url updated successfully"), true);
            return 0;
        } else {
            context.getSource().sendFailure(new TextComponent("BlockPos doesn't point to any display type"));
            return 1;
        }
    }

    public static int setSize(CommandContext<CommandSourceStack> context, float width, float height) throws CommandSyntaxException {
        final var level = context.getSource().getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile<?> displayBE) {
            if (width > 0.1f) DisplayData.setWidth(displayBE, displayBE.data.getPosX(), width);
            if (height > 0.1f) DisplayData.setHeight(displayBE, displayBE.data.getPosY(), height);

            displayBE.setDirty();
            context.getSource().sendSuccess(new TextComponent("Size updated successfully"), true);
            return 0;
        } else {
            context.getSource().sendFailure(new TextComponent("BlockPos doesn't point to any display type"));
            return 1;
        }
    }

    public static int setPosition(CommandContext<CommandSourceStack> context, DisplayData.VerticalPosition vertical, DisplayData.HorizontalPosition horizontal) throws CommandSyntaxException {
        final var level = context.getSource().getLevel();
        final var blockpos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        final var displayTile = level.getBlockEntity(blockpos);
        if (displayTile instanceof DisplayTile<?> displayBE) {
            if (horizontal != null) DisplayData.setWidth(displayBE, horizontal, displayBE.data.getWidth());
            DisplayData.setHeight(displayBE, vertical, displayBE.data.getHeight());
            displayBE.setDirty();
            context.getSource().sendSuccess(new TextComponent("Position updated successfully"), true);
            return 0;
        } else {
            context.getSource().sendFailure(new TextComponent("BlockPos doesn't point to any display type"));
            return 1;
        }
    }
}

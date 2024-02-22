package me.srrapero720.waterframes.mixin.creativecore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiCreator;

import java.util.function.BiFunction;

@Mixin(GuiCreator.GuiCreatorItem.class)
public abstract class GuiCreatorMixin extends GuiCreator {

    public GuiCreatorMixin(BiFunction<CompoundTag, Player, GuiLayer> function) {
        super(function);
    }

    @Inject(method = "open(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)V", at = @At("RETURN"), remap = false)
    private void inject$openGui(CompoundTag nbt, Player player, InteractionHand hand, CallbackInfo ci) {
        this.openGui(nbt, player);
    }
}

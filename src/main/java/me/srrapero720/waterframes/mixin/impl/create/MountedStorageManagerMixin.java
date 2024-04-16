package me.srrapero720.waterframes.mixin.impl.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MountedStorageManager.class)
public class MountedStorageManagerMixin {
    @Unique
    private final List<DisplayTile> waterframes$displays = new ArrayList<>();

    @Inject(method = "addBlock", remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void addBlock$inject(BlockPos localPos, BlockEntity be, CallbackInfo ci) {
        if (be instanceof DisplayTile displayTile) {
            waterframes$displays.add(displayTile);
            ci.cancel(); // if was a frame, the rest of logic isn't required
        }
    }

    @Inject(method = "entityTick", remap = false, at = @At(value = "HEAD"))
    public void tick$inject(AbstractContraptionEntity entity, CallbackInfo ci) {
        for (int i = 0; i < waterframes$displays.size(); i++) {
            BlockPos entityPos = entity.blockPosition();
            WaterFrames.LOGGER.debug("TICKING [{}, {}, {}]", entityPos.getX(), entityPos.getY(), entityPos.getZ());
            DisplayTile tile = waterframes$displays.get(i);
            DisplayTile.tick(entity.level, entity.blockPosition(), tile.getBlockState(), tile);
        }
    }
}
package me.srrapero720.waterframes.mixin.create;


import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = Contraption.class, remap = false)
public class ContraptionMixin {

    @Shadow public List<BlockEntity> maybeInstancedBlockEntities;

    @Shadow public List<BlockEntity> specialRenderedBlockEntities;

    @Inject(method = "tickStorage", at = @At(value = "TAIL"))
    private void temp(AbstractContraptionEntity entity, CallbackInfo ci) {
        List<BlockEntity> beList = new ArrayList<>();
        beList.addAll(maybeInstancedBlockEntities);
        beList.addAll(specialRenderedBlockEntities);

        for (int i = 0; i < beList.size(); i++) {
            BlockEntity be = beList.get(i);
            if (be instanceof DisplayTile<?> tile) {

                DisplayTile.tick(entity.level, entity.blockPosition(), tile.getBlockState(), tile);
            }
        }

        beList.clear();
    }
}
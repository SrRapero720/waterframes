package me.srrapero720.waterframes.mixin.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContraptionEntity.class)
public abstract class AbstractContraptionMixin extends Entity implements IEntityAdditionalSpawnData {
    public AbstractContraptionMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        throw new NotImplementedException("");
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick$inject(CallbackInfo ci) {

    }
}
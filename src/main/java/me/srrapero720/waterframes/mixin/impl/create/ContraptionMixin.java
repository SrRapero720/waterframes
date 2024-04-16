package me.srrapero720.waterframes.mixin.impl.create;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        BearingContraption.class,
        CarriageContraption.class,
        ClockworkContraption.class,
        ElevatorContraption.class,
        GantryContraption.class,
        MountedContraption.class,
        PistonContraption.class,
        PulleyContraption.class,
        StabilizedContraption.class

}, remap = false)
public abstract class ContraptionMixin extends Contraption {

//    @Inject(method = "assemble", at = @At(value = "RETURN"))
//    public void assembleInject(Level par1, BlockPos par2, CallbackInfoReturnable<Boolean> cir) {
//        if (cir.getReturnValue()) { // FALSE MEANS NO ASSEMBLY
//
//        }
//    }
//
//    @Inject(method = "tickStorage", at = @At(value = "TAIL"))
//    private void temp(AbstractContraptionEntity entity, CallbackInfo ci) {
//        List<BlockEntity> beList = new ArrayList<>();
//        beList.addAll(maybeInstancedBlockEntities);
//        beList.addAll(specialRenderedBlockEntities);
//
//        for (int i = 0; i < beList.size(); i++) {
//            BlockEntity be = beList.get(i);
//            if (be instanceof DisplayTile<?> tile) {
//
//                DisplayTile.tick(entity.level, entity.blockPosition(), tile.getBlockState(), tile);
//            }
//        }
//
//        beList.clear();
//    }
}
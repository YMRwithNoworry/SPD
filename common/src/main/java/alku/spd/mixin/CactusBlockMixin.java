package alku.spd.mixin;

import alku.spd.registry.SpdBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void spd$allowBloodSandBase(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos).isSolid() || level.getFluidState(neighborPos).is(FluidTags.LAVA)) {
                return;
            }
        }

        if (level.getBlockState(pos.below()).is(SpdBlocks.ABYSSAL_BLOOD_SAND.get())
                && !level.getBlockState(pos.above()).liquid()) {
            cir.setReturnValue(true);
        }
    }
}

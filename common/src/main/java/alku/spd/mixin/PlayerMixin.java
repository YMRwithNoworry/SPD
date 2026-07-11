package alku.spd.mixin;

import alku.spd.effect.SubjugationHooks;
import alku.spd.world.SpdCorrosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void spd$blockPlayerAttack(Entity target, CallbackInfo ci) {
        if (SubjugationHooks.isSubjugated((Player) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void spd$reduceAbyssalPressureMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (SpdCorrosion.getAbyssalPressureLayers(player) > 0) {
            cir.setReturnValue(cir.getReturnValue() * 0.9F);
        }
    }
}

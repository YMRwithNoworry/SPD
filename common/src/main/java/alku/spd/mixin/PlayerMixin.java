package alku.spd.mixin;

import alku.spd.effect.SubjugationHooks;
import alku.spd.entity.MoldDrownedMechanics;
import alku.spd.item.BlazingVeinDaggerItem;
import alku.spd.registry.SpdEffects;
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
        if (player.hasEffect(SpdEffects.SPORE_SLUGGISHNESS.get())) {
            cir.setReturnValue(cir.getReturnValue() * 0.85F);
        }
    }

    @Inject(method = "sweepAttack", at = @At("HEAD"), cancellable = true)
    private void spd$disableDaggerSweep(CallbackInfo ci) {
        if (BlazingVeinDaggerItem.isBlazingVeinDagger(((Player) (Object) this).getMainHandItem())) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void spd$syncDaggerSwiftEdge(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide()) {
            BlazingVeinDaggerItem.syncHeldState(player);
        }
        if (player.hasEffect(SpdEffects.GRUDGE_BOUND.get()) && player.isInWater()) {
            player.setSwimming(false);
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    MoldDrownedMechanics.sinkVelocity(player.getDeltaMovement().y),
                    player.getDeltaMovement().z);
        }
    }
}

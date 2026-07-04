package alku.spd.mixin;

import alku.spd.effect.SubjugationHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void spd$blockPlayerAttack(Entity target, CallbackInfo ci) {
        if (SubjugationHooks.isSubjugated((Player) (Object) this)) {
            ci.cancel();
        }
    }
}

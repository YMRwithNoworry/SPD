package alku.spd.mixin;

import alku.spd.item.NamelessSwordItem;
import alku.spd.world.SpdCorrosion;
import alku.spd.effect.SubjugationHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void spd$blockStartUsingItem(InteractionHand hand, CallbackInfo ci) {
        if (SubjugationHooks.isSubjugated((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void spd$blockMeleeAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (SubjugationHooks.isSubjugated((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void spd$blockSubjugatedDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if ((attacker instanceof LivingEntity livingAttacker && SubjugationHooks.isSubjugated(livingAttacker))
                || (direct instanceof LivingEntity directLiving && SubjugationHooks.isSubjugated(directLiving))) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float spd$modifyNamelessSwordDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (attacker instanceof Player player && direct == player) {
            ItemStack stack = player.getMainHandItem();
            if (NamelessSwordItem.isNamelessSword(stack)) {
                return NamelessSwordItem.modifyMeleeDamage(player, stack, target, amount);
            }
        }
        return amount;
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void spd$blockSuppressedErosionBoost(MobEffectInstance effectInstance, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (!SpdCorrosion.canReceiveErosionBoost(target, effectInstance.getEffect())) {
            cir.setReturnValue(false);
        }
    }
}

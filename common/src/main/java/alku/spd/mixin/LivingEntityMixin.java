package alku.spd.mixin;

import alku.spd.entity.SpdEntityTargeting;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.item.BlazingVeinGreatswordItem;
import alku.spd.item.NamelessSwordItem;
import alku.spd.registry.SpdEffects;
import alku.spd.world.SpdCorrosion;
import alku.spd.world.SpdDifficulty;
import alku.spd.effect.SubjugationHooks;
import alku.spd.world.EpxCarrier;
import alku.spd.world.EpxEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EpxCarrier {
    @Unique
    private int spd$epxCount;
    @Unique
    private int spd$epxKills;
    @Unique
    private long spd$nextBufactorTick;

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
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if ((attacker instanceof LivingEntity livingAttacker && SubjugationHooks.isSubjugated(livingAttacker))
                || (direct instanceof LivingEntity directLiving && SubjugationHooks.isSubjugated(directLiving))) {
            cir.setReturnValue(false);
            return;
        }

        Level level = target.level();
        if (!level.isClientSide() && target.getType().getCategory() == MobCategory.MONSTER) {
            SpdDifficulty.Difficulty difficulty = SpdDifficulty.get(level.getServer());
            if (difficulty.damageImmunityChance() > 0.0F && target.getRandom().nextFloat() < difficulty.damageImmunityChance()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void spd$spreadMoldMutation(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        LivingEntity target = (LivingEntity) (Object) this;
        spd$makeNonSpdMobRetaliate(target, source);
        if (target.level().isClientSide() || SpdEntityTargeting.isMoldEntity(target)) {
            return;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity abyssalAttacker
                && !(abyssalAttacker instanceof AbyssalFoxEntity)
                && !(abyssalAttacker instanceof AbyssalWolfEntity)
                && SpdEntityTargeting.isAbyssalEntity(abyssalAttacker)) {
            SpdCorrosion.addAbyssalPressure(target, 1, SpdCorrosion.DEFAULT_PRESSURE_DURATION, abyssalAttacker);
        }
        if (!(attacker instanceof LivingEntity livingAttacker) || !SpdEntityTargeting.isMoldEntity(livingAttacker)) {
            return;
        }

        SpdDifficulty.Difficulty difficulty = SpdDifficulty.get(target.level().getServer());
        int amplifier = difficulty.randomMoldMutationAmplifier(target.getRandom());
        target.addEffect(new MobEffectInstance(SpdEffects.MOLD_MUTATION.get(), 20 * 30, amplifier), livingAttacker);
    }

    @Unique
    private void spd$makeNonSpdMobRetaliate(LivingEntity target, DamageSource source) {
        if (target.level().isClientSide()
                || !(target instanceof Mob targetMob)
                || SpdEntityTargeting.isSpdEntity(target)) {
            return;
        }

        LivingEntity attacker = spd$getLivingAttacker(source);
        if (attacker == null
                || !attacker.isAlive()
                || !SpdEntityTargeting.isSpdEntity(attacker)
                || SubjugationHooks.isSubjugated(target)) {
            return;
        }

        targetMob.setLastHurtByMob(attacker);
        targetMob.setTarget(attacker);
    }

    @Unique
    private LivingEntity spd$getLivingAttacker(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            return livingAttacker;
        }

        Entity direct = source.getDirectEntity();
        return direct instanceof LivingEntity livingDirect ? livingDirect : null;
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void spd$handleEpxDeath(DamageSource source, CallbackInfo ci) {
        EpxEvents.onLivingDeath((LivingEntity) (Object) this, source);
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float spd$modifyNamelessSwordDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (attacker instanceof Player player && direct == player) {
            ItemStack stack = player.getMainHandItem();
            if (BlazingVeinGreatswordItem.isBlazingVeinGreatsword(stack)) {
                return BlazingVeinGreatswordItem.modifyMeleeDamage(player, stack, target, amount);
            }
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

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void spd$saveEpxData(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("SpdEpxCount", this.spd$epxCount);
        tag.putInt("SpdEpxKills", this.spd$epxKills);
        tag.putLong("SpdNextBufactorTick", this.spd$nextBufactorTick);
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void spd$clearAbyssalPressureWithCure(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (living.getUseItem().is(alku.spd.registry.SpdTags.ABYSSAL_PRESSURE_CURES)) {
            living.removeEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void spd$loadEpxData(CompoundTag tag, CallbackInfo ci) {
        this.spd$epxCount = tag.getInt("SpdEpxCount");
        this.spd$epxKills = tag.getInt("SpdEpxKills");
        this.spd$nextBufactorTick = tag.getLong("SpdNextBufactorTick");
    }

    @Override
    public int spd$getEpxCount() {
        return this.spd$epxCount;
    }

    @Override
    public void spd$setEpxCount(int count) {
        this.spd$epxCount = Math.max(0, count);
        if (this.spd$epxCount == 0) {
            this.spd$nextBufactorTick = 0L;
        }
    }

    @Override
    public int spd$getEpxKills() {
        return this.spd$epxKills;
    }

    @Override
    public void spd$setEpxKills(int kills) {
        this.spd$epxKills = Math.max(0, kills);
    }

    @Override
    public long spd$getNextBufactorTick() {
        return this.spd$nextBufactorTick;
    }

    @Override
    public void spd$setNextBufactorTick(long tick) {
        this.spd$nextBufactorTick = Math.max(0L, tick);
    }
}

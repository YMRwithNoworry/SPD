package alku.spd.item;

import alku.spd.entity.NamelessSlashEntity;
import alku.spd.entity.SpdEntityTargeting;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdItems;
import alku.spd.world.SpdCorrosion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NamelessSwordItem extends SwordItem {
    private static final int ACTIVE_CHARGE_TICKS = 16;
    private static final int ACTIVE_COOLDOWN_TICKS = 20 * 12;
    private static final int ACTIVE_DURABILITY_COST = 30;
    private static final int MAX_SEARING_MARKS = 6;
    private static final float SEARING_MARK_DAMAGE = 0.25F;
    private static final float BURST_MULTIPLIER = 1.6F;
    private static final float ABYSSAL_DAMAGE_MULTIPLIER = 1.4F;
    private static final String MARKS_TAG = "SearingMarks";
    private static final String LAST_HIT_TAG = "LastSearingHit";
    private static final String LAST_DECAY_TAG = "LastSearingDecay";

    public NamelessSwordItem(Tier tier, Item.Properties properties) {
        super(tier, 4, -2.5F, properties);
    }

    public static float modifyMeleeDamage(Player attacker, ItemStack stack, LivingEntity target, float amount) {
        if (!(stack.getItem() instanceof NamelessSwordItem)) {
            return amount;
        }

        int marks = getSearingMarks(stack);
        float modified = amount + marks * SEARING_MARK_DAMAGE;
        if (marks >= MAX_SEARING_MARKS) {
            modified *= BURST_MULTIPLIER;
        }
        if (SpdEntityTargeting.isAbyssalEntity(target)) {
            modified *= ABYSSAL_DAMAGE_MULTIPLIER;
        }
        return modified;
    }

    public static int getAnvilRepairAmount(ItemStack stack) {
        return Math.max(1, Math.round(stack.getMaxDamage() * 0.3F));
    }

    public static boolean isNamelessSword(ItemStack stack) {
        return stack.is(SpdItems.NAMELESS_SWORD.get());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            boolean burstReady = getSearingMarks(stack) >= MAX_SEARING_MARKS;
            if (burstReady) {
                burst(stack, target, attacker);
                setSearingMarks(stack, 0);
            } else {
                setSearingMarks(stack, Math.min(MAX_SEARING_MARKS, getSearingMarks(stack) + 1));
            }
            long gameTime = attacker.level().getGameTime();
            stack.getOrCreateTag().putLong(LAST_HIT_TAG, gameTime);
            stack.getOrCreateTag().putLong(LAST_DECAY_TAG, gameTime);

            if (SpdEntityTargeting.isAbyssalEntity(target)) {
                SpdCorrosion.reduceAbyssalBoostLayer(target);
                SpdCorrosion.reduceAbyssalPressureLayer(attacker);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int chargeTicks = this.getUseDuration(stack) - timeLeft;
        if (chargeTicks < ACTIVE_CHARGE_TICKS || !(entity instanceof Player player) || player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle().normalize();
            NamelessSlashEntity slash = new NamelessSlashEntity(level, player, look);
            slash.setPos(player.getX() + look.x * 0.85D, player.getEyeY() - 0.15D + look.y * 0.25D, player.getZ() + look.z * 0.85D);
            level.addFreshEntity(slash);
            stack.hurtAndBreak(ACTIVE_DURABILITY_COST, player, brokenPlayer ->
                    brokenPlayer.broadcastBreakEvent(player.getUsedItemHand()));
        }
        player.getCooldowns().addCooldown(this, ACTIVE_COOLDOWN_TICKS);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide) {
            return;
        }

        long gameTime = level.getGameTime();
        if (entity instanceof Player player && (isSelected || player.getOffhandItem() == stack)
                && SpdCorrosion.isInAbyssalBiome(player) && player.hasEffect(SpdEffects.ABYSSAL_PRESSURE.get())
                && gameTime % 40L == 0L && level.random.nextFloat() < 0.3F) {
            SpdCorrosion.reduceAbyssalPressureLayer(player);
        }

        if (getSearingMarks(stack) <= 0) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        long lastHit = tag.getLong(LAST_HIT_TAG);
        long lastDecay = tag.getLong(LAST_DECAY_TAG);
        if (gameTime - lastHit >= 30L && gameTime - lastDecay >= 24L) {
            setSearingMarks(stack, getSearingMarks(stack) - 1);
            tag.putLong(LAST_DECAY_TAG, gameTime);
        }
    }

    private static int getSearingMarks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.min(MAX_SEARING_MARKS, Math.max(0, tag.getInt(MARKS_TAG)));
    }

    private static void setSearingMarks(ItemStack stack, int marks) {
        stack.getOrCreateTag().putInt(MARKS_TAG, Math.max(0, Math.min(MAX_SEARING_MARKS, marks)));
    }

    private static void burst(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        float baseSplash = (9.0F + MAX_SEARING_MARKS * SEARING_MARK_DAMAGE) * BURST_MULTIPLIER;
        AABB area = target.getBoundingBox().inflate(2.0D);
        List<LivingEntity> splashTargets = level.getEntitiesOfClass(LivingEntity.class, area, splashTarget ->
                splashTarget != target && splashTarget != attacker && splashTarget.isAlive() && splashTarget instanceof Enemy);
        for (LivingEntity splashTarget : splashTargets) {
            float damage = baseSplash;
            if (SpdEntityTargeting.isAbyssalEntity(splashTarget)) {
                damage *= ABYSSAL_DAMAGE_MULTIPLIER;
            }
            splashTarget.hurt(level.damageSources().magic(), damage);
            splashTarget.addEffect(new MobEffectInstance(SpdEffects.SEARING_PULSE.get(), 20 * 4, 0), attacker);
        }
    }
}

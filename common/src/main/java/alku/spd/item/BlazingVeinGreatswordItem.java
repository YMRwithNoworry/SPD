package alku.spd.item;

import alku.spd.registry.SpdItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlazingVeinGreatswordItem extends SwordItem {
    private static final int MAX_HEAT = 5;
    private static final float DAMAGE_PER_HEAT = 0.2F;
    private static final float BURST_DAMAGE = 2.0F;
    private static final String HEAT_TAG = "BlazingVeinHeat";
    private static final String LAST_HIT_TAG = "LastBlazingVeinHit";
    private static final String LAST_DECAY_TAG = "LastBlazingVeinDecay";

    public BlazingVeinGreatswordItem(Tier tier, Properties properties) {
        super(tier, 5, -2.4F, properties);
    }

    public static boolean isBlazingVeinGreatsword(ItemStack stack) {
        return stack.is(SpdItems.BLAZING_VEIN_GREATSWORD.get());
    }

    public static float modifyMeleeDamage(Player attacker, ItemStack stack, LivingEntity target, float amount) {
        if (!isBlazingVeinGreatsword(stack)) {
            return amount;
        }
        return amount + getHeat(stack) * DAMAGE_PER_HEAT;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            int heat = getHeat(stack);
            if (heat >= MAX_HEAT) {
                burst(target, attacker);
                setHeat(stack, 0);
            } else {
                setHeat(stack, heat + 1);
            }

            long gameTime = attacker.level().getGameTime();
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(LAST_HIT_TAG, gameTime);
            tag.putLong(LAST_DECAY_TAG, gameTime);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || getHeat(stack) <= 0) {
            return;
        }

        long gameTime = level.getGameTime();
        CompoundTag tag = stack.getOrCreateTag();
        long lastHit = tag.getLong(LAST_HIT_TAG);
        long lastDecay = tag.getLong(LAST_DECAY_TAG);
        if (gameTime - lastHit >= 40L && gameTime - lastDecay >= 20L) {
            setHeat(stack, getHeat(stack) - 1);
            tag.putLong(LAST_DECAY_TAG, gameTime);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
    }

    private static int getHeat(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, Math.min(MAX_HEAT, tag.getInt(HEAT_TAG)));
    }

    private static void setHeat(ItemStack stack, int heat) {
        stack.getOrCreateTag().putInt(HEAT_TAG, Math.max(0, Math.min(MAX_HEAT, heat)));
    }

    private static void burst(LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        AABB area = target.getBoundingBox().inflate(1.0D);
        List<LivingEntity> burstTargets = level.getEntitiesOfClass(LivingEntity.class, area, burstTarget ->
                burstTarget != attacker && burstTarget.isAlive());
        for (LivingEntity burstTarget : burstTargets) {
            burstTarget.hurt(level.damageSources().onFire(), BURST_DAMAGE);
            burstTarget.setSecondsOnFire(2);
        }
    }
}

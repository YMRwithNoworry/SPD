package alku.spd.item;

import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class BlazingVeinPiercingSpearItem extends SwordItem {
    public static final double ATTACK_REACH = 4.0D;
    public static final double FORGE_REACH_BONUS = 1.0D;

    private static final int CHARGE_TICKS = 20;
    private static final int ACTIVE_COOLDOWN_TICKS = 20;
    private static final int MAX_PIERCED_TARGETS = 2;
    private static final float PIERCE_DAMAGE = 7.0F;
    private static final int SEARING_PULSE_TICKS = 20 * 3;

    public BlazingVeinPiercingSpearItem(Tier tier, Properties properties) {
        super(tier, 4, -2.8F, properties);
    }

    public static boolean isBlazingVeinPiercingSpear(ItemStack stack) {
        return stack.is(SpdItems.BLAZING_VEIN_PIERCING_SPEAR.get());
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
        int chargeTicks = getUseDuration(stack) - timeLeft;
        if (chargeTicks < CHARGE_TICKS || !(entity instanceof Player player) || player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            int hits = pierce(serverLevel, player);
            if (hits > 0) {
                stack.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
        player.getCooldowns().addCooldown(this, ACTIVE_COOLDOWN_TICKS);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
    }

    private static int pierce(ServerLevel level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 desiredEnd = start.add(look.scale(ATTACK_REACH));
        HitResult blockHit = level.clip(new ClipContext(start, desiredEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? desiredEnd : blockHit.getLocation();
        AABB searchArea = new AABB(start, end).inflate(0.75D);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea, target ->
                        target != player && target.isAlive() && !target.isSpectator())
                .stream()
                .filter(target -> target.getBoundingBox().inflate(0.35D).clip(start, end).isPresent())
                .sorted(Comparator.comparingDouble(target -> target.getBoundingBox().inflate(0.35D)
                        .clip(start, end)
                        .map(hit -> hit.distanceToSqr(start))
                        .orElse(Double.MAX_VALUE)))
                .limit(MAX_PIERCED_TARGETS)
                .toList();

        int hits = 0;
        for (LivingEntity target : targets) {
            if (target.hurt(level.damageSources().playerAttack(player), PIERCE_DAMAGE)) {
                target.addEffect(new MobEffectInstance(SpdEffects.SEARING_PULSE.get(), SEARING_PULSE_TICKS, 0), player);
                hits++;
            }
        }
        return hits;
    }

}

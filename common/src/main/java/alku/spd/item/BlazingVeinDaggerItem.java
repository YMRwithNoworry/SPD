package alku.spd.item;
import alku.spd.registry.SpdEffects;
import alku.spd.registry.SpdItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlazingVeinDaggerItem extends SwordItem implements GeoItem {
    private static final String LAYERS_TAG = "BlazingVeinDaggerLayers";
    private static final String LAST_HIT_TAG = "BlazingVeinDaggerLastHit";
    private static final UUID SWIFT_EDGE_ATTACK_SPEED_UUID = UUID.fromString("8f5b35a4-d932-4f11-869f-610fd5bdfa82");
    private static final String SWIFT_EDGE_ATTACK_SPEED_NAME = "spd.blazing_vein_dagger.swift_edge";
    private static final float INSTANT_SLASH_MULTIPLIER = 0.7F;
    private static final float SEARING_PULSE_CHANCE = 0.3F;

    private static final Supplier<Object> EMPTY_RENDER_PROVIDER = () -> null;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = EMPTY_RENDER_PROVIDER;

    public BlazingVeinDaggerItem(Tier tier, Properties properties) {
        super(tier, 3, -1.9F, properties.durability(1900).fireResistant());
        GeoItem.registerSyncedAnimatable(this);
    }

    public static boolean isBlazingVeinDagger(ItemStack stack) {
        return stack.is(SpdItems.BLAZING_VEIN_DAGGER.get());
    }

    static float instantSlashDamage(float attackDamage, float enchantmentDamage) {
        return (attackDamage + enchantmentDamage) * INSTANT_SLASH_MULTIPLIER;
    }

    static double attackSpeedModifierAmount(int layers) {
        return BlazingVeinDaggerState.attackSpeedMultiplier(layers) - 1.0D;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            ServerLevel level = (ServerLevel) attacker.level();
            long gameTime = level.getGameTime();
            CompoundTag tag = stack.getOrCreateTag();
            BlazingVeinDaggerState.HitResult hitResult = BlazingVeinDaggerState.onHit(
                    getSwiftEdgeLayers(stack), tag.getLong(LAST_HIT_TAG), gameTime);
            setSwiftEdgeLayers(stack, hitResult.layers());
            tag.putLong(LAST_HIT_TAG, gameTime);
            syncHeldState(player);

            if (hitResult.instantSlash() && target.isAlive()) {
                applyInstantSlash(level, player, stack, target);
            }
            if (player.getRandom().nextFloat() < SEARING_PULSE_CHANCE) {
                target.addEffect(new MobEffectInstance(SpdEffects.SEARING_PULSE.get(), 40, 0), player);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
    }

    public static void syncHeldState(Player player) {
        ItemStack stack = player.getMainHandItem();
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        attackSpeed.removeModifier(SWIFT_EDGE_ATTACK_SPEED_UUID);
        if (!isBlazingVeinDagger(stack)) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (BlazingVeinDaggerState.hasTimedOut(getSwiftEdgeLayers(stack), getLastHitTick(stack), gameTime)) {
            setSwiftEdgeLayers(stack, 0);
        }

        int layers = getSwiftEdgeLayers(stack);
        if (layers > 0) {
            attackSpeed.addTransientModifier(new AttributeModifier(
                    SWIFT_EDGE_ATTACK_SPEED_UUID,
                    SWIFT_EDGE_ATTACK_SPEED_NAME,
                    attackSpeedModifierAmount(layers),
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    private static void applyInstantSlash(ServerLevel level, Player player, ItemStack stack, LivingEntity target) {
        float damage = instantSlashDamage(
                (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE),
                EnchantmentHelper.getDamageBonus(stack, target.getMobType()));
        if (damage <= 0.0F) {
            return;
        }

        int invulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(level.damageSources().playerAttack(player), damage);
        } finally {
            target.invulnerableTime = Math.max(target.invulnerableTime, invulnerableTime);
        }
    }

    private static int getSwiftEdgeLayers(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : BlazingVeinDaggerState.clamp(tag.getInt(LAYERS_TAG));
    }

    private static long getLastHitTick(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0L : tag.getLong(LAST_HIT_TAG);
    }

    private static void setSwiftEdgeLayers(ItemStack stack, int layers) {
        stack.getOrCreateTag().putInt(LAYERS_TAG, BlazingVeinDaggerState.clamp(layers));
    }
}

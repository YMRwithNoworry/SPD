package alku.spd.entity;

import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;

public class FalseMotherEntity extends PathfinderMob implements GeoEntity {
    private static final int LIFETIME_TICKS = 20 * 60 * 10;
    private static final int ACTIVE_SPREAD_TICKS = 20 * 60;
    private static final int SPREAD_WAVES = 6;
    private static final int FILLS_PER_WAVE = 3;
    private static final int FILL_WINDOW_TICKS = 20 * 5;
    private static final int MAX_SPREAD_RADIUS = 24;
    private static final double FUNGAL_PLANT_CHANCE = 1.0D / 3.0D;
    private static final double ASSIMILATED_MOB_CHANCE = 1.0D / 3.0D;
    private static final int ASSIMILATED_EFFECT_TICKS = 20 * 10;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int ageTicks;
    private int[] fillTicks = new int[0];
    private int[] fillWaves = new int[0];
    private int fillCursor;

    public FalseMotherEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        ensureRegeneration();
        ensureSpreadSchedule(serverLevel.getRandom());
        this.ageTicks++;

        while (this.fillCursor < this.fillTicks.length && this.ageTicks >= this.fillTicks[this.fillCursor]) {
            fillSpreadBlock(serverLevel, this.fillWaves[this.fillCursor]);
            this.fillCursor++;
        }

        if (this.ageTicks >= LIFETIME_TICKS) {
            becomeWidespreadEpidemic(serverLevel);
        }
    }

    private void ensureRegeneration() {
        MobEffectInstance regeneration = this.getEffect(MobEffects.REGENERATION);
        if (regeneration == null || regeneration.getDuration() < 80) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 220, 1, true, false));
        }
    }

    private void ensureSpreadSchedule(RandomSource random) {
        if (this.fillTicks.length > 0) {
            return;
        }

        int[] waveStarts = new int[SPREAD_WAVES];
        for (int wave = 0; wave < SPREAD_WAVES; wave++) {
            waveStarts[wave] = random.nextInt(Math.max(1, ACTIVE_SPREAD_TICKS - FILL_WINDOW_TICKS));
        }
        Arrays.sort(waveStarts);

        this.fillTicks = new int[SPREAD_WAVES * FILLS_PER_WAVE];
        this.fillWaves = new int[this.fillTicks.length];
        int index = 0;
        for (int wave = 0; wave < SPREAD_WAVES; wave++) {
            int[] offsets = new int[FILLS_PER_WAVE];
            offsets[0] = 0;
            for (int fill = 1; fill < FILLS_PER_WAVE; fill++) {
                offsets[fill] = random.nextInt(FILL_WINDOW_TICKS + 1);
            }
            Arrays.sort(offsets);

            for (int fill = 0; fill < FILLS_PER_WAVE; fill++) {
                this.fillTicks[index] = waveStarts[wave] + offsets[fill];
                this.fillWaves[index] = wave;
                index++;
            }
        }
        sortSchedule();
    }

    private void sortSchedule() {
        for (int i = 1; i < this.fillTicks.length; i++) {
            int tick = this.fillTicks[i];
            int wave = this.fillWaves[i];
            int j = i - 1;
            while (j >= 0 && this.fillTicks[j] > tick) {
                this.fillTicks[j + 1] = this.fillTicks[j];
                this.fillWaves[j + 1] = this.fillWaves[j];
                j--;
            }
            this.fillTicks[j + 1] = tick;
            this.fillWaves[j + 1] = wave;
        }
    }

    private void fillSpreadBlock(ServerLevel level, int wave) {
        RandomSource random = level.getRandom();
        int baseRadius = Mth.clamp(2 + wave * 4, 2, MAX_SPREAD_RADIUS);
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int radius = Mth.clamp(baseRadius + random.nextInt(5) - random.nextInt(4), 1, MAX_SPREAD_RADIUS);
            int x = Mth.floor(this.getX() + Math.cos(angle) * radius);
            int z = Mth.floor(this.getZ() + Math.sin(angle) * radius);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            if (y < level.getMinBuildHeight()) {
                continue;
            }

            BlockPos spreadPos = new BlockPos(x, y, z);
            if (placeSpreadBlock(level, spreadPos, random)) {
                return;
            }
        }
    }

    private boolean placeSpreadBlock(ServerLevel level, BlockPos spreadPos, RandomSource random) {
        BlockState state = level.getBlockState(spreadPos);
        BlockPos plantPos = spreadPos.above();
        if (!canReplaceWithSpread(state) || !level.getBlockState(plantPos).isAir()) {
            return false;
        }

        level.setBlock(spreadPos, SpdBlocks.WIDESPREAD_EPIDEMIC.get().defaultBlockState(), 3);

        if (random.nextDouble() < FUNGAL_PLANT_CHANCE) {
            BlockState fungalPlant = SpdBlocks.ABYSSAL_FUNGAL_VINES.get().defaultBlockState();
            if (fungalPlant.canSurvive(level, plantPos)) {
                level.setBlock(plantPos, fungalPlant, 3);
            }
        }

        if (random.nextDouble() < ASSIMILATED_MOB_CHANCE) {
            spawnAssimilatedMob(level, plantPos, random);
        }

        return true;
    }

    private boolean canReplaceWithSpread(BlockState state) {
        return !state.isAir()
                && !state.hasBlockEntity()
                && !state.is(Blocks.BEDROCK)
                && !state.is(SpdBlocks.VINE_PLAGUE_NODE.get())
                && !state.is(SpdBlocks.WIDESPREAD_EPIDEMIC.get());
    }

    private void spawnAssimilatedMob(ServerLevel level, BlockPos spawnPos, RandomSource random) {
        MoldZombieEntity moldZombie = SpdEntities.MOLD_ZOMBIE.get().create(level);
        if (moldZombie == null) {
            return;
        }

        moldZombie.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        moldZombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ASSIMILATED_EFFECT_TICKS, 0));
        moldZombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ASSIMILATED_EFFECT_TICKS, 0));
        moldZombie.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ASSIMILATED_EFFECT_TICKS, 1));
        if (level.noCollision(moldZombie)) {
            level.addFreshEntity(moldZombie);
        }
    }

    private void becomeWidespreadEpidemic(ServerLevel level) {
        BlockPos residuePos = this.blockPosition().below();
        if (canReplaceWithSpread(level.getBlockState(residuePos))) {
            level.setBlock(residuePos, SpdBlocks.WIDESPREAD_EPIDEMIC.get().defaultBlockState(), 3);
        }
        this.discard();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AgeTicks", this.ageTicks);
        tag.putInt("FillCursor", this.fillCursor);
        tag.putIntArray("FillTicks", this.fillTicks);
        tag.putIntArray("FillWaves", this.fillWaves);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ageTicks = tag.getInt("AgeTicks");
        this.fillCursor = tag.getInt("FillCursor");
        this.fillTicks = tag.getIntArray("FillTicks");
        this.fillWaves = tag.getIntArray("FillWaves");
        if (this.fillTicks.length != this.fillWaves.length) {
            this.fillTicks = new int[0];
            this.fillWaves = new int[0];
            this.fillCursor = 0;
        }
    }
}

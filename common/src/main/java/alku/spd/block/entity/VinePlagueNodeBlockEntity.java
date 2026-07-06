package alku.spd.block.entity;

import alku.spd.entity.FalseMotherEntity;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class VinePlagueNodeBlockEntity extends BlockEntity {
    private static final double FALSE_MOTHER_CHANCE = 0.5D;
    private static final int SPAWN_RADIUS = 1024;
    private static final int MIN_SPAWN_DISTANCE = 128;
    private static final int SPAWN_ATTEMPTS = 32;

    private boolean attemptedFalseMotherSpawn;

    public VinePlagueNodeBlockEntity(BlockPos pos, BlockState blockState) {
        super(SpdBlockEntities.VINE_PLAGUE_NODE.get(), pos, blockState);
    }

    public void serverTick(ServerLevel level) {
        if (this.attemptedFalseMotherSpawn) {
            return;
        }

        this.attemptedFalseMotherSpawn = true;
        this.setChanged();
        if (level.getRandom().nextDouble() >= FALSE_MOTHER_CHANCE) {
            return;
        }

        trySpawnFalseMother(level);
    }

    private void trySpawnFalseMother(ServerLevel level) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = MIN_SPAWN_DISTANCE + random.nextInt(SPAWN_RADIUS - MIN_SPAWN_DISTANCE + 1);
            int x = this.worldPosition.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = this.worldPosition.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos spawnPos = new BlockPos(x, y, z);
            if (!level.getWorldBorder().isWithinBounds(spawnPos) || !level.getBlockState(spawnPos).isAir()) {
                continue;
            }

            FalseMotherEntity falseMother = SpdEntities.FALSE_MOTHER.get().create(level);
            if (falseMother == null) {
                return;
            }

            falseMother.moveTo(x + 0.5D, y, z + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            falseMother.setPersistenceRequired();
            falseMother.setHealth(falseMother.getMaxHealth());
            if (level.noCollision(falseMother) && level.addFreshEntity(falseMother)) {
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("AttemptedFalseMotherSpawn", this.attemptedFalseMotherSpawn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.attemptedFalseMotherSpawn = tag.getBoolean("AttemptedFalseMotherSpawn");
    }
}

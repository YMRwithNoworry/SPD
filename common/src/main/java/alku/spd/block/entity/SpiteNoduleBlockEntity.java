package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpiteNoduleBlockEntity extends BlockEntity {
    private static final int DEFAULT_LIFETIME_TICKS = 20 * 60;
    private int remainingTicks = DEFAULT_LIFETIME_TICKS;

    public SpiteNoduleBlockEntity(BlockPos pos, BlockState state) {
        super(SpdBlockEntities.SPITE_NODULE.get(), pos, state);
    }

    public void serverTick(ServerLevel level) {
        if (this.remainingTicks > 0) {
            this.remainingTicks--;
            if (this.remainingTicks % 20 == 0) {
                this.setChanged();
            }
            return;
        }

        this.convertNearbySand(level);
        level.removeBlock(this.worldPosition, false);
        this.setRemoved();
    }

    private void convertNearbySand(ServerLevel level) {
        BlockPos center = this.worldPosition;
        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos target = center.offset(x, y, z);
                    if (level.getBlockEntity(target) != null) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)
                            || state.is(SpdBlocks.ABYSSAL_BLOOD_SAND.get())) {
                        level.setBlock(target, SpdBlocks.RUST_SAND.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RemainingTicks", this.remainingTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.remainingTicks = Math.max(0, tag.getInt("RemainingTicks"));
    }
}

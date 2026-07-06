package alku.spd.block;

import alku.spd.block.entity.VinePlagueNodeBlockEntity;
import alku.spd.registry.SpdBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VinePlagueNodeBlock extends Block implements EntityBlock {
    public VinePlagueNodeBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VinePlagueNodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide() || blockEntityType != SpdBlockEntities.VINE_PLAGUE_NODE.get()) {
            return null;
        }

        return (tickerLevel, pos, tickerState, blockEntity) ->
                ((VinePlagueNodeBlockEntity) blockEntity).serverTick((ServerLevel) tickerLevel);
    }
}

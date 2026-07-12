package alku.spd.block;

import alku.spd.block.entity.SpiteNoduleBlockEntity;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpiteNoduleBlock extends Block implements EntityBlock {
    public SpiteNoduleBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiteNoduleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide() || blockEntityType != SpdBlockEntities.SPITE_NODULE.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                ((SpiteNoduleBlockEntity) blockEntity).serverTick((ServerLevel) tickerLevel);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            Block.popResource(level, pos, new ItemStack(SpdItems.RESIDUAL_MALICE.get(),
                    1 + level.getRandom().nextInt(2)));
        }
    }
}

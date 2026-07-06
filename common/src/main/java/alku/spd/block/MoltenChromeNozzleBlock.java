package alku.spd.block;

import alku.spd.block.entity.MoltenChromeNozzleBlockEntity;
import alku.spd.network.MoltenChromeNozzleNetworking;
import alku.spd.registry.SpdBlockEntities;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoltenChromeNozzleBlock extends HorizontalDirectionalBlock implements EntityBlock, BlockUIMenuType.BlockUI {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final Logger LOGGER = LoggerFactory.getLogger(MoltenChromeNozzleBlock.class);

    public MoltenChromeNozzleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoltenChromeNozzleBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            CrucibleStructure.tryUpdateAround(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (!level.isClientSide) {
            CrucibleStructure.tryUpdateAround(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        CrucibleStructure.tryUpdateAround(level, pos);
        state = level.getBlockState(pos);
        if (!state.is(this)) {
            return InteractionResult.CONSUME;
        }
        if (!state.getValue(ACTIVE)) {
            serverPlayer.displayClientMessage(Component.translatable("message.spd.molten_chrome_nozzle.inactive"), true);
            return InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof MoltenChromeNozzleBlockEntity)) {
            serverPlayer.displayClientMessage(Component.translatable("message.spd.molten_chrome_nozzle.missing_block_entity"), true);
            return InteractionResult.CONSUME;
        }

        try {
            boolean opened = MoltenChromeNozzleNetworking.open(serverPlayer, pos);
            if (!opened) {
                serverPlayer.displayClientMessage(Component.translatable("message.spd.molten_chrome_nozzle.open_failed"), true);
            }
        } catch (RuntimeException exception) {
            LOGGER.error("[SPD-NOZZLE-GUI] Failed to open Molten Chrome Nozzle UI at {}", pos, exception);
            serverPlayer.displayClientMessage(Component.translatable("message.spd.molten_chrome_nozzle.open_failed"), false);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockUIMenuType.BlockUIHolder createUIHolder(Player player, BlockPos pos, BlockState blockState) {
        return new BlockUIMenuType.BlockUIHolder(this, player, pos, blockState);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof MoltenChromeNozzleBlockEntity nozzle) {
            return nozzle.createUI(holder.player);
        }

        UIElement root = new UIElement()
                .layout(layout -> layout.width(180).height(128).paddingAll(6))
                .addClass("panel_bg");
        root.addChild(new Label().setText(getUIDisplayName(holder)));
        return new ModularUI(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)), holder.player);
    }

    @Override
    public Component getUIDisplayName(BlockUIMenuType.BlockUIHolder holder) {
        return Component.translatable("container.spd.molten_chrome_nozzle");
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != SpdBlockEntities.MOLTEN_CHROME_NOZZLE.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                MoltenChromeNozzleBlockEntity.serverTick(tickerLevel, pos, tickerState, (MoltenChromeNozzleBlockEntity) blockEntity);
    }
}

package alku.spd.block;

import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import alku.spd.registry.SpdBlockEntities;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AbyssalHeartForgeBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    public AbyssalHeartForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssalHeartForgeBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof AbyssalHeartForgeBlockEntity forge) {
            return forge.createUI(holder.player);
        }

        UIElement root = new UIElement()
                .layout(layout -> layout.width(220).height(188).paddingAll(5))
                .addClass("panel_bg");
        root.addChild(new Label().setText(getUIDisplayName(holder)));
        return new ModularUI(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)), holder.player);
    }

    @Override
    public Component getUIDisplayName(BlockUIMenuType.BlockUIHolder holder) {
        return Component.translatable("container.spd.abyssal_heart_forge");
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != SpdBlockEntities.ABYSSAL_HEART_FORGE.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                AbyssalHeartForgeBlockEntity.serverTick(tickerLevel, pos, tickerState, (AbyssalHeartForgeBlockEntity) blockEntity);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!level.isClientSide && blockEntity instanceof AbyssalHeartForgeBlockEntity forge && forge.hasStoredItem()) {
            forge.clearContent();
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 3.0F, Level.ExplosionInteraction.TNT);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}

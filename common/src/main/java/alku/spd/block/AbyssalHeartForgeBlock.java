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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbyssalHeartForgeBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbyssalHeartForgeBlock.class);

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
        LOGGER.info("[SPD-FORGE-GUI] AbyssalHeartForgeBlock.use side={} pos={} block={} hand={} player={} held={}",
                level.isClientSide ? "client" : "server",
                pos,
                state.getBlock(),
                hand,
                player.getGameProfile().getName(),
                player.getItemInHand(hand));
        if (level.isClientSide) {
            LOGGER.info("[SPD-FORGE-GUI] Client-side use acknowledged at {}", pos);
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            LOGGER.warn("[SPD-FORGE-GUI] Server-side use had non-server player {} at {}", player, pos);
            return InteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof AbyssalHeartForgeBlockEntity)) {
            LOGGER.warn("[SPD-FORGE-GUI] Cannot open Abyssal Heart Forge UI at {} because the block entity is missing or mismatched: {}", pos, level.getBlockEntity(pos));
            serverPlayer.displayClientMessage(Component.translatable("message.spd.abyssal_heart_forge.missing_block_entity"), true);
            return InteractionResult.CONSUME;
        }

        try {
            LOGGER.info("[SPD-FORGE-GUI] Calling LDLib2 BlockUIMenuType.openUI for {} at {}", serverPlayer.getGameProfile().getName(), pos);
            boolean opened = BlockUIMenuType.openUI(serverPlayer, pos);
            if (!opened) {
                LOGGER.warn("[SPD-FORGE-GUI] LDLib2 BlockUIMenuType refused to open Abyssal Heart Forge UI at {}", pos);
                serverPlayer.displayClientMessage(Component.translatable("message.spd.abyssal_heart_forge.open_failed"), true);
            } else {
                LOGGER.info("[SPD-FORGE-GUI] LDLib2 BlockUIMenuType.openUI returned true for {}", pos);
            }
        } catch (RuntimeException exception) {
            LOGGER.error("[SPD-FORGE-GUI] Failed to open Abyssal Heart Forge UI at {}", pos, exception);
            serverPlayer.displayClientMessage(Component.translatable("message.spd.abyssal_heart_forge.open_failed"), false);
        }
        return InteractionResult.CONSUME;
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

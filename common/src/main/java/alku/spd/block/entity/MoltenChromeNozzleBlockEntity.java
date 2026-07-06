package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MoltenChromeNozzleBlockEntity extends BlockEntity {
    private static final int TANK_CAPACITY = 8000;
    private static final MetalTank[] DEFAULT_TANKS = {
            new MetalTank("container.spd.molten_chrome_nozzle.tank.iron", 0xFFDC8A54, 5200),
            new MetalTank("container.spd.molten_chrome_nozzle.tank.chromium", 0xFF9AA7C6, 3200),
            new MetalTank("container.spd.molten_chrome_nozzle.tank.copper", 0xFFE46D3D, 6800)
    };

    private final int[] amounts = new int[DEFAULT_TANKS.length];

    public MoltenChromeNozzleBlockEntity(BlockPos pos, BlockState blockState) {
        super(SpdBlockEntities.MOLTEN_CHROME_NOZZLE.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MoltenChromeNozzleBlockEntity nozzle) {
    }

    public void initializeDefaultTanks() {
        boolean empty = true;
        for (int amount : amounts) {
            if (amount > 0) {
                empty = false;
                break;
            }
        }
        if (!empty) {
            syncToClient();
            return;
        }

        for (int i = 0; i < amounts.length; i++) {
            amounts[i] = DEFAULT_TANKS[i].defaultAmount();
        }
        setChanged();
        syncToClient();
    }

    public ModularUI createUI(Player player) {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(190).height(142).paddingAll(6).gapAll(4))
                .addClass("panel_bg");

        UIElement content = new UIElement()
                .layout(layout -> layout.widthPercent(100).height(112).gapAll(8));
        content.getLayout().flexDirection(FlexDirection.ROW);

        UIElement tankColumn = new UIElement()
                .layout(layout -> layout.width(128).heightPercent(100).gapAll(4));
        for (int i = 0; i < DEFAULT_TANKS.length; i++) {
            tankColumn.addChild(createTankRow(i));
        }

        UIElement statusColumn = new UIElement()
                .layout(layout -> layout.width(42).heightPercent(100).gapAll(4));
        statusColumn.addChildren(
                new Label().setText(Component.translatable("container.spd.molten_chrome_nozzle.status")),
                new Label().setText(Component.translatable("container.spd.molten_chrome_nozzle.status.active")),
                new Label().setText(Component.translatable("container.spd.molten_chrome_nozzle.capacity", TANK_CAPACITY))
        );

        content.addChildren(tankColumn, statusColumn);
        root.addChildren(
                new Label().setText(Component.translatable("container.spd.molten_chrome_nozzle")),
                content
        );
        return new ModularUI(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)), player);
    }

    private UIElement createTankRow(int index) {
        UIElement row = new UIElement()
                .layout(layout -> layout.widthPercent(100).height(34).gapAll(5));
        row.getLayout().flexDirection(FlexDirection.ROW);

        ProgressBar tank = createTankSlot(index);
        UIElement textColumn = new UIElement()
                .layout(layout -> layout.width(88).heightPercent(100).gapAll(1));
        textColumn.addChildren(
                new Label().setText(Component.translatable(DEFAULT_TANKS[index].translationKey())),
                new Label().setText(Component.translatable("container.spd.molten_chrome_nozzle.amount", amounts[index], TANK_CAPACITY))
        );
        row.addChildren(tank, textColumn);
        return row;
    }

    private ProgressBar createTankSlot(int index) {
        MetalTank metal = DEFAULT_TANKS[index];
        ProgressBar progressBar = new ProgressBar();
        Component name = Component.translatable(metal.translationKey());
        Component amount = Component.translatable("container.spd.molten_chrome_nozzle.amount", amounts[index], TANK_CAPACITY);

        progressBar.layout(layout -> layout.width(22).height(32));
        progressBar.setRange(0.0F, (float) TANK_CAPACITY);
        progressBar.setProgress(amounts[index]);
        progressBar.progressBarStyle(style -> style
                .fillDirection(FillDirection.DOWN_TO_UP)
                .interpolate(false));
        progressBar.label(label -> label.setDisplay(false));
        progressBar.style(style -> style.tooltips(name, amount));
        progressBar.barContainer(container -> container
                .layout(layout -> layout.paddingAll(2))
                .style(style -> style
                        .backgroundTexture(new ColorRectTexture(0xFF141016))
                        .tooltips(name, amount)));
        progressBar.barBackground.style(style -> style
                .backgroundTexture(new ColorRectTexture(0xFF060407))
                .tooltips(name, amount));
        progressBar.bar(bar -> bar
                .style(style -> style
                        .backgroundTexture(new ColorRectTexture(metal.color()))
                        .tooltips(name, amount)));
        return progressBar;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < amounts.length; i++) {
            tag.putInt("Amount" + i, amounts[i]);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < amounts.length; i++) {
            amounts[i] = Math.max(0, Math.min(TANK_CAPACITY, tag.getInt("Amount" + i)));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        for (int i = 0; i < amounts.length; i++) {
            tag.putInt("Amount" + i, amounts[i]);
        }
        return tag;
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private record MetalTank(String translationKey, int color, int defaultAmount) {
    }
}

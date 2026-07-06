package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdItems;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalHeartForgeBlockEntity extends BlockEntity implements Container, GeoBlockEntity {
    public static final int SLOT_COUNT = 18;
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_START = 2;
    public static final int OUTPUT_END = SLOT_COUNT;
    public static final int MAX_PROGRESS = 200;

    private static final int REQUIRED_SHARDS = 4;
    private static final ItemStack[] EXTRA_OUTPUTS = {
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.GOLD_INGOT),
            new ItemStack(Items.COPPER_INGOT),
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.LAPIS_LAZULI),
            new ItemStack(Items.REDSTONE),
            new ItemStack(Items.QUARTZ),
            new ItemStack(Items.AMETHYST_SHARD)
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;

    public AbyssalHeartForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), pos, blockState);
    }

    public static boolean isShard(ItemStack stack) {
        return stack.is(SpdItems.BLAZING_SHARD.get());
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbyssalHeartForgeBlockEntity forge) {
        if (!forge.hasRecipeInputs()) {
            if (forge.progress != 0) {
                forge.progress = 0;
                setChanged(level, pos, state);
            }
            return;
        }

        if (!forge.canInsertOutput(new ItemStack(SpdItems.BLAZING_CARBON_STEEL_INGOT.get()))) {
            return;
        }

        forge.progress++;
        if (forge.progress >= MAX_PROGRESS) {
            forge.progress = 0;
            forge.craftItem(level);
        }
        setChanged(level, pos, state);
    }

    public boolean hasStoredItem() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return !hasStoredItem();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
            syncInventoryToClient();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(this.items, slot);
        if (!stack.isEmpty()) {
            syncInventoryToClient();
        }
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        syncInventoryToClient();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT) {
            return isShard(stack);
        }
        if (slot == FUEL_SLOT) {
            return isFuel(stack);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
        syncInventoryToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Progress", this.progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
    }

    public Component getDisplayName() {
        return Component.translatable("container.spd.abyssal_heart_forge");
    }

    public ModularUI createUI(Player player) {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(220).height(188).paddingAll(6).gapAll(3))
                .addClass("panel_bg");

        UIElement forgeRow = new UIElement()
                .layout(layout -> layout.widthPercent(100).height(78).gapAll(10));
        forgeRow.getLayout().flexDirection(FlexDirection.ROW);

        UIElement inputColumn = new UIElement()
                .layout(layout -> layout.width(44).heightPercent(100).gapAll(4));
        inputColumn.addChildren(
                createFilteredSlot(INPUT_SLOT),
                createCraftProgressBar(),
                createFilteredSlot(FUEL_SLOT)
        );

        UIElement outputGrid = new UIElement()
                .layout(layout -> layout.width(72).height(72).gapAll(0));
        for (int row = 0; row < 4; row++) {
            UIElement outputRow = new UIElement();
            outputRow.getLayout().flexDirection(FlexDirection.ROW);
            for (int column = 0; column < 4; column++) {
                outputRow.addChild(createOutputSlot(OUTPUT_START + row * 4 + column));
            }
            outputGrid.addChild(outputRow);
        }

        forgeRow.addChildren(inputColumn, outputGrid);
        root.addChildren(
                new Label().setText(getDisplayName()),
                forgeRow,
                new Label().setText(Component.translatable("container.inventory")),
                new InventorySlots()
        );

        return new ModularUI(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)), player);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Progress", this.progress);
        return tag;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean hasProcessingIngredients() {
        return hasRecipeInputs();
    }

    private double getCraftProgress() {
        return MAX_PROGRESS <= 0 ? 0.0D : Math.min(1.0D, (double) this.progress / (double) MAX_PROGRESS);
    }

    private ProgressBar createCraftProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.label(label -> label.setDisplay(false));
        progressBar.layout(layout -> layout.width(40).height(14));
        progressBar.bind(DataBindingBuilder.floatValS2C(() -> (float) getCraftProgress()).build());
        return progressBar;
    }

    private ItemSlot createFilteredSlot(int slot) {
        return new ItemSlot(new Slot(this, slot, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (slot == INPUT_SLOT) {
                    return isShard(stack);
                }
                if (slot == FUEL_SLOT) {
                    return isFuel(stack);
                }
                return false;
            }
        });
    }

    private ItemSlot createOutputSlot(int slot) {
        return new ItemSlot(new Slot(this, slot, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    private boolean hasRecipeInputs() {
        return this.items.get(INPUT_SLOT).getCount() >= REQUIRED_SHARDS && isFuel(this.items.get(FUEL_SLOT));
    }

    private void craftItem(Level level) {
        if (!hasRecipeInputs()) {
            return;
        }

        ItemStack result = new ItemStack(SpdItems.BLAZING_CARBON_STEEL_INGOT.get());
        if (!insertOutput(result)) {
            return;
        }

        this.items.get(INPUT_SLOT).shrink(REQUIRED_SHARDS);
        this.items.get(FUEL_SLOT).shrink(1);
        if (this.items.get(INPUT_SLOT).isEmpty()) {
            this.items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
        if (this.items.get(FUEL_SLOT).isEmpty()) {
            this.items.set(FUEL_SLOT, ItemStack.EMPTY);
        }

        if (level.random.nextFloat() < 0.30F) {
            ItemStack extra = EXTRA_OUTPUTS[level.random.nextInt(EXTRA_OUTPUTS.length)].copy();
            insertOutput(extra);
        }
        if (level.random.nextFloat() < 0.01F) {
            insertOutput(new ItemStack(Items.NETHERITE_SCRAP));
        }
        syncInventoryToClient();
    }

    private boolean canInsertOutput(ItemStack stack) {
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            ItemStack existing = this.items.get(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private boolean insertOutput(ItemStack stack) {
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            ItemStack existing = this.items.get(slot);
            if (!existing.isEmpty()
                    && ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), getMaxStackSize())) {
                existing.grow(stack.getCount());
                setChanged();
                return true;
            }
        }
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            if (this.items.get(slot).isEmpty()) {
                this.items.set(slot, stack.copy());
                setChanged();
                return true;
            }
        }
        return false;
    }

    private void syncInventoryToClient() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}

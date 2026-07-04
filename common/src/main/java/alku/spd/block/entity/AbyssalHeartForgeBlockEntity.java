package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdItems;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
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

public class AbyssalHeartForgeBlockEntity extends BlockEntity implements Container, GeoBlockEntity, IUIHolder.BlockEntityUI {
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

    @Override
    public ModularUI createUI(Player entityPlayer) {
        WidgetGroup root = new WidgetGroup(0, 0, 220, 188);
        root.setBackground(new ColorRectTexture(0xFFC6C6C6));
        root.addWidget(new ImageWidget(0, 0, 220, 2, new ColorRectTexture(0xFFFFFFFF)));
        root.addWidget(new ImageWidget(0, 0, 2, 188, new ColorRectTexture(0xFFFFFFFF)));
        root.addWidget(new ImageWidget(2, 2, 216, 1, new ColorRectTexture(0xFFE0E0E0)));
        root.addWidget(new ImageWidget(2, 2, 1, 184, new ColorRectTexture(0xFFE0E0E0)));
        root.addWidget(new ImageWidget(0, 186, 220, 2, new ColorRectTexture(0xFF555555)));
        root.addWidget(new ImageWidget(218, 0, 2, 188, new ColorRectTexture(0xFF555555)));
        root.addWidget(new ImageWidget(2, 185, 216, 1, new ColorRectTexture(0xFF8B8B8B)));
        root.addWidget(new ImageWidget(217, 2, 1, 184, new ColorRectTexture(0xFF8B8B8B)));

        root.addWidget(new LabelWidget(10, 7, getDisplayName()).setTextColor(0x404040).setDropShadow(false));
        root.addWidget(new LabelWidget(29, 93, Component.translatable("container.inventory")).setTextColor(0x404040).setDropShadow(false));

        root.addWidget(new SlotWidget(this, INPUT_SLOT, 26, 34, true, true) {
            @Override
            protected Slot createSlot(Container inventory, int index) {
                return new Slot(inventory, index, 0, 0) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return isShard(stack);
                    }
                };
            }
        });
        root.addWidget(new SlotWidget(this, FUEL_SLOT, 26, 76, true, true) {
            @Override
            protected Slot createSlot(Container inventory, int index) {
                return new Slot(inventory, index, 0, 0) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return isFuel(stack);
                    }
                };
            }
        });

        int index = OUTPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                root.addWidget(new SlotWidget(this, index++, 122 + column * 18, 18 + row * 18, true, false));
            }
        }

        ProgressTexture progressTexture = new ProgressTexture(
                new ResourceTexture("ldlib:textures/gui/progress_bar_arrow.png").getSubTexture(0.0, 0.0, 1.0, 0.5),
                new ResourceTexture("ldlib:textures/gui/progress_bar_arrow.png").getSubTexture(0.0, 0.5, 1.0, 0.5)
        ).setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        root.addWidget(new ProgressWidget(this::getCraftProgress, 63, 43, 40, 18, progressTexture));

        PlayerInventoryWidget playerInventory = new PlayerInventoryWidget();
        playerInventory.setSelfPosition(24, 98);
        root.addWidget(playerInventory);

        return new ModularUI(root, this, entityPlayer);
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

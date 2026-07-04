package alku.spd.block.entity;

import alku.spd.menu.AbyssalHeartForgeMenu;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdItems;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalHeartForgeBlockEntity extends BlockEntity implements Container, GeoBlockEntity, ExtendedMenuProvider {
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
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AbyssalHeartForgeBlockEntity.this.progress;
                case 1 -> MAX_PROGRESS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                AbyssalHeartForgeBlockEntity.this.progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

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
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.spd.abyssal_heart_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AbyssalHeartForgeMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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

        if (level.random.nextFloat() < 0.10F) {
            ItemStack extra = EXTRA_OUTPUTS[level.random.nextInt(EXTRA_OUTPUTS.length)].copy();
            insertOutput(extra);
        }
        if (level.random.nextFloat() < 0.01F) {
            insertOutput(new ItemStack(Items.NETHERITE_SCRAP));
        }
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
}

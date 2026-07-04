package alku.spd.menu;

import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import alku.spd.registry.SpdMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AbyssalHeartForgeMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_START = AbyssalHeartForgeBlockEntity.SLOT_COUNT;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;

    public AbyssalHeartForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, getContainer(playerInventory, buf), new SimpleContainerData(2));
    }

    public AbyssalHeartForgeMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(SpdMenus.ABYSSAL_HEART_FORGE.get(), containerId);
        checkContainerSize(container, AbyssalHeartForgeBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);
        addForgeSlots(container);
        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    public int getProgressScaled(int width) {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        if (progress <= 0 || maxProgress <= 0) {
            return 0;
        }
        return progress * width / maxProgress;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return moved;
        }

        ItemStack stack = slot.getItem();
        moved = stack.copy();

        if (index >= AbyssalHeartForgeBlockEntity.OUTPUT_START && index < PLAYER_INVENTORY_START) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, moved);
        } else if (index == AbyssalHeartForgeBlockEntity.INPUT_SLOT || index == AbyssalHeartForgeBlockEntity.FUEL_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbyssalHeartForgeBlockEntity.isShard(stack)) {
            if (!moveItemStackTo(stack, AbyssalHeartForgeBlockEntity.INPUT_SLOT, AbyssalHeartForgeBlockEntity.INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbyssalHeartForgeBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, AbyssalHeartForgeBlockEntity.FUEL_SLOT, AbyssalHeartForgeBlockEntity.FUEL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < PLAYER_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == moved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return moved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private void addForgeSlots(Container container) {
        addSlot(new Slot(container, AbyssalHeartForgeBlockEntity.INPUT_SLOT, 26, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AbyssalHeartForgeBlockEntity.isShard(stack);
            }
        });
        addSlot(new Slot(container, AbyssalHeartForgeBlockEntity.FUEL_SLOT, 26, 76) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AbyssalHeartForgeBlockEntity.isFuel(stack);
            }
        });

        int index = AbyssalHeartForgeBlockEntity.OUTPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                addSlot(new Slot(container, index++, 122 + column * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 29 + column * 18, 104 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 29 + column * 18, 162));
        }
    }

    private static Container getContainer(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(buf.readBlockPos());
        if (blockEntity instanceof Container container) {
            return container;
        }
        return new SimpleContainer(AbyssalHeartForgeBlockEntity.SLOT_COUNT);
    }
}

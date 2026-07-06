package alku.spd.gui;

import alku.spd.mixin.AbstractContainerMenuAccessor;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.slot.LocalSlot;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.Set;

public final class Ldlib2SlotRegistrar {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ldlib2SlotRegistrar.class);

    private Ldlib2SlotRegistrar() {
    }

    public static void registerMenuSlots(ModularUIContainerMenu menu) {
        ModularUI modularUI = menu.getModularUI();
        if (modularUI == null) {
            LOGGER.warn("[SPD-FORGE-GUI] Cannot register LDLib2 slots for menu {} because its ModularUI is missing", menu.containerId);
            return;
        }

        int before = menu.slots.size();
        int added = 0;
        Set<Slot> knownSlots = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        knownSlots.addAll(menu.slots);

        for (ItemSlot itemSlot : modularUI.ui.rootElement.selfAndAllChildren()
                .filter(ItemSlot.class::isInstance)
                .map(ItemSlot.class::cast)
                .toList()) {
            Slot slot = itemSlot.getSlot();
            if (slot == null || slot instanceof LocalSlot || knownSlots.contains(slot)) {
                continue;
            }
            ((AbstractContainerMenuAccessor) menu).spd$addSlot(slot);
            knownSlots.add(slot);
            added++;
        }

        if (added > 0 || before == 0) {
            LOGGER.info("[SPD-FORGE-GUI] Registered {} LDLib2 vanilla slots for menu {} ({} -> {})",
                    added,
                    menu.containerId,
                    before,
                    menu.slots.size());
        }
    }
}

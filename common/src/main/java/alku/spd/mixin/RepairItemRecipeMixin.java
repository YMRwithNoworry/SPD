package alku.spd.mixin;

import alku.spd.item.NamelessSwordItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void spd$blockNamelessSwordCraftingRepair(CraftingContainer container, Level level, CallbackInfoReturnable<Boolean> cir) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (NamelessSwordItem.isNamelessSword(container.getItem(slot))) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}

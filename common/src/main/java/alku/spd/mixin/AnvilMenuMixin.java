package alku.spd.mixin;

import alku.spd.item.NamelessSwordItem;
import alku.spd.registry.SpdItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Shadow
    public int repairItemCountCost;

    @Shadow
    @Final
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void spd$adjustNamelessSwordRepair(CallbackInfo ci) {
        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) this;
        Container inputSlots = accessor.spd$getInputSlots();
        ResultContainer resultSlots = accessor.spd$getResultSlots();
        ItemStack base = inputSlots.getItem(0);
        ItemStack material = inputSlots.getItem(1);

        if (!NamelessSwordItem.isNamelessSword(base)) {
            return;
        }

        if (NamelessSwordItem.isNamelessSword(material)) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            this.repairItemCountCost = 0;
            this.cost.set(0);
            return;
        }

        if (!material.is(SpdItems.BLAZING_CARBON_STEEL_INGOT.get()) || !base.isDamaged()) {
            return;
        }

        ItemStack result = resultSlots.getItem(0);
        result = result.isEmpty() || !NamelessSwordItem.isNamelessSword(result) ? base.copy() : result.copy();
        int damage = base.getDamageValue();
        int repairAmount = NamelessSwordItem.getAnvilRepairAmount(base);
        int used = 0;
        while (damage > 0 && used < material.getCount()) {
            damage = Math.max(0, damage - repairAmount);
            used++;
        }

        result.setDamageValue(damage);
        resultSlots.setItem(0, result);
        this.repairItemCountCost = used;
        this.cost.set(Math.max(this.cost.get(), Math.max(1, used)));
    }
}

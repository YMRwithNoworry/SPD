package alku.spd.mixin;

import alku.spd.registry.SpdItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void spd$requireCultureMediumForLiquidGold(Player player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (itemEntity.getItem().is(SpdItems.LIQUID_GOLD.get())
                && !player.getInventory().contains(new ItemStack(SpdItems.CULTURE_MEDIUM.get()))) {
            ci.cancel();
        }
    }
}

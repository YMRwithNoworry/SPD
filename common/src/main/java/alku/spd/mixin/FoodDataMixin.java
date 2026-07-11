package alku.spd.mixin;

import alku.spd.world.SpdCorrosion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void spd$modifyAbyssalPressureNaturalHealing(Player player, float amount) {
        int layers = SpdCorrosion.getAbyssalPressureLayers(player);
        if (layers >= 7) {
            return;
        }
        player.heal(layers > 0 ? amount * 0.7F : amount);
    }
}

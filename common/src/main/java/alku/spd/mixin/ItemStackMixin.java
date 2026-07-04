package alku.spd.mixin;

import alku.spd.effect.SubjugationHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void spd$blockItemUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (SubjugationHooks.isSubjugated(player)) {
            cir.setReturnValue(InteractionResultHolder.fail((ItemStack) (Object) this));
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void spd$blockUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (SubjugationHooks.isSubjugated(context.getPlayer())) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void spd$blockUseOnEntity(Player player, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (SubjugationHooks.isSubjugated(player)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}

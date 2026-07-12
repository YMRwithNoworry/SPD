package alku.spd.fabric.mixin;

import alku.spd.client.renderer.AbyssalBlazingRuneSteleItemRenderer;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(AbyssalBlazingRuneSteleItem.class)
public abstract class AbyssalBlazingRuneSteleItemFabricMixin {
    @Inject(method = "createRenderer", at = @At("HEAD"), cancellable = true)
    private void spd$createRenderer(Consumer<Object> consumer, CallbackInfo ci) {
        consumer.accept(new RenderProvider() {
            private AbyssalBlazingRuneSteleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new AbyssalBlazingRuneSteleItemRenderer();
                }
                return renderer;
            }
        });
        ci.cancel();
    }

    @Inject(method = "getRenderProvider", at = @At("HEAD"), cancellable = true)
    private void spd$getRenderProvider(CallbackInfoReturnable<Supplier<Object>> cir) {
        cir.setReturnValue(GeoItem.makeRenderer((AbyssalBlazingRuneSteleItem) (Object) this));
    }
}

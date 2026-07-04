package alku.spd.mixin;

import alku.spd.world.AbyssalGloomWeather;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Shadow
    @Final
    private NativeImage lightPixels;

    @Shadow
    @Final
    private DynamicTexture lightTexture;

    @Inject(method = "updateLightTexture", at = @At("TAIL"))
    private void spd$dimAbyssalGloomLight(float partialTicks, CallbackInfo ci) {
        if (!AbyssalGloomWeather.isClientActive()) {
            return;
        }

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                this.lightPixels.setPixelRGBA(x, y, dim(this.lightPixels.getPixelRGBA(x, y)));
            }
        }
        this.lightTexture.upload();
    }

    private static int dim(int rgba) {
        int alpha = rgba & 0xFF000000;
        int red = (rgba >>> 16) & 0xFF;
        int green = (rgba >>> 8) & 0xFF;
        int blue = rgba & 0xFF;
        red = dimChannel(red);
        green = dimChannel(green);
        blue = dimChannel(blue);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private static int dimChannel(int value) {
        return Math.min(255, Math.max(18, Math.round(value * 0.55F)));
    }
}

package alku.spd.mixin;

import alku.spd.client.SubjugationClientOverlay;
import alku.spd.world.AbyssalGloomWeather;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
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
        float strength = 0.0F;
        if (AbyssalGloomWeather.isClientActive()) {
            strength = Math.max(strength, 0.58F);
        }

        Minecraft minecraft = Minecraft.getInstance();
        strength = Math.max(strength, SubjugationClientOverlay.getVisualProgress(minecraft, partialTicks) * 0.55F);

        if (strength <= 0.0F) {
            return;
        }

        for (int y = 0; y < 16; y++) {
            int skyOnly = this.lightPixels.getPixelRGBA(0, y);
            for (int x = 1; x < 16; x++) {
                this.lightPixels.setPixelRGBA(x, y, attenuateBlockLight(this.lightPixels.getPixelRGBA(x, y), skyOnly, strength));
            }
        }
        this.lightTexture.upload();
    }

    private static int attenuateBlockLight(int rgba, int skyOnly, float strength) {
        int alpha = rgba & 0xFF000000;
        int red = attenuateChannel((rgba >>> 16) & 0xFF, (skyOnly >>> 16) & 0xFF, strength);
        int green = attenuateChannel((rgba >>> 8) & 0xFF, (skyOnly >>> 8) & 0xFF, strength);
        int blue = attenuateChannel(rgba & 0xFF, skyOnly & 0xFF, strength);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private static int attenuateChannel(int value, int skyOnly, float strength) {
        int dimmed = Mth.lerpInt(Mth.clamp(strength, 0.0F, 1.0F), value, skyOnly);
        return Mth.clamp(dimmed, 0, 255);
    }
}

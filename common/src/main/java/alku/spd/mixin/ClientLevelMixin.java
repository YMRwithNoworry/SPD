package alku.spd.mixin;

import alku.spd.world.AbyssalGloomWeather;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "getSkyDarken", at = @At("HEAD"), cancellable = true)
    private void spd$darkenAbyssalGloomSky(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (AbyssalGloomWeather.isClientActive()) {
            cir.setReturnValue(0.28F);
        }
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void spd$tintAbyssalGloomSky(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (AbyssalGloomWeather.isClientActive()) {
            cir.setReturnValue(cir.getReturnValue().scale(0.18D).add(0.015D, 0.006D, 0.006D));
        }
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void spd$tintAbyssalGloomClouds(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (AbyssalGloomWeather.isClientActive()) {
            cir.setReturnValue(new Vec3(0.16D, 0.085D, 0.075D));
        }
    }

    @Inject(method = "getStarBrightness", at = @At("HEAD"), cancellable = true)
    private void spd$hideAbyssalGloomStars(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (AbyssalGloomWeather.isClientActive()) {
            cir.setReturnValue(0.0F);
        }
    }
}

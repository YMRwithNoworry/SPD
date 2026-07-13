package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.world.SpdBigEyes;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalFoxModel extends GeoModel<AbyssalFoxEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_fox.geo.json");
    private static final ResourceLocation BIG_EYES_MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_fox_big_eyes.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_fox.png");
    private static final ResourceLocation BIG_EYES_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_fox_big_eyes.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Spd.MOD_ID, "animations/abyssal_fox.animation.json");

    @Override
    public ResourceLocation getModelResource(AbyssalFoxEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_MODEL : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalFoxEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalFoxEntity animatable) {
        return ANIMATION;
    }
}

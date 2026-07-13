package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.world.SpdBigEyes;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalWolfModel extends GeoModel<AbyssalWolfEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_wolf.geo.json");
    private static final ResourceLocation BIG_EYES_MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_wolf_big_eyes.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_wolf.png");
    private static final ResourceLocation BIG_EYES_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_wolf_big_eyes.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Spd.MOD_ID, "animations/abyssal_wolf.animation.json");

    @Override
    public ResourceLocation getModelResource(AbyssalWolfEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_MODEL : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalWolfEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalWolfEntity animatable) {
        return ANIMATION;
    }
}

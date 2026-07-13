package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalTurtleEntity;
import alku.spd.world.SpdBigEyes;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalTurtleModel extends GeoModel<AbyssalTurtleEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_turtle.geo.json");
    private static final ResourceLocation BIG_EYES_MODEL = new ResourceLocation(Spd.MOD_ID, "geo/abyssal_turtle_big_eyes.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_turtle.png");
    private static final ResourceLocation BIG_EYES_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_turtle_big_eyes.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Spd.MOD_ID, "animations/abyssal_turtle.animation.json");

    @Override
    public ResourceLocation getModelResource(AbyssalTurtleEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_MODEL : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalTurtleEntity animatable) {
        return SpdBigEyes.isClientActive() ? BIG_EYES_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalTurtleEntity animatable) {
        return ANIMATION;
    }
}

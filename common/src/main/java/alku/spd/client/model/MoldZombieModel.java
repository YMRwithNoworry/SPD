package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.world.SpdBigEyes;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MoldZombieModel extends GeoModel<MoldZombieEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Spd.MOD_ID, "geo/mold_zombie.geo.json");
    private static final ResourceLocation BIG_EYES_MODEL = new ResourceLocation(Spd.MOD_ID, "geo/mold_zombie_big_eyes.geo.json");
    private static final ResourceLocation DROWNED_MODEL = new ResourceLocation(Spd.MOD_ID, "geo/mold_drowned.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/mold_zombie.png");
    private static final ResourceLocation POUNCER_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/mold_zombie_pouncer.png");
    private static final ResourceLocation BIG_EYES_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/mold_zombie_big_eyes.png");
    private static final ResourceLocation DROWNED_TEXTURE = new ResourceLocation(Spd.MOD_ID, "textures/entity/mold_drowned.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Spd.MOD_ID, "animations/mold_zombie.animation.json");
    private static final ResourceLocation DROWNED_ANIMATION = new ResourceLocation(Spd.MOD_ID, "animations/mold_drowned.animation.json");

    @Override
    public ResourceLocation getModelResource(MoldZombieEntity animatable) {
        if (animatable.isDrownedVariant()) {
            return DROWNED_MODEL;
        }
        return SpdBigEyes.isClientActive() ? BIG_EYES_MODEL : MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MoldZombieEntity animatable) {
        if (animatable.isDrownedVariant()) {
            return DROWNED_TEXTURE;
        }
        if (SpdBigEyes.isClientActive()) {
            return BIG_EYES_TEXTURE;
        }
        return animatable.isPouncerVariant() ? POUNCER_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MoldZombieEntity animatable) {
        return animatable.isDrownedVariant() ? DROWNED_ANIMATION : ANIMATION;
    }
}

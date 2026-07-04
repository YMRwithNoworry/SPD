package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalLizardEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalLizardModel extends GeoModel<AbyssalLizardEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalLizardEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_lizard.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalLizardEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_lizard.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalLizardEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_lizard.animation.json");
    }
}

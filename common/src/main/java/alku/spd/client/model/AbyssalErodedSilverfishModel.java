package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalErodedSilverfishModel extends GeoModel<AbyssalErodedSilverfishEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalErodedSilverfishEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_eroded_silverfish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalErodedSilverfishEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_eroded_silverfish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalErodedSilverfishEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_eroded_silverfish.animation.json");
    }
}

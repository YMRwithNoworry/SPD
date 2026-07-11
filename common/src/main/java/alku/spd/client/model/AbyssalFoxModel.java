package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalFoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalFoxModel extends GeoModel<AbyssalFoxEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalFoxEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_fox.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalFoxEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_fox.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalFoxEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_fox.animation.json");
    }
}

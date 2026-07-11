package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalWolfEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalWolfModel extends GeoModel<AbyssalWolfEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalWolfEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_wolf.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalWolfEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_wolf.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalWolfEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_wolf.animation.json");
    }
}

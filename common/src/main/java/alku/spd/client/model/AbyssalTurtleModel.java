package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.AbyssalTurtleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalTurtleModel extends GeoModel<AbyssalTurtleEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_turtle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/abyssal_turtle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_turtle.animation.json");
    }
}

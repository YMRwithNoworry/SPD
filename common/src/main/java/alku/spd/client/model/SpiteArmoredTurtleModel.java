package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.SpiteArmoredTurtleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SpiteArmoredTurtleModel extends GeoModel<SpiteArmoredTurtleEntity> {
    @Override
    public ResourceLocation getModelResource(SpiteArmoredTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/spite_armored_turtle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpiteArmoredTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/spite_armored_turtle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpiteArmoredTurtleEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/spite_armored_turtle.animation.json");
    }
}

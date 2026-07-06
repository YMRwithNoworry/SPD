package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.FalseMotherEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FalseMotherModel extends GeoModel<FalseMotherEntity> {
    @Override
    public ResourceLocation getModelResource(FalseMotherEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/mascot.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FalseMotherEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/mascot.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FalseMotherEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/mascot.animation.json");
    }
}

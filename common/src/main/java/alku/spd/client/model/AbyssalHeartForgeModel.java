package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalHeartForgeModel extends GeoModel<AbyssalHeartForgeBlockEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalHeartForgeBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_heart_forge.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalHeartForgeBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/block/abyssal_heart_forge.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalHeartForgeBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_heart_forge.animation.json");
    }
}

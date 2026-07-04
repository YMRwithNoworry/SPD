package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.block.entity.AbyssalFungalVinesBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AbyssalFungalVinesModel extends GeoModel<AbyssalFungalVinesBlockEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalFungalVinesBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_fungal_vines.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalFungalVinesBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/block/abyssal_fungal_vines.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalFungalVinesBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/abyssal_fungal_vines.animation.json");
    }
}

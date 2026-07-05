package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.block.entity.MascotBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MascotModel extends GeoModel<MascotBlockEntity> {
    @Override
    public ResourceLocation getModelResource(MascotBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/mascot.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MascotBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/entity/mascot.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MascotBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/mascot.animation.json");
    }
}

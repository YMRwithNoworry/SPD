package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.MoldZombieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MoldZombieModel extends GeoModel<MoldZombieEntity> {
    @Override
    public ResourceLocation getModelResource(MoldZombieEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/mold_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MoldZombieEntity animatable) {
        String texture = animatable.isPouncerVariant()
                ? "textures/entity/mold_zombie_pouncer.png"
                : "textures/entity/mold_zombie.png";
        return new ResourceLocation(Spd.MOD_ID, texture);
    }

    @Override
    public ResourceLocation getAnimationResource(MoldZombieEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "animations/mold_zombie.animation.json");
    }
}

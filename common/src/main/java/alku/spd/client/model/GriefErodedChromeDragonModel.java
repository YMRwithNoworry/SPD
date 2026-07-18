package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.entity.GriefErodedChromeDragonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class GriefErodedChromeDragonModel extends GeoModel<GriefErodedChromeDragonEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Spd.MOD_ID,
            "geo/grief_eroded_chrome_dragon.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spd.MOD_ID,
            "textures/entity/grief_eroded_chrome_dragon.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Spd.MOD_ID,
            "animations/grief_eroded_chrome_dragon.animation.json");

    @Override
    public ResourceLocation getModelResource(GriefErodedChromeDragonEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GriefErodedChromeDragonEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GriefErodedChromeDragonEntity animatable) {
        return ANIMATION;
    }
}

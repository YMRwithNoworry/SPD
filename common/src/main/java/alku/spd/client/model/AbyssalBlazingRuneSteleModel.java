package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.block.entity.AbyssalBlazingRuneSteleBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalBlazingRuneSteleModel extends GeoModel<AbyssalBlazingRuneSteleBlockEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_blazing_rune_stele.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/block/abyssal_blazing_rune_stele.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return null;
    }
}

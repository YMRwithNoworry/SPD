package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalBlazingRuneSteleItemModel extends GeoModel<AbyssalBlazingRuneSteleItem> {
    @Override
    public ResourceLocation getModelResource(AbyssalBlazingRuneSteleItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_blazing_rune_stele.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalBlazingRuneSteleItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/block/abyssal_blazing_rune_stele.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalBlazingRuneSteleItem animatable) {
        return null;
    }
}

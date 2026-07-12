package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.item.BlazingVeinDaggerItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BlazingVeinDaggerModel extends GeoModel<BlazingVeinDaggerItem> {
    @Override
    public ResourceLocation getModelResource(BlazingVeinDaggerItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/blazing_vein_dagger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlazingVeinDaggerItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/item/blazing_vein_dagger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlazingVeinDaggerItem animatable) {
        return null;
    }
}

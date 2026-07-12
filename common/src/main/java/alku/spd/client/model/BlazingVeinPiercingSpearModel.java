package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BlazingVeinPiercingSpearModel extends GeoModel<BlazingVeinPiercingSpearItem> {
    @Override
    public ResourceLocation getModelResource(BlazingVeinPiercingSpearItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/blazing_vein_piercing_spear.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlazingVeinPiercingSpearItem animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/item/blazing_vein_piercing_spear.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlazingVeinPiercingSpearItem animatable) {
        return null;
    }
}

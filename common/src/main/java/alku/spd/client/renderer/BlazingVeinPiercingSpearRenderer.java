package alku.spd.client.renderer;

import alku.spd.client.model.BlazingVeinPiercingSpearModel;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BlazingVeinPiercingSpearRenderer extends GeoItemRenderer<BlazingVeinPiercingSpearItem> {
    public BlazingVeinPiercingSpearRenderer() {
        super(new BlazingVeinPiercingSpearModel());
    }
}

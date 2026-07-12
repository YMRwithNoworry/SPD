package alku.spd.client.renderer;

import alku.spd.client.model.BlazingVeinDaggerModel;
import alku.spd.item.BlazingVeinDaggerItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BlazingVeinDaggerRenderer extends GeoItemRenderer<BlazingVeinDaggerItem> {
    public BlazingVeinDaggerRenderer() {
        super(new BlazingVeinDaggerModel());
    }
}

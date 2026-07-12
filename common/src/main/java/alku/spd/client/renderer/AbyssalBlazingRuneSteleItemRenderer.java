package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalBlazingRuneSteleItemModel;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class AbyssalBlazingRuneSteleItemRenderer extends GeoItemRenderer<AbyssalBlazingRuneSteleItem> {
    public AbyssalBlazingRuneSteleItemRenderer() {
        super(new AbyssalBlazingRuneSteleItemModel());
    }
}

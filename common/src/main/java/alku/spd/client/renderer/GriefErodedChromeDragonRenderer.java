package alku.spd.client.renderer;

import alku.spd.client.model.GriefErodedChromeDragonModel;
import alku.spd.entity.GriefErodedChromeDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class GriefErodedChromeDragonRenderer extends GeoEntityRenderer<GriefErodedChromeDragonEntity> {
    public GriefErodedChromeDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new GriefErodedChromeDragonModel());
        this.shadowRadius = 2.0F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}

package alku.spd.client.renderer;

import alku.spd.client.model.SpiteArmoredTurtleModel;
import alku.spd.entity.SpiteArmoredTurtleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class SpiteArmoredTurtleRenderer extends GeoEntityRenderer<SpiteArmoredTurtleEntity> {
    public SpiteArmoredTurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiteArmoredTurtleModel());
        this.shadowRadius = 0.65F;
    }
}

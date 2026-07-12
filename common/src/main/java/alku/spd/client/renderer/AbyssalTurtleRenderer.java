package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalTurtleModel;
import alku.spd.entity.AbyssalTurtleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AbyssalTurtleRenderer extends GeoEntityRenderer<AbyssalTurtleEntity> {
    public AbyssalTurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalTurtleModel());
        this.shadowRadius = 0.7F;
    }
}

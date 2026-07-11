package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalWolfModel;
import alku.spd.entity.AbyssalWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AbyssalWolfRenderer extends GeoEntityRenderer<AbyssalWolfEntity> {
    public AbyssalWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new AbyssalWolfModel());
        this.shadowRadius = 0.5F;
    }
}

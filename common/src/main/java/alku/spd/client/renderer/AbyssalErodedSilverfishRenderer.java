package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalErodedSilverfishModel;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbyssalErodedSilverfishRenderer extends GeoEntityRenderer<AbyssalErodedSilverfishEntity> {
    public AbyssalErodedSilverfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalErodedSilverfishModel());
        this.shadowRadius = 0.35F;
    }
}

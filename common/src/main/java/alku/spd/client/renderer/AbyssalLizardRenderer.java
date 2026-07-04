package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalLizardModel;
import alku.spd.entity.AbyssalLizardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbyssalLizardRenderer extends GeoEntityRenderer<AbyssalLizardEntity> {
    public AbyssalLizardRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalLizardModel());
        this.shadowRadius = 1.1F;
    }
}

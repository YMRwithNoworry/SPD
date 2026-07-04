package alku.spd.client.renderer;

import alku.spd.client.model.MoldZombieModel;
import alku.spd.entity.MoldZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MoldZombieRenderer extends GeoEntityRenderer<MoldZombieEntity> {
    public MoldZombieRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MoldZombieModel());
        this.shadowRadius = 0.5F;
    }
}

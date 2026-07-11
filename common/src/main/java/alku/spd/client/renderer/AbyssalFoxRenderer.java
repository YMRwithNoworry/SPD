package alku.spd.client.renderer;

import alku.spd.client.model.AbyssalFoxModel;
import alku.spd.entity.AbyssalFoxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbyssalFoxRenderer extends GeoEntityRenderer<AbyssalFoxEntity> {
    public AbyssalFoxRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AbyssalFoxModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    protected int getBlockLightLevel(AbyssalFoxEntity entity, BlockPos pos) {
        int normalLight = super.getBlockLightLevel(entity, pos);
        return entity.level().isNight() ? Math.max(normalLight, 6) : normalLight;
    }
}

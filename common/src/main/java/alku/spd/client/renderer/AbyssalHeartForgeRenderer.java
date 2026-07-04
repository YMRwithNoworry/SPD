package alku.spd.client.renderer;

import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import alku.spd.client.model.AbyssalHeartForgeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AbyssalHeartForgeRenderer extends GeoBlockRenderer<AbyssalHeartForgeBlockEntity> {
    public AbyssalHeartForgeRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalHeartForgeModel());
    }

    @Override
    public RenderType getRenderType(AbyssalHeartForgeBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}

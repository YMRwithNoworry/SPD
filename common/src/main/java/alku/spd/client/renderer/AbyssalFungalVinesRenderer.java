package alku.spd.client.renderer;

import alku.spd.block.entity.AbyssalFungalVinesBlockEntity;
import alku.spd.client.model.AbyssalFungalVinesModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AbyssalFungalVinesRenderer extends GeoBlockRenderer<AbyssalFungalVinesBlockEntity> {
    public AbyssalFungalVinesRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalFungalVinesModel());
    }

    @Override
    public RenderType getRenderType(AbyssalFungalVinesBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}

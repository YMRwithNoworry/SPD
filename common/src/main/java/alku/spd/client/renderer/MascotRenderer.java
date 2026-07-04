package alku.spd.client.renderer;

import alku.spd.block.entity.MascotBlockEntity;
import alku.spd.client.model.MascotModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class MascotRenderer extends GeoBlockRenderer<MascotBlockEntity> {
    private static final float MODEL_SCALE = 0.22F;

    public MascotRenderer(BlockEntityRendererProvider.Context context) {
        super(new MascotModel());
    }

    @Override
    public void preRender(PoseStack poseStack, MascotBlockEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(0.0F, 0.35F, 0.0F);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(MascotBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}

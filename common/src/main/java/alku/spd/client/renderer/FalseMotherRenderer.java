package alku.spd.client.renderer;

import alku.spd.client.model.FalseMotherModel;
import alku.spd.entity.FalseMotherEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FalseMotherRenderer extends GeoEntityRenderer<FalseMotherEntity> {
    private static final float MODEL_SCALE = 0.22F;

    public FalseMotherRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FalseMotherModel());
        this.shadowRadius = 0.9F;
    }

    @Override
    public void preRender(PoseStack poseStack, FalseMotherEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender, float partialTick,
                          int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(FalseMotherEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}

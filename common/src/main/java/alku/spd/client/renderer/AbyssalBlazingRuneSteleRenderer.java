package alku.spd.client.renderer;

import alku.spd.block.AbyssalBlazingRuneSteleBlock;
import alku.spd.block.entity.AbyssalBlazingRuneSteleBlockEntity;
import alku.spd.client.model.AbyssalBlazingRuneSteleModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class AbyssalBlazingRuneSteleRenderer extends GeoBlockRenderer<AbyssalBlazingRuneSteleBlockEntity> {
    public AbyssalBlazingRuneSteleRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalBlazingRuneSteleModel());
    }

    @Override
    public void preRender(PoseStack poseStack, AbyssalBlazingRuneSteleBlockEntity animatable,
                          BakedGeoModel model, MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        Direction facing = animatable.getBlockState().getValue(AbyssalBlazingRuneSteleBlock.FACING);
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}

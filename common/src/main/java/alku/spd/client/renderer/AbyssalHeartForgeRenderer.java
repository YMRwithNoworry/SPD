package alku.spd.client.renderer;

import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import alku.spd.client.model.AbyssalHeartForgeModel;
import alku.spd.registry.SpdItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class AbyssalHeartForgeRenderer extends GeoBlockRenderer<AbyssalHeartForgeBlockEntity> {
    private static final float[][] SHARD_OFFSETS = {
            {0.0F, 0.0F},
            {-0.16F, 0.12F},
            {0.17F, -0.11F}
    };

    public AbyssalHeartForgeRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalHeartForgeModel());
        addRenderLayer(new ProcessingShardLayer(this));
    }

    @Override
    public RenderType getRenderType(AbyssalHeartForgeBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static void renderProcessingShards(AbyssalHeartForgeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        float spin = (gameTime + partialTick) * 4.0F;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack shard = new ItemStack(SpdItems.BLAZING_SHARD.get());

        for (int i = 0; i < SHARD_OFFSETS.length; i++) {
            poseStack.pushPose();
            float bob = (float) Math.sin((gameTime + partialTick + i * 7.0F) * 0.14F) * 0.025F;
            poseStack.translate(0.5F + SHARD_OFFSETS[i][0], 0.74F + bob, 0.5F + SHARD_OFFSETS[i][1]);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin + i * 120.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(28.0F));
            poseStack.scale(0.38F, 0.38F, 0.38F);
            itemRenderer.renderStatic(shard, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, level, (int) blockEntity.getBlockPos().asLong() + i);
            poseStack.popPose();
        }
    }

    private static final class ProcessingShardLayer extends GeoRenderLayer<AbyssalHeartForgeBlockEntity> {
        private ProcessingShardLayer(GeoRenderer<AbyssalHeartForgeBlockEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, AbyssalHeartForgeBlockEntity blockEntity, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (blockEntity.hasProcessingIngredients()) {
                renderProcessingShards(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }
    }
}

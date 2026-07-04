package alku.spd.client.renderer;

import alku.spd.entity.AbyssalTornadoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class AbyssalTornadoRenderer extends EntityRenderer<AbyssalTornadoEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/particle/smoke_0.png");
    private static final int FUNNEL_RINGS = 18;
    private static final int FUNNEL_SIDES = 28;
    private static final int PUFFS = 112;

    public AbyssalTornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AbyssalTornadoEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float height = entity.getVisualHeight(partialTick);
        if (height <= 0.05F) {
            return;
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float time = entity.tickCount + partialTick;
        renderFunnel(buffer, poseStack, height, time);
        renderCloudPuffs(buffer, poseStack, height, time);
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderFunnel(VertexConsumer buffer, PoseStack poseStack, float height, float time) {
        Matrix4f matrix = poseStack.last().pose();
        for (int ring = 0; ring < FUNNEL_RINGS; ring++) {
            float t0 = ring / (float) FUNNEL_RINGS;
            float t1 = (ring + 1) / (float) FUNNEL_RINGS;
            float y0 = height * t0;
            float y1 = height * t1;
            float r0 = radiusAt(t0);
            float r1 = radiusAt(t1);
            float a0 = alphaAt(t0);
            float a1 = alphaAt(t1);
            float twist0 = time * 0.11F + ring * 0.47F;
            float twist1 = time * 0.11F + (ring + 1) * 0.47F;
            float xOffset0 = centerOffsetX(t0, time);
            float zOffset0 = centerOffsetZ(t0, time);
            float xOffset1 = centerOffsetX(t1, time);
            float zOffset1 = centerOffsetZ(t1, time);
            for (int side = 0; side < FUNNEL_SIDES; side++) {
                float p0 = side / (float) FUNNEL_SIDES;
                float p1 = (side + 1) / (float) FUNNEL_SIDES;
                addFunnelQuad(buffer, matrix, p0, p1, y0, y1, r0, r1, twist0, twist1, a0, a1, xOffset0, zOffset0, xOffset1, zOffset1);
            }
        }
    }

    private void renderCloudPuffs(VertexConsumer buffer, PoseStack poseStack, float height, float time) {
        for (int puff = 0; puff < PUFFS; puff++) {
            float seed = puff * 37.17F + 11.0F;
            float t = 0.03F + hash(seed) * 0.95F;
            float radius = radiusAt(t) * (0.62F + hash(seed + 3.1F) * 0.72F);
            float angle = hash(seed + 7.3F) * Mth.TWO_PI + time * (0.035F + (1.0F - t) * 0.055F) + t * 8.5F;
            float x = centerOffsetX(t, time) + Mth.cos(angle) * radius;
            float z = centerOffsetZ(t, time) + Mth.sin(angle) * radius;
            float y = height * t + Mth.sin(time * 0.025F + seed) * (0.18F + t * 0.35F);
            float size = (0.85F + hash(seed + 13.4F) * 1.45F) * (0.75F + t * 1.25F);
            float alpha = (0.055F + hash(seed + 21.8F) * 0.085F) * cloudAlpha(t);
            int gray = 36 + (int) (hash(seed + 31.9F) * 36.0F);
            int red = gray + 18 + (int) (t * 22.0F);
            renderBillboard(buffer, poseStack, x, y, z, size * 1.25F, size, red, gray, gray + 8, alpha);
        }
    }

    @Override
    public boolean shouldRender(AbyssalTornadoEntity livingEntity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    private static float radiusAt(float t) {
        float upperSpread = Mth.clamp((t - 0.68F) / 0.32F, 0.0F, 1.0F);
        return 0.58F + (float) Math.pow(t, 1.45F) * 4.4F + upperSpread * upperSpread * 2.8F;
    }

    private static float alphaAt(float t) {
        return (0.08F + (1.0F - t) * 0.12F) * cloudAlpha(t);
    }

    private static float cloudAlpha(float t) {
        float fadeBottom = Mth.clamp(t / 0.12F, 0.0F, 1.0F);
        float fadeTop = Mth.clamp((1.0F - t) / 0.08F, 0.0F, 1.0F);
        return fadeBottom * fadeTop;
    }

    private static float centerOffsetX(float t, float time) {
        return Mth.sin(time * 0.018F + t * 5.2F) * t * 0.62F;
    }

    private static float centerOffsetZ(float t, float time) {
        return Mth.cos(time * 0.016F + t * 4.6F) * t * 0.62F;
    }

    private static float hash(float value) {
        return Mth.frac(Mth.sin(value * 12.9898F) * 43758.547F);
    }

    private static void addFunnelQuad(VertexConsumer buffer, Matrix4f matrix, float p0, float p1, float y0, float y1,
                                      float r0, float r1, float twist0, float twist1, float a0, float a1,
                                      float xOffset0, float zOffset0, float xOffset1, float zOffset1) {
        float x00 = xOffset0 + Mth.cos(p0 * Mth.TWO_PI + twist0) * r0;
        float z00 = zOffset0 + Mth.sin(p0 * Mth.TWO_PI + twist0) * r0;
        float x01 = xOffset0 + Mth.cos(p1 * Mth.TWO_PI + twist0) * r0;
        float z01 = zOffset0 + Mth.sin(p1 * Mth.TWO_PI + twist0) * r0;
        float x10 = xOffset1 + Mth.cos(p0 * Mth.TWO_PI + twist1) * r1;
        float z10 = zOffset1 + Mth.sin(p0 * Mth.TWO_PI + twist1) * r1;
        float x11 = xOffset1 + Mth.cos(p1 * Mth.TWO_PI + twist1) * r1;
        float z11 = zOffset1 + Mth.sin(p1 * Mth.TWO_PI + twist1) * r1;
        vertex(buffer, matrix, x00, y0, z00, 0.0F, 0.0F, 86, 44, 52, a0);
        vertex(buffer, matrix, x10, y1, z10, 0.0F, 1.0F, 72, 52, 60, a1);
        vertex(buffer, matrix, x11, y1, z11, 1.0F, 1.0F, 72, 52, 60, a1);
        vertex(buffer, matrix, x01, y0, z01, 1.0F, 0.0F, 86, 44, 52, a0);
    }

    private void renderBillboard(VertexConsumer buffer, PoseStack poseStack, float x, float y, float z, float width, float height,
                                 int red, int green, int blue, float alpha) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        Matrix4f matrix = poseStack.last().pose();
        float halfWidth = width * 0.5F;
        float halfHeight = height * 0.5F;
        vertex(buffer, matrix, -halfWidth, -halfHeight, 0.0F, 0.0F, 1.0F, red, green, blue, alpha);
        vertex(buffer, matrix, halfWidth, -halfHeight, 0.0F, 1.0F, 1.0F, red, green, blue, alpha);
        vertex(buffer, matrix, halfWidth, halfHeight, 0.0F, 1.0F, 0.0F, red, green, blue, alpha);
        vertex(buffer, matrix, -halfWidth, halfHeight, 0.0F, 0.0F, 0.0F, red, green, blue, alpha);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v,
                               int red, int green, int blue, float alpha) {
        buffer.vertex(matrix, x, y, z)
                .color(red, green, blue, (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalTornadoEntity entity) {
        return TEXTURE;
    }
}

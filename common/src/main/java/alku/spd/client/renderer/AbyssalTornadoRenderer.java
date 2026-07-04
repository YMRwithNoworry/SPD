package alku.spd.client.renderer;

import alku.spd.entity.AbyssalTornadoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class AbyssalTornadoRenderer extends EntityRenderer<AbyssalTornadoEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final int RINGS = 10;
    private static final int SIDES = 18;

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
        Matrix4f matrix = poseStack.last().pose();
        float time = entity.tickCount + partialTick;
        for (int ring = 0; ring < RINGS; ring++) {
            float y0 = height * ring / RINGS;
            float y1 = height * (ring + 1) / RINGS;
            float r0 = radiusAt(ring / (float) RINGS);
            float r1 = radiusAt((ring + 1) / (float) RINGS);
            float a0 = alphaAt(ring / (float) RINGS);
            float a1 = alphaAt((ring + 1) / (float) RINGS);
            float twist0 = time * 0.16F + ring * 0.42F;
            float twist1 = time * 0.16F + (ring + 1) * 0.42F;
            for (int side = 0; side < SIDES; side++) {
                float p0 = side / (float) SIDES;
                float p1 = (side + 1) / (float) SIDES;
                addQuad(buffer, matrix, p0, p1, y0, y1, r0, r1, twist0, twist1, a0, a1);
            }
        }
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static float radiusAt(float t) {
        return 0.65F + t * 2.15F;
    }

    private static float alphaAt(float t) {
        return 0.08F + (1.0F - t) * 0.22F;
    }

    private static void addQuad(VertexConsumer buffer, Matrix4f matrix, float p0, float p1, float y0, float y1,
                                float r0, float r1, float twist0, float twist1, float a0, float a1) {
        float x00 = Mth.cos(p0 * Mth.TWO_PI + twist0) * r0;
        float z00 = Mth.sin(p0 * Mth.TWO_PI + twist0) * r0;
        float x01 = Mth.cos(p1 * Mth.TWO_PI + twist0) * r0;
        float z01 = Mth.sin(p1 * Mth.TWO_PI + twist0) * r0;
        float x10 = Mth.cos(p0 * Mth.TWO_PI + twist1) * r1;
        float z10 = Mth.sin(p0 * Mth.TWO_PI + twist1) * r1;
        float x11 = Mth.cos(p1 * Mth.TWO_PI + twist1) * r1;
        float z11 = Mth.sin(p1 * Mth.TWO_PI + twist1) * r1;
        vertex(buffer, matrix, x00, y0, z00, a0);
        vertex(buffer, matrix, x10, y1, z10, a1);
        vertex(buffer, matrix, x11, y1, z11, a1);
        vertex(buffer, matrix, x01, y0, z01, a0);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float alpha) {
        buffer.vertex(matrix, x, y, z)
                .color(150, 8, 18, (int) (alpha * 255.0F))
                .uv(0.0F, 0.0F)
                .overlayCoords(0)
                .uv2(15728880)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalTornadoEntity entity) {
        return TEXTURE;
    }
}

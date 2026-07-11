package alku.spd.client;

import alku.spd.Spd;
import alku.spd.mixin.GameRendererAccessor;
import alku.spd.mixin.PostChainAccessor;
import alku.spd.registry.SpdEffects;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Random;

public final class SubjugationClientOverlay {
    private static final ResourceLocation POST_EFFECT = new ResourceLocation(Spd.MOD_ID, "shaders/post/subjugation.json");
    private static final int FADE_IN_TICKS = 60;

    private static boolean previewEnabled;
    private static int previewStartTick;
    private static boolean subjugationActive;
    private static int subjugationStartTick;
    private static boolean postEffectFailed;

    private SubjugationClientOverlay() {
    }

    public static boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public static boolean togglePreview() {
        setPreviewEnabled(!previewEnabled);
        return previewEnabled;
    }

    public static void setPreviewEnabled(boolean enabled) {
        previewEnabled = enabled;
        if (enabled) {
            Minecraft minecraft = Minecraft.getInstance();
            previewStartTick = minecraft.player == null ? 0 : minecraft.player.tickCount;
        }
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        AbyssalPressureClient.render(graphics);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        float progress = getVisualProgress(minecraft, partialTick);
        if (progress <= 0.0F) {
            shutdownPostEffect(minecraft);
            return;
        }
        shutdownPostEffect(minecraft);
        renderOldCameraArtifacts(graphics, minecraft, width, height, progress);
    }

    public static float getVisualProgress(Minecraft minecraft, float partialTick) {
        if (minecraft.player == null) {
            return 0.0F;
        }

        MobEffectInstance effect = minecraft.player.getEffect(SpdEffects.SUBJUGATION.get());
        if (effect == null) {
            subjugationActive = false;
            return previewEnabled ? getPreviewProgress(minecraft, partialTick) : 0.0F;
        }

        if (!subjugationActive || minecraft.player.tickCount < subjugationStartTick) {
            subjugationActive = true;
            subjugationStartTick = minecraft.player.tickCount;
        }
        return getSubjugationProgress(minecraft, partialTick);
    }

    private static void renderOldCameraArtifacts(GuiGraphics graphics, Minecraft minecraft, int width, int height, float progress) {
        Random random = new Random((minecraft.player.tickCount / 2L) * 918273L);
        int specks = 4 + (int) (progress * 12.0F);
        for (int i = 0; i < specks; i++) {
            int x = random.nextInt(Math.max(1, width));
            int y = random.nextInt(Math.max(1, height));
            int size = random.nextBoolean() ? 1 : 2;
            int alpha = (int) (progress * (8 + random.nextInt(18)));
            graphics.fill(x, y, Math.min(width, x + size), Math.min(height, y + size), (alpha << 24) | 0xD8D8D8);
        }
    }

    private static float getPreviewProgress(Minecraft minecraft, float partialTick) {
        if (minecraft.player.tickCount < previewStartTick) {
            previewStartTick = minecraft.player.tickCount;
        }
        return Mth.clamp((minecraft.player.tickCount - previewStartTick + partialTick) / FADE_IN_TICKS, 0.0F, 1.0F);
    }

    private static float getSubjugationProgress(Minecraft minecraft, float partialTick) {
        return Mth.clamp((minecraft.player.tickCount - subjugationStartTick + partialTick) / FADE_IN_TICKS, 0.0F, 1.0F);
    }

    private static void updatePostEffect(Minecraft minecraft, float progress) {
        if (postEffectFailed) {
            return;
        }

        PostChain current = minecraft.gameRenderer.currentEffect();
        if (current == null || !POST_EFFECT.toString().equals(current.getName())) {
            try {
                ((GameRendererAccessor) minecraft.gameRenderer).spd$loadEffect(POST_EFFECT);
                current = minecraft.gameRenderer.currentEffect();
            } catch (RuntimeException exception) {
                postEffectFailed = true;
                return;
            }
        }

        if (current == null || !POST_EFFECT.toString().equals(current.getName())) {
            return;
        }

        for (PostPass pass : ((PostChainAccessor) current).spd$getPasses()) {
            Uniform progressUniform = pass.getEffect().getUniform("Progress");
            if (progressUniform != null) {
                progressUniform.set(progress);
            }
        }
    }

    private static void shutdownPostEffect(Minecraft minecraft) {
        PostChain current = minecraft.gameRenderer.currentEffect();
        if (current != null && POST_EFFECT.toString().equals(current.getName())) {
            minecraft.gameRenderer.shutdownEffect();
        }
    }
}

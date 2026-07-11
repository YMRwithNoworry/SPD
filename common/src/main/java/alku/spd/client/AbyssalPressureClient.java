package alku.spd.client;

import alku.spd.registry.SpdEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;

public final class AbyssalPressureClient {
    private static int nextHeavySoundTick;

    private AbyssalPressureClient() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        MobEffectInstance effect = minecraft.player.getEffect(SpdEffects.ABYSSAL_PRESSURE.get());
        if (effect == null) {
            return;
        }

        int layers = Math.min(10, effect.getAmplifier() + 1);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int edge = layers >= 7 ? Math.max(24, Math.min(width, height) / 5)
                : layers >= 4 ? 16 : 8;
        int alpha = layers >= 7 ? 80 + (layers - 7) * 12
                : layers >= 4 ? 42 + (layers - 4) * 10 : 10 + layers * 6;
        int color = (Math.min(150, alpha) << 24) | 0x2A0000;

        graphics.fill(0, 0, width, edge, color);
        graphics.fill(0, height - edge, width, height, color);
        graphics.fill(0, edge, edge, height - edge, color);
        graphics.fill(width - edge, edge, width, height - edge, color);

        if (layers >= 4) {
            renderSpores(graphics, minecraft, width, height, layers);
        }
        if (layers >= 7 && minecraft.player.tickCount >= nextHeavySoundTick) {
            minecraft.player.playSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), 0.28F, 0.55F);
            nextHeavySoundTick = minecraft.player.tickCount + 20 * 12;
        }
    }

    private static void renderSpores(GuiGraphics graphics, Minecraft minecraft, int width, int height, int layers) {
        RandomSource random = RandomSource.create(minecraft.player.tickCount / 3L * 341873128712L);
        int count = 3 + layers * 2;
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(Math.max(1, width));
            int y = random.nextInt(Math.max(1, height));
            int size = layers >= 7 && random.nextBoolean() ? 3 : 2;
            int alpha = layers >= 7 ? 90 + random.nextInt(50) : 45 + random.nextInt(35);
            graphics.fill(x, y, Math.min(width, x + size), Math.min(height, y + size),
                    (alpha << 24) | 0x080404);
        }
    }
}

package alku.spd.client;

import alku.spd.Spd;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class TitleCreditsPanel {
    private static final ResourceLocation CREDITS_TEXT = new ResourceLocation(Spd.MOD_ID, "texts/credits.txt");
    private static final ResourceLocation GDP_TEXTURE = new ResourceLocation("ldlib2", "textures/gui/gdp_styles.png");
    private static final int COLLAPSED_WIDTH = 138;
    private static final int EXPANDED_WIDTH = 206;
    private static final int COLLAPSED_HEIGHT = 30;
    private static final int EXPANDED_HEIGHT = 178;
    private static final int HEADER_HEIGHT = 30;
    private static boolean expanded;
    private static float animation;
    private static boolean gdpTouched;
    private static List<String> credits;

    private TitleCreditsPanel() {
    }

    public static void tick() {
        float target = expanded ? 1.0F : 0.0F;
        animation = Mth.clamp(animation + (target - animation) * 0.24F, 0.0F, 1.0F);
        if (Math.abs(animation - target) < 0.01F) {
            animation = target;
        }
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int mouseX, int mouseY, float partialTick) {
        touchGdpStylesheet();
        loadCredits();

        float eased = smooth(animation);
        int panelWidth = (int) Mth.lerp(eased, COLLAPSED_WIDTH, EXPANDED_WIDTH);
        int panelHeight = (int) Mth.lerp(eased, COLLAPSED_HEIGHT, Math.min(EXPANDED_HEIGHT, screenHeight - 22));
        int x = Math.max(8, screenWidth - panelWidth - 10);
        int y = 10;
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + HEADER_HEIGHT;

        drawFrame(graphics, x, y, panelWidth, panelHeight, hovered);

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        graphics.drawString(font, Component.literal("致谢名单"), x + 18, y + 10, 0xFFE8C77B, false);
        graphics.drawString(font, expanded ? Component.literal("▲") : Component.literal("▼"), x + panelWidth - 20, y + 10, 0xFFD9A94F, false);

        if (panelHeight <= HEADER_HEIGHT + 6) {
            return;
        }

        int contentTop = y + HEADER_HEIGHT + 8;
        int contentBottom = y + panelHeight - 10;
        int textX = x + 14;
        int textY = contentTop;
        int maxTextWidth = panelWidth - 28;

        graphics.enableScissor(x + 7, contentTop - 3, x + panelWidth - 7, contentBottom);
        for (String line : credits) {
            if (line.isBlank()) {
                textY += 6;
                continue;
            }
            List<FormattedCharSequence> wrapped = font.split(FormattedText.of(line), maxTextWidth);
            for (FormattedCharSequence sequence : wrapped) {
                if (textY > contentBottom) {
                    break;
                }
                graphics.drawString(font, sequence, textX, textY, 0xFFE9D8B0, false);
                textY += 12;
            }
            textY += 2;
        }
        graphics.disableScissor();
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth) {
        if (button != 0) {
            return false;
        }
        float eased = smooth(animation);
        int panelWidth = (int) Mth.lerp(eased, COLLAPSED_WIDTH, EXPANDED_WIDTH);
        int x = Math.max(8, screenWidth - panelWidth - 10);
        int y = 10;
        if (mouseX < x || mouseX > x + panelWidth || mouseY < y || mouseY > y + HEADER_HEIGHT) {
            return false;
        }
        expanded = !expanded;
        return true;
    }

    private static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        graphics.fill(x + 4, y + 5, x + width + 4, y + height + 5, 0x66000000);
        graphics.fillGradient(x, y, x + width, y + height, 0xEE1B1315, 0xEE0A0708);
        graphics.fill(x + 1, y + 1, x + width - 1, y + HEADER_HEIGHT, hovered ? 0xFF402522 : 0xFF2D1918);
        graphics.fill(x + 6, y + HEADER_HEIGHT, x + width - 6, y + HEADER_HEIGHT + 1, 0xBBD69A44);
        graphics.fill(x + 4, y + 4, x + 7, y + height - 4, 0x99C58E45);
        graphics.fill(x + width - 7, y + 4, x + width - 4, y + height - 4, 0x99C58E45);
        graphics.renderOutline(x, y, width, height, hovered ? 0xFFE8C77B : 0xFFC08B44);

        graphics.setColor(1.0F, 1.0F, 1.0F, 0.18F);
        graphics.blit(GDP_TEXTURE, x + 4, y + 4, 0, 0, 16, 16);
        graphics.blit(GDP_TEXTURE, x + width - 20, y + 4, 16, 0, 16, 16);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void loadCredits() {
        if (credits != null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        List<String> loaded = new ArrayList<>();
        try {
            Resource resource = minecraft.getResourceManager().getResource(CREDITS_TEXT).orElseThrow();
            try (BufferedReader reader = resource.openAsReader()) {
                reader.lines()
                        .map(line -> line.replace("\uFEFF", "").strip())
                        .forEach(loaded::add);
            }
        } catch (IOException | RuntimeException exception) {
            loaded.add("感谢所有支持 SPD 的玩家");
        }
        credits = loaded.isEmpty() ? List.of("感谢所有支持 SPD 的玩家") : List.copyOf(loaded);
    }

    private static void touchGdpStylesheet() {
        if (gdpTouched) {
            return;
        }
        gdpTouched = true;
        StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP);
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}

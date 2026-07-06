package alku.spd.client;

import alku.spd.Spd;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
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
    private static final int COLLAPSED_WIDTH = 138;
    private static final int EXPANDED_WIDTH = 206;
    private static final int COLLAPSED_HEIGHT = 30;
    private static final int HEADER_HEIGHT = 30;
    private static final int EDGE_MARGIN = 10;
    private static final int CONTENT_PADDING_X = 14;
    private static final long ANIMATION_DURATION_NANOS = 280_000_000L;
    private static final String STRIKETHROUGH_MARKER = "（此处用横线划掉）";
    private static final String STRIKETHROUGH_NAME = "uu_Uly";
    private static boolean expanded;
    private static float animation;
    private static float animationFrom;
    private static float animationTarget;
    private static long animationStartNanos = System.nanoTime();
    private static boolean gdpTouched;
    private static List<String> credits;

    private TitleCreditsPanel() {
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int mouseX, int mouseY, float partialTick) {
        touchGdpStylesheet();
        loadCredits();
        updateAnimation();

        float eased = smooth(animation);
        int panelWidth = (int) Mth.lerp(eased, COLLAPSED_WIDTH, EXPANDED_WIDTH);
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int expandedHeight = getExpandedHeight(font, screenHeight);
        int panelHeight = (int) Mth.lerp(eased, COLLAPSED_HEIGHT, expandedHeight);
        int x = Math.max(8, screenWidth - panelWidth - EDGE_MARGIN);
        int y = 10;
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + HEADER_HEIGHT;

        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        try {
            drawFrame(graphics, x, y, panelWidth, panelHeight, hovered);

            graphics.drawString(font, Component.literal("致谢名单"), x + 14, y + 10, 0xFFE8C77B, false);
            drawArrowButton(graphics, font, x + panelWidth - 27, y + 4, expanded, hovered);

            if (panelHeight <= HEADER_HEIGHT + 6) {
                return;
            }

            int contentTop = y + HEADER_HEIGHT + 8;
            int contentBottom = y + panelHeight - 10;
            int textX = x + 14;
            int textY = contentTop;
            int maxTextWidth = panelWidth - CONTENT_PADDING_X * 2;

            graphics.enableScissor(x + 7, contentTop - 3, x + panelWidth - 7, contentBottom);
            try {
                for (String line : credits) {
                    if (line.isBlank()) {
                        textY += 6;
                        continue;
                    }
                    List<FormattedCharSequence> wrapped = font.split(formatCreditLine(line), maxTextWidth);
                    for (FormattedCharSequence sequence : wrapped) {
                        if (textY > contentBottom) {
                            break;
                        }
                        graphics.drawString(font, sequence, textX, textY, 0xFFE9D8B0, false);
                        textY += 12;
                    }
                    textY += 2;
                }
            } finally {
                graphics.disableScissor();
            }
        } finally {
            graphics.pose().popPose();
            graphics.flush();
        }
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth) {
        if (button != 0) {
            return false;
        }
        updateAnimation();
        float eased = smooth(animation);
        int panelWidth = (int) Mth.lerp(eased, COLLAPSED_WIDTH, EXPANDED_WIDTH);
        int x = Math.max(8, screenWidth - panelWidth - EDGE_MARGIN);
        int y = 10;
        if (mouseX < x || mouseX > x + panelWidth || mouseY < y || mouseY > y + HEADER_HEIGHT) {
            return false;
        }
        expanded = !expanded;
        animationFrom = animation;
        animationTarget = expanded ? 1.0F : 0.0F;
        animationStartNanos = System.nanoTime();
        return true;
    }

    private static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        graphics.fill(x + 4, y + 5, x + width + 4, y + height + 5, 0x66000000);
        graphics.fillGradient(x, y, x + width, y + height, 0xFF1B1315, 0xFF0A0708);
        graphics.fill(x + 1, y + 1, x + width - 1, y + HEADER_HEIGHT, hovered ? 0xFF402522 : 0xFF2D1918);
        graphics.fill(x + 7, y + HEADER_HEIGHT + 1, x + width - 7, y + height - 7, 0xFF0D0A0B);
        graphics.fill(x + 6, y + HEADER_HEIGHT, x + width - 6, y + HEADER_HEIGHT + 1, 0xBBD69A44);
        graphics.fill(x + 4, y + 4, x + 7, y + height - 4, 0xFFC58E45);
        graphics.fill(x + width - 7, y + 4, x + width - 4, y + height - 4, 0xFFC58E45);
        graphics.renderOutline(x, y, width, height, hovered ? 0xFFE8C77B : 0xFFC08B44);
    }

    private static void drawArrowButton(GuiGraphics graphics, Font font, int x, int y, boolean opened, boolean hovered) {
        int fill = hovered ? 0xFF4A2B23 : 0xFF281817;
        int border = hovered ? 0xFFE8C77B : 0xFFC08B44;
        graphics.fill(x, y, x + 22, y + 22, fill);
        graphics.renderOutline(x, y, 22, 22, border);
        String arrow = opened ? "▲" : "▼";
        int textX = x + (22 - font.width(arrow)) / 2;
        graphics.drawString(font, arrow, textX, y + 7, 0xFFFFD36E, false);
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
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private static void updateAnimation() {
        if (animation == animationTarget) {
            return;
        }
        float progress = Mth.clamp((float) (System.nanoTime() - animationStartNanos) / ANIMATION_DURATION_NANOS, 0.0F, 1.0F);
        animation = Mth.lerp(smooth(progress), animationFrom, animationTarget);
        if (progress >= 1.0F) {
            animation = animationTarget;
        }
    }

    private static int getExpandedHeight(Font font, int screenHeight) {
        int contentHeight = 0;
        int maxTextWidth = EXPANDED_WIDTH - CONTENT_PADDING_X * 2;
        for (String line : credits) {
            if (line.isBlank()) {
                contentHeight += 6;
                continue;
            }
            contentHeight += font.split(formatCreditLine(line), maxTextWidth).size() * 12 + 2;
        }
        int desired = HEADER_HEIGHT + 18 + contentHeight + 10;
        int available = screenHeight - 20;
        return Mth.clamp(desired, COLLAPSED_HEIGHT, available);
    }

    private static FormattedText formatCreditLine(String line) {
        if (!line.contains(STRIKETHROUGH_MARKER)) {
            return FormattedText.of(line);
        }

        String cleaned = line.replace(STRIKETHROUGH_MARKER, "");
        int nameStart = cleaned.indexOf(STRIKETHROUGH_NAME);
        if (nameStart < 0) {
            return FormattedText.of(cleaned);
        }

        int nameEnd = nameStart + STRIKETHROUGH_NAME.length();
        return Component.literal(cleaned.substring(0, nameStart))
                .append(Component.literal(STRIKETHROUGH_NAME).withStyle(ChatFormatting.STRIKETHROUGH))
                .append(Component.literal(cleaned.substring(nameEnd)));
    }
}

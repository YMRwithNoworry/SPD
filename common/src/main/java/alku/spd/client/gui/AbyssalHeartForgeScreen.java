package alku.spd.client.gui;

import alku.spd.menu.AbyssalHeartForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AbyssalHeartForgeScreen extends AbstractContainerScreen<AbyssalHeartForgeMenu> {
    public AbyssalHeartForgeScreen(AbyssalHeartForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 188;
        this.titleLabelX = 10;
        this.titleLabelY = 7;
        this.inventoryLabelX = 29;
        this.inventoryLabelY = 93;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        drawRaisedPanel(graphics, left, top, this.imageWidth, this.imageHeight);
        drawInsetPanel(graphics, left + 8, top + 18, 200, 74);
        drawInsetPanel(graphics, left + 27, top + 102, 166, 62);
        drawInsetPanel(graphics, left + 27, top + 160, 166, 26);

        for (Slot slot : this.menu.slots) {
            drawSlot(graphics, left + slot.x - 1, top + slot.y - 1);
        }

        int arrowX = left + 63;
        int arrowY = top + 43;
        drawProgressArrow(graphics, arrowX, arrowY, this.menu.getProgressScaled(34));
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x + 2, y + 2, x + 17, y + 17, 0xFFC6C6C6);
    }

    private static void drawRaisedPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        graphics.fill(x, y, x + width, y + 2, 0xFFFFFFFF);
        graphics.fill(x, y, x + 2, y + height, 0xFFFFFFFF);
        graphics.fill(x, y + height - 2, x + width, y + height, 0xFF555555);
        graphics.fill(x + width - 2, y, x + width, y + height, 0xFF555555);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 4, 0xFFE0E0E0);
        graphics.fill(x + 2, y + 2, x + 4, y + height - 2, 0xFFE0E0E0);
        graphics.fill(x + 2, y + height - 4, x + width - 2, y + height - 2, 0xFF8B8B8B);
        graphics.fill(x + width - 4, y + 2, x + width - 2, y + height - 2, 0xFF8B8B8B);
    }

    private static void drawInsetPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + width, y + height, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC6C6C6);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF9A9A9A);
    }

    private static void drawProgressArrow(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x, y + 5, x + 28, y + 13, 0xFF373737);
        graphics.fill(x + 28, y + 2, x + 35, y + 16, 0xFF373737);
        graphics.fill(x + 35, y + 5, x + 42, y + 13, 0xFF373737);

        graphics.fill(x + 1, y + 6, x + 27, y + 12, 0xFF8B8B8B);
        graphics.fill(x + 27, y + 3, x + 34, y + 15, 0xFF8B8B8B);
        graphics.fill(x + 34, y + 6, x + 41, y + 12, 0xFF8B8B8B);

        int filled = Math.min(progress, 34);
        if (filled > 0) {
            graphics.fill(x + 2, y + 7, x + 2 + Math.min(filled, 25), y + 11, 0xFFFF6A2E);
            if (filled > 25) {
                int arrowFill = filled - 25;
                graphics.fill(x + 27, y + 4, x + 27 + Math.min(arrowFill, 7), y + 14, 0xFFFF6A2E);
                if (arrowFill > 7) {
                    graphics.fill(x + 34, y + 7, x + 34 + Math.min(arrowFill - 7, 6), y + 11, 0xFFFF6A2E);
                }
            }
        }
    }
}

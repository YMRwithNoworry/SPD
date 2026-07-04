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

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFFE7DDD1);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFFF4EEE6);
        graphics.fill(left + 10, top + 17, left + 208, top + 90, 0xFFE2D4C6);

        for (Slot slot : this.menu.slots) {
            drawSlot(graphics, left + slot.x - 1, top + slot.y - 1);
        }

        int arrowX = left + 63;
        int arrowY = top + 43;
        graphics.fill(arrowX, arrowY, arrowX + 43, arrowY + 14, 0xFF5A4740);
        graphics.fill(arrowX + 2, arrowY + 2, arrowX + 2 + this.menu.getProgressScaled(34), arrowY + 12, 0xFFE05932);
        graphics.fill(arrowX + 37, arrowY - 3, arrowX + 48, arrowY + 17, 0xFF5A4740);
        graphics.fill(arrowX + 39, arrowY + 1, arrowX + 44, arrowY + 13, 0xFFE05932);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8E786B);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFFEF8EE);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFFEADFD3);
    }
}

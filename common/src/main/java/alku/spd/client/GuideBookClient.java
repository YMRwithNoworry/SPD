package alku.spd.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class GuideBookClient {
    private static final List<FormattedText> PAGES = List.of(
            Component.translatable("guide.spd.book.page.1"),
            Component.translatable("guide.spd.book.page.2"),
            Component.translatable("guide.spd.book.page.3")
    );

    private GuideBookClient() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new BookViewScreen(new GuideBookAccess()));
    }

    private static final class GuideBookAccess implements BookViewScreen.BookAccess {
        @Override
        public int getPageCount() {
            return PAGES.size();
        }

        @Override
        public FormattedText getPageRaw(int page) {
            if (page < 0 || page >= PAGES.size()) {
                return FormattedText.EMPTY;
            }
            return PAGES.get(page);
        }
    }
}

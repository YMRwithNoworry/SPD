package alku.spd.forge.mixin;

import alku.spd.client.renderer.AbyssalBlazingRuneSteleItemRenderer;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(AbyssalBlazingRuneSteleItem.class)
public abstract class AbyssalBlazingRuneSteleItemForgeMixin {
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AbyssalBlazingRuneSteleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new AbyssalBlazingRuneSteleItemRenderer();
                }
                return renderer;
            }
        });
    }
}

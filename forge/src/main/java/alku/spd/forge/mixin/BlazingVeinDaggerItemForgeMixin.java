package alku.spd.forge.mixin;

import alku.spd.client.renderer.BlazingVeinDaggerRenderer;
import alku.spd.item.BlazingVeinDaggerItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(BlazingVeinDaggerItem.class)
public abstract class BlazingVeinDaggerItemForgeMixin {
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlazingVeinDaggerRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BlazingVeinDaggerRenderer();
                }
                return this.renderer;
            }
        });
    }
}

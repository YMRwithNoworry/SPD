package alku.spd.forge.mixin;

import alku.spd.client.renderer.BlazingVeinPiercingSpearRenderer;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(BlazingVeinPiercingSpearItem.class)
public abstract class BlazingVeinPiercingSpearItemForgeMixin {
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlazingVeinPiercingSpearRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BlazingVeinPiercingSpearRenderer();
                }
                return this.renderer;
            }
        });
    }
}

package alku.spd.client.renderer;

import alku.spd.block.entity.AbyssalBlazingRuneSteleBlockEntity;
import alku.spd.client.model.AbyssalBlazingRuneSteleModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class AbyssalBlazingRuneSteleRenderer extends GeoBlockRenderer<AbyssalBlazingRuneSteleBlockEntity> {
    public AbyssalBlazingRuneSteleRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalBlazingRuneSteleModel());
    }
}

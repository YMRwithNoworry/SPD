package alku.spd.fabric.client;

import alku.spd.client.AbyssalGloomClient;
import alku.spd.client.SubjugationClientOverlay;
import alku.spd.client.SpdClientCommands;
import alku.spd.client.gui.AbyssalHeartForgeScreen;
import alku.spd.client.renderer.AbyssalFungalVinesRenderer;
import alku.spd.client.renderer.AbyssalHeartForgeRenderer;
import alku.spd.client.renderer.AbyssalLizardRenderer;
import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdMenus;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class SpdFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardRenderer::new);
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_LIGHT_WAVE.get(), NoopRenderer::new);
        EntityRendererRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_FUNGAL_VINES.get(), AbyssalFungalVinesRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeRenderer::new);
        MenuRegistry.registerScreenFactory(SpdMenus.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeScreen::new);
        AbyssalGloomClient.register();
        SpdClientCommands.register();
        HudRenderCallback.EVENT.register(SubjugationClientOverlay::render);
    }
}

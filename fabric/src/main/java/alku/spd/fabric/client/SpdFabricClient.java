package alku.spd.fabric.client;

import alku.spd.client.AbyssalGloomClient;
import alku.spd.client.SubjugationClientOverlay;
import alku.spd.client.SpdClientCommands;
import alku.spd.client.renderer.AbyssalFungalVinesRenderer;
import alku.spd.client.renderer.AbyssalHeartForgeRenderer;
import alku.spd.client.renderer.AbyssalLizardRenderer;
import alku.spd.client.renderer.AbyssalTornadoRenderer;
import alku.spd.client.renderer.EpxRenderer;
import alku.spd.client.renderer.FalseMotherRenderer;
import alku.spd.client.renderer.MascotRenderer;
import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdMenus;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class SpdFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardRenderer::new);
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_LIGHT_WAVE.get(), NoopRenderer::new);
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_TORNADO.get(), AbyssalTornadoRenderer::new);
        EntityRendererRegistry.register(SpdEntities.NAMELESS_SLASH.get(), NoopRenderer::new);
        EntityRendererRegistry.register(SpdEntities.FALSE_MOTHER.get(), FalseMotherRenderer::new);
        EntityRendererRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
        EntityRendererRegistry.register(SpdEntities.EPX.get(), EpxRenderer::new);
        EntityRendererRegistry.register(SpdEntities.EPX_CLOUD.get(), NoopRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_FUNGAL_VINES.get(), AbyssalFungalVinesRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.MASCOT.get(), MascotRenderer::new);
        MenuRegistry.registerScreenFactory(SpdMenus.ABYSSAL_HEART_FORGE.get(), ModularUIContainerScreen::new);
        AbyssalGloomClient.register();
        SpdClientCommands.register();
        HudRenderCallback.EVENT.register(SubjugationClientOverlay::render);
    }
}

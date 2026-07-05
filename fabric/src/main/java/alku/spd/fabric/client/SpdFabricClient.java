package alku.spd.fabric.client;

import alku.spd.client.AbyssalGloomClient;
import alku.spd.client.SubjugationClientOverlay;
import alku.spd.client.SpdClientCommands;
import alku.spd.client.renderer.AbyssalFungalVinesRenderer;
import alku.spd.client.renderer.AbyssalHeartForgeRenderer;
import alku.spd.client.renderer.AbyssalLizardRenderer;
import alku.spd.client.renderer.AbyssalTornadoRenderer;
import alku.spd.client.renderer.EpxRenderer;
import alku.spd.client.renderer.MascotRenderer;
import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdEntities;
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
        EntityRendererRegistry.register(SpdEntities.ABYSSAL_TORNADO.get(), AbyssalTornadoRenderer::new);
        EntityRendererRegistry.register(SpdEntities.NAMELESS_SLASH.get(), NoopRenderer::new);
        EntityRendererRegistry.register(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
        EntityRendererRegistry.register(SpdEntities.EPX.get(), EpxRenderer::new);
        EntityRendererRegistry.register(SpdEntities.EPX_CLOUD.get(), NoopRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_FUNGAL_VINES.get(), AbyssalFungalVinesRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.MASCOT.get(), MascotRenderer::new);
        AbyssalGloomClient.register();
        SpdClientCommands.register();
        HudRenderCallback.EVENT.register(SubjugationClientOverlay::render);
    }
}

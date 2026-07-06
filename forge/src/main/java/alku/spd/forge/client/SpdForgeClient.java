package alku.spd.forge.client;

import alku.spd.Spd;
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
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;

@Mod.EventBusSubscriber(modid = Spd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpdForgeClient {
    private SpdForgeClient() {
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        SpdClientCommands.register();
        AbyssalGloomClient.register();
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_FUNGAL_VINES.get(), AbyssalFungalVinesRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.MASCOT.get(), MascotRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_LIGHT_WAVE.get(), NoopRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_TORNADO.get(), AbyssalTornadoRenderer::new);
        event.registerEntityRenderer(SpdEntities.NAMELESS_SLASH.get(), NoopRenderer::new);
        event.registerEntityRenderer(SpdEntities.FALSE_MOTHER.get(), FalseMotherRenderer::new);
        event.registerEntityRenderer(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieRenderer::new);
        event.registerEntityRenderer(SpdEntities.EPX.get(), EpxRenderer::new);
        event.registerEntityRenderer(SpdEntities.EPX_CLOUD.get(), NoopRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = Spd.MOD_ID, value = Dist.CLIENT)
    public static final class ClientForgeEvents {
        private ClientForgeEvents() {
        }

        @SubscribeEvent
        public static void renderSubjugationOverlay(RenderGuiOverlayEvent.Post event) {
            SubjugationClientOverlay.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}

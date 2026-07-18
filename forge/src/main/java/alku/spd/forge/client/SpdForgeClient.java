package alku.spd.forge.client;

import alku.spd.Spd;
import alku.spd.client.AbyssalGloomClient;
import alku.spd.client.SubjugationClientOverlay;
import alku.spd.client.SpdClientCommands;
import alku.spd.client.TitleMusicClient;
import alku.spd.client.renderer.AbyssalHeartForgeRenderer;
import alku.spd.client.renderer.AbyssalBlazingRuneSteleRenderer;
import alku.spd.client.renderer.AbyssalErodedSilverfishRenderer;
import alku.spd.client.renderer.AbyssalFoxRenderer;
import alku.spd.client.renderer.AbyssalTornadoRenderer;
import alku.spd.client.renderer.AbyssalTurtleRenderer;
import alku.spd.client.renderer.AbyssalWolfRenderer;
import alku.spd.client.renderer.EpxRenderer;
import alku.spd.client.renderer.FalseMotherRenderer;
import alku.spd.client.renderer.GriefErodedChromeDragonRenderer;
import alku.spd.client.renderer.MascotRenderer;
import alku.spd.client.renderer.MoldZombieRenderer;
import alku.spd.network.AbyssalHeartForgeNetworking;
import alku.spd.network.MoltenChromeNozzleNetworking;
import alku.spd.registry.SpdBlockEntities;
import alku.spd.registry.SpdEntities;
import alku.spd.world.SpdBigEyesNetworking;
import alku.spd.world.SpdWeatherNetworking;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Spd.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpdForgeClient {
    private SpdForgeClient() {
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        AbyssalHeartForgeNetworking.register();
        MoltenChromeNozzleNetworking.register();
        SpdBigEyesNetworking.register();
        SpdWeatherNetworking.register();
        SpdClientCommands.register();
        AbyssalGloomClient.register();
        TitleMusicClient.register();
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_HEART_FORGE.get(), AbyssalHeartForgeRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.MASCOT.get(), MascotRenderer::new);
        BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_BLAZING_RUNE_STELE.get(),
                AbyssalBlazingRuneSteleRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpdEntities.ABYSSAL_ERODED_SILVERFISH.get(), AbyssalErodedSilverfishRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_FOX.get(), AbyssalFoxRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_WOLF.get(), AbyssalWolfRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_TURTLE.get(), AbyssalTurtleRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_LIGHT_WAVE.get(), NoopRenderer::new);
        event.registerEntityRenderer(SpdEntities.ABYSSAL_TORNADO.get(), AbyssalTornadoRenderer::new);
        event.registerEntityRenderer(SpdEntities.NAMELESS_SLASH.get(), NoopRenderer::new);
        event.registerEntityRenderer(SpdEntities.FALSE_MOTHER.get(), FalseMotherRenderer::new);
        event.registerEntityRenderer(SpdEntities.GRIEF_ERODED_CHROME_DRAGON.get(), GriefErodedChromeDragonRenderer::new);
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

package alku.spd.forge;

import alku.spd.Spd;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdEntities;
import alku.spd.world.AbyssalBloodDesertSurface;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(Spd.MOD_ID)
public final class SpdForge {
    public SpdForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Spd.MOD_ID, modEventBus);
        GeckoLib.initialize();
        Spd.init();
        registerAbyssalBloodDesertBiome();
        modEventBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(this::onChunkLoad);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardEntity.createAttributes().build());
        event.put(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes().build());
    }

    private static void registerAbyssalBloodDesertBiome() {
        BiomeManager.addAdditionalOverworldBiomes(SpdBiomes.ABYSSAL_BLOOD_DESERT);
        BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(SpdBiomes.ABYSSAL_BLOOD_DESERT, 6));
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.isNewChunk() && event.getLevel() instanceof ServerLevel level) {
            AbyssalBloodDesertSurface.replaceSurface(level, event.getChunk());
        }
    }
}

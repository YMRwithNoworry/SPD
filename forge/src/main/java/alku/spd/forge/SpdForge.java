package alku.spd.forge;

import alku.spd.Spd;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.FalseMotherEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdEntities;
import alku.spd.world.AbyssalBloodDesertSurface;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

import java.util.UUID;

@Mod(Spd.MOD_ID)
public final class SpdForge {
    private static final UUID PIERCING_SPEAR_REACH_ID = UUID.fromString("c5bb4b7d-6c79-45fc-814a-23bc98bc26d9");

    public SpdForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Spd.MOD_ID, modEventBus);
        GeckoLib.initialize();
        Spd.init();
        registerAbyssalBloodDesertBiome();
        modEventBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(this::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(this::addItemAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardEntity.createAttributes().build());
        event.put(SpdEntities.ABYSSAL_ERODED_SILVERFISH.get(), AbyssalErodedSilverfishEntity.createAttributes().build());
        event.put(SpdEntities.FALSE_MOTHER.get(), FalseMotherEntity.createAttributes().build());
        event.put(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes().build());
    }

    private static void registerAbyssalBloodDesertBiome() {
        BiomeManager.addAdditionalOverworldBiomes(SpdBiomes.ABYSSAL_BLOOD_DESERT);
        BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(SpdBiomes.ABYSSAL_BLOOD_DESERT, 6));
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AbyssalBloodDesertSurface.replaceSurface(level, event.getChunk());
        }
    }

    private void addItemAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND && BlazingVeinPiercingSpearItem.isBlazingVeinPiercingSpear(event.getItemStack())) {
            event.addModifier(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                    PIERCING_SPEAR_REACH_ID,
                    "SPD blazing vein piercing spear reach",
                    BlazingVeinPiercingSpearItem.FORGE_REACH_BONUS,
                    AttributeModifier.Operation.ADDITION));
        }
    }
}

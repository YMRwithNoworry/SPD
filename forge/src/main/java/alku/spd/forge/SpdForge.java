package alku.spd.forge;

import alku.spd.Spd;
import alku.spd.entity.AbyssalErodedSilverfishEntity;
import alku.spd.entity.AbyssalFoxEntity;
import alku.spd.entity.AbyssalWolfEntity;
import alku.spd.entity.FalseMotherEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.item.BlazingVeinPiercingSpearItem;
import alku.spd.registry.SpdEntities;
import alku.spd.world.SpdTerraBlender;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

import java.util.UUID;
import net.minecraft.world.level.levelgen.Heightmap;

@Mod(Spd.MOD_ID)
public final class SpdForge {
    private static final UUID PIERCING_SPEAR_REACH_ID = UUID.fromString("c5bb4b7d-6c79-45fc-814a-23bc98bc26d9");

    public SpdForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Spd.MOD_ID, modEventBus);
        GeckoLib.initialize();
        Spd.init();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(this::addItemAttributes);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpdTerraBlender.register();
            SpawnPlacements.register(
                    SpdEntities.ABYSSAL_FOX.get(),
                    SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    AbyssalFoxEntity::checkSpawnRules);
            SpawnPlacements.register(
                    SpdEntities.ABYSSAL_WOLF.get(),
                    SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    AbyssalWolfEntity::checkSpawnRules);
        });
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SpdEntities.ABYSSAL_ERODED_SILVERFISH.get(), AbyssalErodedSilverfishEntity.createAttributes().build());
        event.put(SpdEntities.FALSE_MOTHER.get(), FalseMotherEntity.createAttributes().build());
        event.put(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes().build());
        event.put(SpdEntities.ABYSSAL_FOX.get(), AbyssalFoxEntity.createAttributes().build());
        event.put(SpdEntities.ABYSSAL_WOLF.get(), AbyssalWolfEntity.createAttributes().build());
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

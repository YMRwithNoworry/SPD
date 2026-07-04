package alku.spd.forge;

import alku.spd.Spd;
import alku.spd.entity.AbyssalLizardEntity;
import alku.spd.entity.MoldZombieEntity;
import alku.spd.registry.SpdEntities;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
        modEventBus.addListener(this::registerAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SpdEntities.ABYSSAL_LIZARD.get(), AbyssalLizardEntity.createAttributes().build());
        event.put(SpdEntities.MOLD_ZOMBIE.get(), MoldZombieEntity.createAttributes().build());
    }
}

package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.block.entity.AbyssalFungalVinesBlockEntity;
import alku.spd.block.entity.AbyssalHeartForgeBlockEntity;
import alku.spd.block.entity.MascotBlockEntity;
import alku.spd.block.entity.MoltenChromeNozzleBlockEntity;
import alku.spd.block.entity.VinePlagueNodeBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SpdBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Spd.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<AbyssalFungalVinesBlockEntity>> ABYSSAL_FUNGAL_VINES = BLOCK_ENTITIES.register("abyssal_fungal_vines", () ->
            BlockEntityType.Builder.of(AbyssalFungalVinesBlockEntity::new, SpdBlocks.ABYSSAL_FUNGAL_VINES.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<AbyssalHeartForgeBlockEntity>> ABYSSAL_HEART_FORGE = BLOCK_ENTITIES.register("abyssal_heart_forge", () ->
            BlockEntityType.Builder.of(AbyssalHeartForgeBlockEntity::new, SpdBlocks.ABYSSAL_HEART_FORGE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<MascotBlockEntity>> MASCOT = BLOCK_ENTITIES.register("mascot", () ->
            BlockEntityType.Builder.of(MascotBlockEntity::new, SpdBlocks.MASCOT.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<MoltenChromeNozzleBlockEntity>> MOLTEN_CHROME_NOZZLE = BLOCK_ENTITIES.register("molten_chrome_nozzle", () ->
            BlockEntityType.Builder.of(MoltenChromeNozzleBlockEntity::new, SpdBlocks.MOLTEN_CHROME_NOZZLE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<VinePlagueNodeBlockEntity>> VINE_PLAGUE_NODE = BLOCK_ENTITIES.register("vine_plague_node", () ->
            BlockEntityType.Builder.of(VinePlagueNodeBlockEntity::new, SpdBlocks.VINE_PLAGUE_NODE.get()).build(null));

    private SpdBlockEntities() {
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}

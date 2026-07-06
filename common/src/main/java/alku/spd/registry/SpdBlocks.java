package alku.spd.registry;

import alku.spd.Spd;
import alku.spd.block.AbyssalBloodSandBlock;
import alku.spd.block.AbyssalFungalVinesBlock;
import alku.spd.block.AbyssalHeartForgeBlock;
import alku.spd.block.MascotBlock;
import alku.spd.block.VinePlagueNodeBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SpdBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Spd.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> SACRED_STIGMA = BLOCKS.register("sacred_stigma", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                    .strength(3.0F, 3600000.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> BLAZING_VEIN_STONE = BLOCKS.register("blazing_vein_stone", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
                    .lightLevel(state -> 5)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> ABYSSAL_BLOOD_SAND = BLOCKS.register("abyssal_blood_sand", () ->
            new AbyssalBloodSandBlock(BlockBehaviour.Properties.copy(Blocks.SAND)
                    .isViewBlocking((state, level, pos) -> true)));

    public static final RegistrySupplier<Block> ABYSSAL_FUNGAL_VINES = BLOCKS.register("abyssal_fungal_vines", () ->
            new AbyssalFungalVinesBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .noCollission()
                    .noOcclusion()
                    .strength(0.2F)
                    .sound(SoundType.GRASS)));

    public static final RegistrySupplier<Block> VINE_PLAGUE_NODE = BLOCKS.register("vine_plague_node", () ->
            new VinePlagueNodeBlock(BlockBehaviour.Properties.copy(Blocks.NETHERRACK)
                    .strength(1.5F)
                    .sound(SoundType.NETHERRACK)));

    public static final RegistrySupplier<Block> WIDESPREAD_EPIDEMIC = BLOCKS.register("widespread_epidemic", () ->
            new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK)
                    .strength(1.5F)
                    .sound(SoundType.NETHERRACK)));

    public static final RegistrySupplier<Block> ABYSSAL_HEART_FORGE = BLOCKS.register("abyssal_heart_forge", () ->
            new AbyssalHeartForgeBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistrySupplier<Block> MASCOT = BLOCKS.register("mascot", () ->
            new MascotBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion()
                    .strength(1.0F)
                    .sound(SoundType.STONE)));

    private SpdBlocks() {
    }

    public static void register() {
        BLOCKS.register();
    }
}

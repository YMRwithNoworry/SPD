package alku.spd.world;

import alku.spd.registry.SpdBiomes;
import alku.spd.registry.SpdBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Set;

public final class AbyssalBloodDesertSurface {
    private static final Set<Block> REPLACEABLE_SAND_BLOCKS = Set.of(
            Blocks.SAND,
            Blocks.RED_SAND);

    private AbyssalBloodDesertSurface() {
    }

    public static void replaceSurface(ServerLevel level, ChunkAccess chunk) {
        BlockState bloodSand = SpdBlocks.ABYSSAL_BLOOD_SAND.get().defaultBlockState();
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean changed = false;

        for (int localX = 0; localX < 16; localX++) {
            int x = chunkPos.getMinBlockX() + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int z = chunkPos.getMinBlockZ() + localZ;
                for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (!REPLACEABLE_SAND_BLOCKS.contains(state.getBlock())) {
                        continue;
                    }
                    if (!level.getBiome(pos).is(SpdBiomes.ABYSSAL_BLOOD_DESERT)) {
                        continue;
                    }

                    chunk.setBlockState(pos, bloodSand, false);
                    changed = true;
                }
            }
        }

        if (changed) {
            chunk.setUnsaved(true);
        }
    }
}

package alku.spd.world;

import alku.spd.registry.SpdBlocks;
import alku.spd.registry.SpdTags;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Converts already-generated dirt in loaded infected-biome chunks without loading new chunks. */
public final class SpdGriefErodedDirtEvents {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int CHUNK_RADIUS = 2;
    private static final int CHUNKS_PER_SCAN = 1;
    private static final Map<ServerLevel, ScanState> STATES = new WeakHashMap<>();

    private SpdGriefErodedDirtEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(SpdGriefErodedDirtEvents::tickLevel);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD
                || level.players().isEmpty()
                || level.getGameTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        ScanState state = STATES.computeIfAbsent(level, ignored -> new ScanState());
        for (ServerPlayer player : level.players()) {
            state.enqueueAround(player.chunkPosition().x, player.chunkPosition().z);
        }

        for (int i = 0; i < CHUNKS_PER_SCAN; i++) {
            Long chunkKey = state.poll();
            if (chunkKey == null) {
                return;
            }

            int chunkX = (int) (chunkKey >> 32);
            int chunkZ = (int) (long) chunkKey;
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
            if (chunk == null) {
                continue;
            }

            replaceDirt(level, chunk);
            state.scanned.add(chunkKey);
        }
    }

    private static void replaceDirt(ServerLevel level, LevelChunk chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        Block replacement = SpdBlocks.GRIEF_ERODED_DIRT.get();

        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    pos.set(minX + localX, y, minZ + localZ);
                    if (level.getBlockState(pos).is(Blocks.DIRT)
                            && level.getBiome(pos).is(SpdTags.ABYSSAL_BIOMES)) {
                        level.setBlock(pos, replacement.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static final class ScanState {
        private final ArrayDeque<Long> pending = new ArrayDeque<>();
        private final Set<Long> queued = new HashSet<>();
        private final Set<Long> scanned = new HashSet<>();

        private void enqueueAround(int centerX, int centerZ) {
            for (int x = centerX - CHUNK_RADIUS; x <= centerX + CHUNK_RADIUS; x++) {
                for (int z = centerZ - CHUNK_RADIUS; z <= centerZ + CHUNK_RADIUS; z++) {
                    long key = chunkKey(x, z);
                    if (!scanned.contains(key) && queued.add(key)) {
                        pending.add(key);
                    }
                }
            }
        }

        private Long poll() {
            Long key = pending.poll();
            if (key != null) {
                queued.remove(key);
            }
            return key;
        }

        private static long chunkKey(int x, int z) {
            return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        }
    }
}

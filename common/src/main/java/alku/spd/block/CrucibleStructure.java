package alku.spd.block;

import alku.spd.block.entity.MoltenChromeNozzleBlockEntity;
import alku.spd.registry.SpdBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CrucibleStructure {
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 9;
    private static final int MIN_HEIGHT = 3;

    private CrucibleStructure() {
    }

    public static void tryUpdateAround(Level level, BlockPos changedPos) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Optional<Structure> structure = findStructure(serverLevel, changedPos);
        if (structure.isPresent()) {
            structure.get().activateNozzle(serverLevel);
            return;
        }

        deactivateInvalidNozzles(serverLevel, changedPos);
    }

    private static Optional<Structure> findStructure(ServerLevel level, BlockPos changedPos) {
        for (int size = MIN_SIZE; size <= MAX_SIZE; size++) {
            for (int height = MIN_HEIGHT; height <= MAX_SIZE; height++) {
                for (int minY = changedPos.getY() - height + 1; minY <= changedPos.getY(); minY++) {
                    for (int minX = changedPos.getX() - size + 1; minX <= changedPos.getX(); minX++) {
                        for (int minZ = changedPos.getZ() - size + 1; minZ <= changedPos.getZ(); minZ++) {
                            Structure candidate = new Structure(new BlockPos(minX, minY, minZ), size, height);
                            if (candidate.contains(changedPos) && candidate.isValid(level)) {
                                return Optional.of(candidate);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static void deactivateInvalidNozzles(ServerLevel level, BlockPos changedPos) {
        int radius = MAX_SIZE;
        BlockPos.betweenClosed(changedPos.offset(-radius, -radius, -radius), changedPos.offset(radius, radius, radius))
                .forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (isActiveNozzle(state) && findStructure(level, pos.immutable()).isEmpty()) {
                        level.setBlock(pos, state.setValue(MoltenChromeNozzleBlock.ACTIVE, false), 3);
                    }
                });
    }

    private static boolean isCrucibleShell(BlockState state) {
        return state.is(SpdBlocks.CRUCIBLE_WALL.get()) || state.is(SpdBlocks.MOLTEN_CHROME_NOZZLE.get());
    }

    private static boolean isActiveNozzle(BlockState state) {
        return state.is(SpdBlocks.MOLTEN_CHROME_NOZZLE.get())
                && state.hasProperty(MoltenChromeNozzleBlock.ACTIVE)
                && state.getValue(MoltenChromeNozzleBlock.ACTIVE);
    }

    private record NozzleCandidate(BlockPos pos, Direction facing) {
    }

    private static final class Structure {
        private final BlockPos origin;
        private final int footprintSize;
        private final int height;
        private final List<NozzleCandidate> nozzleCandidates = new ArrayList<>();
        private final List<NozzleCandidate> nozzleBlocks = new ArrayList<>();
        private final List<BlockPos> activeNozzles = new ArrayList<>();

        private Structure(BlockPos origin, int footprintSize, int height) {
            this.origin = origin;
            this.footprintSize = footprintSize;
            this.height = height;
        }

        private boolean contains(BlockPos pos) {
            return pos.getX() >= origin.getX() && pos.getX() < origin.getX() + footprintSize
                    && pos.getY() >= origin.getY() && pos.getY() < origin.getY() + height
                    && pos.getZ() >= origin.getZ() && pos.getZ() < origin.getZ() + footprintSize;
        }

        private boolean isValid(ServerLevel level) {
            nozzleCandidates.clear();
            nozzleBlocks.clear();
            activeNozzles.clear();

            for (int dx = 0; dx < footprintSize; dx++) {
                for (int dy = 0; dy < height; dy++) {
                    for (int dz = 0; dz < footprintSize; dz++) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        boolean xInside = dx > 0 && dx < footprintSize - 1;
                        boolean yInside = dy > 0 && dy < height - 1;
                        boolean zInside = dz > 0 && dz < footprintSize - 1;
                        boolean bottomFace = dy == 0 && xInside && zInside;
                        boolean xWall = yInside && (dx == 0 || dx == footprintSize - 1) && zInside;
                        boolean zWall = yInside && (dz == 0 || dz == footprintSize - 1) && xInside;
                        boolean optionalEdge = isOptionalCrucibleEdge(dx, dy, dz);
                        boolean shell = bottomFace || xWall || zWall;
                        BlockState state = level.getBlockState(pos);

                        if (shell) {
                            if (!isCrucibleShell(state)) {
                                return false;
                            }
                            Optional<Direction> nozzleFacing = getNozzleFacing(dx, dy, dz);
                            if (state.is(SpdBlocks.MOLTEN_CHROME_NOZZLE.get())) {
                                if (nozzleFacing.isEmpty()) {
                                    return false;
                                }
                                nozzleBlocks.add(new NozzleCandidate(pos.immutable(), nozzleFacing.get()));
                            }
                            if (isActiveNozzle(state)) {
                                if (nozzleFacing.isEmpty()) {
                                    return false;
                                }
                                activeNozzles.add(pos.immutable());
                            }
                            nozzleFacing.ifPresent(direction -> nozzleCandidates.add(new NozzleCandidate(pos.immutable(), direction)));
                        } else if (optionalEdge) {
                            if (!state.isAir() && !state.is(SpdBlocks.CRUCIBLE_WALL.get())) {
                                return false;
                            }
                        } else if (!state.isAir()) {
                            return false;
                        }
                    }
                }
            }

            return activeNozzles.size() <= 1 && nozzleBlocks.size() <= 1 && !nozzleCandidates.isEmpty();
        }

        private boolean isOptionalCrucibleEdge(int dx, int dy, int dz) {
            boolean xBoundary = dx == 0 || dx == footprintSize - 1;
            boolean yBoundary = dy == 0 || dy == height - 1;
            boolean zBoundary = dz == 0 || dz == footprintSize - 1;
            return (xBoundary ? 1 : 0) + (yBoundary ? 1 : 0) + (zBoundary ? 1 : 0) >= 2;
        }

        private Optional<Direction> getNozzleFacing(int dx, int dy, int dz) {
            if (dy <= 0 || dy >= height - 1) {
                return Optional.empty();
            }
            if (dx == 0 && dz > 0 && dz < footprintSize - 1) {
                return Optional.of(Direction.WEST);
            }
            if (dx == footprintSize - 1 && dz > 0 && dz < footprintSize - 1) {
                return Optional.of(Direction.EAST);
            }
            if (dz == 0 && dx > 0 && dx < footprintSize - 1) {
                return Optional.of(Direction.NORTH);
            }
            if (dz == footprintSize - 1 && dx > 0 && dx < footprintSize - 1) {
                return Optional.of(Direction.SOUTH);
            }
            return Optional.empty();
        }

        private void activateNozzle(ServerLevel level) {
            if (!activeNozzles.isEmpty()) {
                BlockPos activePos = activeNozzles.get(0);
                BlockState activeState = level.getBlockState(activePos);
                getCandidate(activePos).ifPresent(candidate -> {
                    if (activeState.getValue(MoltenChromeNozzleBlock.FACING) != candidate.facing()) {
                        level.setBlock(activePos, activeState.setValue(MoltenChromeNozzleBlock.FACING, candidate.facing()), 3);
                    }
                    initializeNozzle(level, activePos);
                });
                return;
            }

            if (!nozzleBlocks.isEmpty()) {
                NozzleCandidate nozzle = nozzleBlocks.get(0);
                BlockState state = level.getBlockState(nozzle.pos());
                level.setBlock(nozzle.pos(), state
                        .setValue(MoltenChromeNozzleBlock.FACING, nozzle.facing())
                        .setValue(MoltenChromeNozzleBlock.ACTIVE, true), 3);
                initializeNozzle(level, nozzle.pos());
                return;
            }

            List<NozzleCandidate> walls = nozzleCandidates.stream()
                    .filter(candidate -> level.getBlockState(candidate.pos()).is(SpdBlocks.CRUCIBLE_WALL.get()))
                    .toList();
            if (walls.isEmpty()) {
                return;
            }

            NozzleCandidate selected = walls.get(level.random.nextInt(walls.size()));
            level.setBlock(selected.pos(), SpdBlocks.MOLTEN_CHROME_NOZZLE.get().defaultBlockState()
                    .setValue(MoltenChromeNozzleBlock.FACING, selected.facing())
                    .setValue(MoltenChromeNozzleBlock.ACTIVE, true), 3);
            initializeNozzle(level, selected.pos());
        }

        private Optional<NozzleCandidate> getCandidate(BlockPos pos) {
            return nozzleCandidates.stream()
                    .filter(candidate -> candidate.pos().equals(pos))
                    .findFirst();
        }

        private void initializeNozzle(ServerLevel level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof MoltenChromeNozzleBlockEntity nozzle) {
                nozzle.initializeDefaultTanks();
            }
        }
    }
}

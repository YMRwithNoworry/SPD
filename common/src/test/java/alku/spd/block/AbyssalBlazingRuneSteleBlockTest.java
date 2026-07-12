package alku.spd.block;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AbyssalBlazingRuneSteleBlockTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void lowerHalfPairsWithTheBlockAbove() {
        assertEquals(new BlockPos(4, 11, 7),
                AbyssalBlazingRuneSteleBlock.otherHalfPos(new BlockPos(4, 10, 7), DoubleBlockHalf.LOWER));
    }

    @Test
    void upperHalfPairsWithTheBlockBelow() {
        assertEquals(new BlockPos(4, 9, 7),
                AbyssalBlazingRuneSteleBlock.otherHalfPos(new BlockPos(4, 10, 7), DoubleBlockHalf.UPPER));
    }

    @Test
    void collisionRotatesWithTheStele() {
        AABB north = AbyssalBlazingRuneSteleBlock.shapeFor(Direction.NORTH).bounds();
        AABB east = AbyssalBlazingRuneSteleBlock.shapeFor(Direction.EAST).bounds();

        assertEquals(0.5D, (north.minX + north.maxX) / 2.0D, 1.0E-9D);
        assertEquals(0.5D, (north.minZ + north.maxZ) / 2.0D, 1.0E-9D);
        assertEquals(0.5D, (east.minX + east.maxX) / 2.0D, 1.0E-9D);
        assertEquals(0.5D, (east.minZ + east.maxZ) / 2.0D, 1.0E-9D);
        assertEquals(15.0D / 16.0D, north.getXsize(), 1.0E-9D);
        assertEquals(10.0D / 16.0D, north.getZsize(), 1.0E-9D);
        assertEquals(10.0D / 16.0D, east.getXsize(), 1.0E-9D);
        assertEquals(15.0D / 16.0D, east.getZsize(), 1.0E-9D);
    }

    @Test
    void placementFacesBackTowardThePlayer() {
        assertEquals(Direction.NORTH, AbyssalBlazingRuneSteleBlock.facingForPlacement(Direction.SOUTH));
        assertEquals(Direction.EAST, AbyssalBlazingRuneSteleBlock.facingForPlacement(Direction.WEST));
    }
}

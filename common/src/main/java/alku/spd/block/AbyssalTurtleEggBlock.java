package alku.spd.block;

import alku.spd.entity.AbyssalTurtleEntity;
import alku.spd.entity.SpdEntityTargeting;
import alku.spd.registry.SpdEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

public final class AbyssalTurtleEggBlock extends TurtleEggBlock {
    public static final int NEST_INTRUSION_RADIUS = 4;
    public static final int ANGER_RADIUS = 12;

    public AbyssalTurtleEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isNight() || random.nextInt(3) != 0 || !TurtleEggBlock.onSand(level, pos)) return;
        int hatch = state.getValue(HATCH);
        if (hatch < AbyssalTurtleEggLogic.MAX_HATCH) {
            level.setBlock(pos, state.setValue(HATCH, AbyssalTurtleEggLogic.hatch(hatch + 1)), 2);
            return;
        }
        int eggs = state.getValue(EGGS);
        level.removeBlock(pos, false);
        for (int i = 0; i < eggs; i++) {
            AbyssalTurtleEntity turtle = SpdEntities.ABYSSAL_TURTLE.get().create(level);
            if (turtle == null) continue;
            turtle.setAge(-24000);
            turtle.setHomePos(pos);
            turtle.moveTo(pos.getX() + 0.3D + random.nextDouble() * 0.4D, pos.getY(),
                    pos.getZ() + 0.3D + random.nextDouble() * 0.4D, random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(turtle);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            alertNearby(level, pos, player, NEST_INTRUSION_RADIUS);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide && entity instanceof Player player) {
            alertNearby(level, pos, player, NEST_INTRUSION_RADIUS);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                              net.minecraft.world.item.ItemStack tool) {
        if (!level.isClientSide) alertNearby(level, pos, player, ANGER_RADIUS);
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide && explosion.getDirectSourceEntity() instanceof Player player) {
            alertNearby(level, pos, player, ANGER_RADIUS);
        }
        super.wasExploded(level, pos, explosion);
    }

    public static void alertNearby(Level level, BlockPos pos, Player player, int radius) {
        List<AbyssalTurtleEntity> turtles = level.getEntitiesOfClass(AbyssalTurtleEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(radius),
                turtle -> turtle.isAlive() && !turtle.isBaby());
        for (AbyssalTurtleEntity turtle : turtles) turtle.setTarget(player);
    }
}

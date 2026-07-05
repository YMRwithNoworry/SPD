package alku.spd.block;

import alku.spd.entity.SpdEntityTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssalBloodSandBlock extends FallingBlock {
    private static final VoxelShape SURFACE_SHAPE = box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public AbyssalBloodSandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (shouldSink(entity)) {
                return Shapes.empty();
            }
        }
        return SURFACE_SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (shouldSink(entity)) {
            entity.makeStuckInBlock(state, new Vec3(0.65D, 0.08D, 0.65D));
        }
    }

    private static boolean shouldSink(Entity entity) {
        return entity instanceof LivingEntity livingEntity && SpdEntityTargeting.isNonSpdLiving(livingEntity);
    }
}

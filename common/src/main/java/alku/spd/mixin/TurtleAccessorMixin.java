package alku.spd.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Turtle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Turtle.class)
public interface TurtleAccessorMixin {
    @Invoker("setHasEgg")
    void spd$setHasEgg(boolean hasEgg);

    @Invoker("getHomePos")
    BlockPos spd$getHomePos();
}

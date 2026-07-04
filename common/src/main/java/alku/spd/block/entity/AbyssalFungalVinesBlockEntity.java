package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import alku.spd.world.AbyssalGloomWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalFungalVinesBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation WIND_SWAY = RawAnimation.begin().thenLoop("wind_sway");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssalFungalVinesBlockEntity(BlockPos pos, BlockState blockState) {
        super(SpdBlockEntities.ABYSSAL_FUNGAL_VINES.get(), pos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            Level level = this.getLevel();
            if (level != null && AbyssalGloomWeather.isActive(level)) {
                state.setAndContinue(WIND_SWAY);
                return PlayState.CONTINUE;
            }

            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

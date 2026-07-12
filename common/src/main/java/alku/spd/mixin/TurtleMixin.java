package alku.spd.mixin;

import alku.spd.world.AbyssalTurtleInfectionCarrier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Turtle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Turtle.class)
public abstract class TurtleMixin implements AbyssalTurtleInfectionCarrier {
    @Unique
    private int spd$abyssalTurtleInfection;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void spd$saveAbyssalTurtleInfection(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("SpdAbyssalTurtleInfection", this.spd$abyssalTurtleInfection);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void spd$loadAbyssalTurtleInfection(CompoundTag tag, CallbackInfo ci) {
        this.spd$abyssalTurtleInfection = Math.max(0, tag.getInt("SpdAbyssalTurtleInfection"));
    }

    @Override
    public int spd$getAbyssalTurtleInfection() {
        return this.spd$abyssalTurtleInfection;
    }

    @Override
    public void spd$setAbyssalTurtleInfection(int progress) {
        this.spd$abyssalTurtleInfection = Math.max(0, Math.min(6000, progress));
    }
}

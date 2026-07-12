package alku.spd.world;

import alku.spd.entity.AbyssalTurtleEntity;
import alku.spd.entity.AbyssalTurtleMechanics;
import alku.spd.mixin.TurtleAccessorMixin;
import alku.spd.registry.SpdEntities;
import alku.spd.registry.SpdTags;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.entity.EntityTypeTest;

public final class AbyssalTurtleInfectionEvents {
    private AbyssalTurtleInfectionEvents() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(AbyssalTurtleInfectionEvents::tickLevel);
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) return;
        for (Turtle turtle : level.getEntities(EntityTypeTest.forClass(Turtle.class), turtle ->
                turtle.isAlive() && !(turtle instanceof AbyssalTurtleEntity))) {
            AbyssalTurtleInfectionCarrier carrier = (AbyssalTurtleInfectionCarrier) turtle;
            boolean inAbyssalBiome = level.getBiome(turtle.blockPosition()).is(SpdTags.ABYSSAL_BIOMES);
            int progress = AbyssalTurtleMechanics.infectionProgress(
                    carrier.spd$getAbyssalTurtleInfection(), inAbyssalBiome);
            carrier.spd$setAbyssalTurtleInfection(progress);
            if (progress >= 6000) convert(turtle);
        }
    }

    private static void convert(Turtle turtle) {
        Preservation preservation = Preservation.capture(turtle);
        AbyssalTurtleEntity converted = turtle.convertTo(SpdEntities.ABYSSAL_TURTLE.get(), true);
        if (converted != null) preservation.apply(converted);
    }

    private record Preservation(int age, Component customName, boolean customNameVisible, float healthRatio,
                                BlockPos home, boolean hasEgg) {
        private static Preservation capture(Turtle turtle) {
            TurtleAccessorMixin access = (TurtleAccessorMixin) (Object) turtle;
            return new Preservation(turtle.getAge(), turtle.getCustomName(), turtle.isCustomNameVisible(),
                    turtle.getMaxHealth() == 0.0F ? 1.0F : turtle.getHealth() / turtle.getMaxHealth(),
                    access.spd$getHomePos(), turtle.hasEgg());
        }

        private void apply(AbyssalTurtleEntity turtle) {
            turtle.setAge(this.age);
            turtle.setCustomName(this.customName);
            turtle.setCustomNameVisible(this.customNameVisible);
            turtle.setHealth(Math.max(1.0F, turtle.getMaxHealth() * this.healthRatio));
            turtle.setHomePos(this.home);
            ((TurtleAccessorMixin) (Object) turtle).spd$setHasEgg(this.hasEgg);
        }
    }
}

package alku.spd.world;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SpdWeatherEvents {
    private static final double WIND_STRENGTH = 0.055D;

    private SpdWeatherEvents() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> dispatcher.register(
                Commands.literal("spd")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("weather")
                                .then(Commands.literal("abyssal_gloom")
                                        .executes(command -> toggle(command.getSource().getLevel()))
                                        .then(Commands.argument("active", BoolArgumentType.bool())
                                                .executes(command -> set(command.getSource().getLevel(), BoolArgumentType.getBool(command, "active"))))))));

        TickEvent.SERVER_LEVEL_POST.register(SpdWeatherEvents::tickLevel);
        PlayerEvent.PLAYER_JOIN.register(SpdWeatherNetworking::syncPlayer);
        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> SpdWeatherNetworking.syncPlayer(player));
        LifecycleEvent.SERVER_STOPPING.register(server -> AbyssalGloomWeather.clear());
    }

    private static int toggle(ServerLevel level) {
        boolean active = !AbyssalGloomWeather.isActive(level);
        AbyssalGloomWeather.setActive(level, active);
        level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(AbyssalGloomWeather.NAME + "天气已" + (active ? "开启" : "关闭") + "。"), false);
        return active ? 1 : 0;
    }

    private static int set(ServerLevel level, boolean active) {
        AbyssalGloomWeather.setActive(level, active);
        level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(AbyssalGloomWeather.NAME + "天气已" + (active ? "开启" : "关闭") + "。"), false);
        return active ? 1 : 0;
    }

    private static void tickLevel(ServerLevel level) {
        AbyssalGloomWeather.State state = AbyssalGloomWeather.getOrCreate(level);
        boolean wasActive = state.active();
        state.tick(level);
        if (!state.active()) {
            if (wasActive) {
                for (ServerPlayer player : level.players()) {
                    player.removeEffect(MobEffects.DARKNESS);
                }
            }
            return;
        }

        Vec3 wind = new Vec3(state.windX() * WIND_STRENGTH, 0.0D, state.windZ() * WIND_STRENGTH);
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living && living.onGround() && level.canSeeSky(living.blockPosition())) {
                Vec3 motion = living.getDeltaMovement();
                living.setDeltaMovement(motion.x * 0.82D + wind.x, motion.y, motion.z * 0.82D + wind.z);
                living.hurtMarked = true;
            }
        }

        for (ServerPlayer player : level.players()) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, true));
            if (isExposedToAbyssalDust(player)) {
                if (state.ticks() % 40 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, true, true, true));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1, true, true, true));
                }
            }
        }
    }

    private static boolean isExposedToAbyssalDust(ServerPlayer player) {
        if (!player.level().canSeeSky(player.blockPosition())) {
            return false;
        }
        return !player.isUnderWater() && !player.isInWater() && !player.isInLava();
    }
}

package alku.spd.world;

import alku.spd.entity.AbyssalTornadoEntity;
import alku.spd.registry.SpdEntities;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpdWeatherEvents {
    private static final double WIND_STRENGTH = 0.055D;
    private static final int TORNADO_CHECK_INTERVAL = 20 * 8;
    private static final int MAX_TORNADOES_PER_LEVEL = 3;

    private SpdWeatherEvents() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> dispatcher.register(
                Commands.literal("spd")
                        .then(Commands.literal("weather")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("abyssal_gloom")
                                        .executes(command -> toggle(command.getSource().getLevel()))
                                        .then(Commands.argument("active", BoolArgumentType.bool())
                                                .executes(command -> set(command.getSource().getLevel(), BoolArgumentType.getBool(command, "active"))))
                                        .then(Commands.literal("tornado")
                                                .then(Commands.argument("duration_seconds", IntegerArgumentType.integer(1, 600))
                                                        .executes(command -> spawnTornadoAtPlayer(command.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(command, "duration_seconds")))))))));

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

    private static int spawnTornadoAtPlayer(ServerPlayer player, int durationSeconds) {
        ServerLevel level = player.serverLevel();
        AbyssalGloomWeather.State state = AbyssalGloomWeather.getOrCreate(level);
        Vec3 wind = new Vec3(state.windX(), 0.0D, state.windZ());
        AbyssalTornadoEntity tornado = new AbyssalTornadoEntity(level, player.getX(), player.getY(), player.getZ(), wind, durationSeconds * 20);
        level.addFreshEntity(tornado);
        player.displayClientMessage(Component.literal("已生成渊默龙卷风，持续 " + durationSeconds + " 秒。"), false);
        return 1;
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
            if (isExposedToAbyssalDust(player)) {
                if (state.ticks() % 40 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, true, true, true));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1, true, true, true));
                }
            }
        }

        if (state.ticks() % TORNADO_CHECK_INTERVAL == 0) {
            trySpawnTornado(level, state);
        }
    }

    private static void trySpawnTornado(ServerLevel level, AbyssalGloomWeather.State state) {
        if (level.dimension() != Level.OVERWORLD || level.players().isEmpty()) {
            return;
        }
        ServerPlayer anchor = level.players().get(level.random.nextInt(level.players().size()));
        AABB nearbyLoadedArea = anchor.getBoundingBox().inflate(160.0D, 96.0D, 160.0D);
        if (level.getEntities(SpdEntities.ABYSSAL_TORNADO.get(), nearbyLoadedArea, Entity::isAlive).size() >= MAX_TORNADOES_PER_LEVEL) {
            return;
        }
        if (level.random.nextFloat() > 0.32F) {
            return;
        }

        double distance = Mth.nextDouble(level.random, 36.0D, 88.0D);
        double angle = level.random.nextDouble() * Mth.TWO_PI;
        int x = Mth.floor(anchor.getX() + Mth.cos((float) angle) * distance);
        int z = Mth.floor(anchor.getZ() + Mth.sin((float) angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (!level.hasChunkAt(new net.minecraft.core.BlockPos(x, y, z))) {
            return;
        }
        net.minecraft.core.BlockPos ground = new net.minecraft.core.BlockPos(x, y - 1, z);
        net.minecraft.core.BlockPos spawnPos = ground.above();
        if (!level.canSeeSky(spawnPos)
                || level.getBlockState(ground).isAir()
                || level.getFluidState(spawnPos).is(FluidTags.WATER)
                || level.getFluidState(spawnPos).is(FluidTags.LAVA)) {
            return;
        }

        Vec3 wind = new Vec3(state.windX(), 0.0D, state.windZ());
        AbyssalTornadoEntity tornado = new AbyssalTornadoEntity(level, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, wind);
        level.addFreshEntity(tornado);
    }

    private static boolean isExposedToAbyssalDust(ServerPlayer player) {
        if (!player.level().canSeeSky(player.blockPosition())) {
            return false;
        }
        return !player.isUnderWater() && !player.isInWater() && !player.isInLava();
    }
}

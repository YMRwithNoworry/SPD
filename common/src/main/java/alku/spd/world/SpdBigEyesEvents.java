package alku.spd.world;

import com.mojang.brigadier.arguments.BoolArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SpdBigEyesEvents {
    private SpdBigEyesEvents() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("spd")
                    .then(Commands.literal("big_eyes")
                            .executes(command -> show(command.getSource()))
                            .then(Commands.literal("status")
                                    .executes(command -> show(command.getSource())))
                            .then(Commands.literal("on")
                                    .requires(source -> source.hasPermission(2))
                                    .executes(command -> set(command.getSource(), true)))
                            .then(Commands.literal("off")
                                    .requires(source -> source.hasPermission(2))
                                    .executes(command -> set(command.getSource(), false)))
                            .then(Commands.literal("toggle")
                                    .requires(source -> source.hasPermission(2))
                                    .executes(command -> toggle(command.getSource())))
                            .then(Commands.argument("active", BoolArgumentType.bool())
                                    .requires(source -> source.hasPermission(2))
                                    .executes(command -> set(command.getSource(),
                                            BoolArgumentType.getBool(command, "active"))))));
        });

        PlayerEvent.PLAYER_JOIN.register(SpdBigEyesNetworking::syncPlayer);
        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> SpdBigEyesNetworking.syncPlayer(player));
    }

    private static int show(CommandSourceStack source) {
        boolean active = SpdBigEyes.isActive(source.getServer());
        source.sendSuccess(() -> Component.translatable(active
                ? "commands.spd.big_eyes.status.on"
                : "commands.spd.big_eyes.status.off"), false);
        return active ? 1 : 0;
    }

    private static int toggle(CommandSourceStack source) {
        return set(source, !SpdBigEyes.isActive(source.getServer()));
    }

    private static int set(CommandSourceStack source, boolean active) {
        SpdBigEyes.setActive(source.getServer(), active);
        SpdBigEyesNetworking.syncAll(source.getServer());
        source.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(active
                ? "commands.spd.big_eyes.set.on"
                : "commands.spd.big_eyes.set.off"), false);
        return 1;
    }
}

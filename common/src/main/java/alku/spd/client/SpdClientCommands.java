package alku.spd.client;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.network.chat.Component;
import alku.spd.world.AbyssalGloomWeather;

public final class SpdClientCommands {
    private static boolean registered;

    private SpdClientCommands() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) -> dispatcher.register(
                ClientCommandRegistrationEvent.literal("spd")
                        .then(ClientCommandRegistrationEvent.literal("subjugation_preview")
                                .executes(command -> toggle(command.getSource()))
                                .then(ClientCommandRegistrationEvent.literal("on")
                                        .executes(command -> set(command.getSource(), true)))
                                .then(ClientCommandRegistrationEvent.literal("off")
                                        .executes(command -> set(command.getSource(), false)))
                                .then(ClientCommandRegistrationEvent.literal("toggle")
                                        .executes(command -> toggle(command.getSource())))
                                .then(ClientCommandRegistrationEvent.literal("status")
                                        .executes(command -> status(command.getSource()))))
                        .then(ClientCommandRegistrationEvent.literal("abyssal_gloom_status")
                                .executes(command -> abyssalGloomStatus(command.getSource())))));
    }

    private static int toggle(ClientCommandSourceStack source) {
        return sendState(source, SubjugationClientOverlay.togglePreview());
    }

    private static int set(ClientCommandSourceStack source, boolean enabled) {
        SubjugationClientOverlay.setPreviewEnabled(enabled);
        return sendState(source, enabled);
    }

    private static int status(ClientCommandSourceStack source) {
        boolean enabled = SubjugationClientOverlay.isPreviewEnabled();
        source.arch$sendSuccess(() -> Component.literal("臣缚视觉预览当前为" + (enabled ? "开启" : "关闭") + "。"), false);
        return enabled ? 1 : 0;
    }

    private static int sendState(ClientCommandSourceStack source, boolean enabled) {
        source.arch$sendSuccess(() -> Component.literal("臣缚视觉预览已" + (enabled ? "开启" : "关闭") + "。"), false);
        return enabled ? 1 : 0;
    }

    private static int abyssalGloomStatus(ClientCommandSourceStack source) {
        boolean active = AbyssalGloomWeather.isClientActive();
        source.arch$sendSuccess(() -> Component.literal("渊默天气当前为" + (active ? "开启" : "关闭") + "。"), false);
        return active ? 1 : 0;
    }
}

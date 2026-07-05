package alku.spd.world;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public final class SpdDifficultyEvents {
    private SpdDifficultyEvents() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("spd")
                    .then(Commands.literal("difficulty")
                            .executes(command -> show(command.getSource()))
                            .then(Commands.literal("get")
                                    .executes(command -> show(command.getSource())))
                            .then(Commands.literal("set")
                                    .requires(source -> source.hasPermission(2))
                                    .then(Commands.argument("difficulty", StringArgumentType.word())
                                            .suggests(SpdDifficultyEvents::suggestDifficulties)
                                            .executes(command -> set(command.getSource(), StringArgumentType.getString(command, "difficulty")))))
                            .then(Commands.argument("difficulty", StringArgumentType.word())
                                    .requires(source -> source.hasPermission(2))
                                    .suggests(SpdDifficultyEvents::suggestDifficulties)
                                    .executes(command -> set(command.getSource(), StringArgumentType.getString(command, "difficulty"))))));

            dispatcher.register(Commands.literal("dif")
                    .then(Commands.literal("world")
                            .executes(command -> show(command.getSource()))
                            .then(Commands.argument("difficulty", StringArgumentType.word())
                                    .requires(source -> source.hasPermission(2))
                                    .suggests(SpdDifficultyEvents::suggestDifficulties)
                                    .executes(command -> set(command.getSource(), StringArgumentType.getString(command, "difficulty"))))));
        });
    }

    private static int show(CommandSourceStack source) {
        SpdDifficulty.Difficulty difficulty = SpdDifficulty.get(source.getServer());
        source.sendSuccess(() -> Component.literal("当前 SPD 难度为：" + difficulty.serializedName() + "。"), false);
        return difficulty.ordinal() + 1;
    }

    private static int set(CommandSourceStack source, String name) {
        SpdDifficulty.Difficulty difficulty = SpdDifficulty.Difficulty.byName(name).orElse(null);
        if (difficulty == null) {
            source.sendFailure(Component.literal("未知 SPD 难度：" + name + "。可用难度：" + SpdDifficulty.availableNames()));
            return 0;
        }

        SpdDifficulty.set(source.getServer(), difficulty);
        source.getServer().getPlayerList().broadcastSystemMessage(Component.literal("SPD 难度已切换为：" + difficulty.serializedName() + "。"), false);
        return difficulty.ordinal() + 1;
    }

    private static CompletableFuture<Suggestions> suggestDifficulties(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (SpdDifficulty.Difficulty difficulty : SpdDifficulty.Difficulty.values()) {
            builder.suggest(difficulty.serializedName());
        }
        return builder.buildFuture();
    }
}

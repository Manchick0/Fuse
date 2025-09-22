package com.manchickas.fuse;

import com.manchickas.fuse.script.ScriptLoader;
import com.manchickas.optionated.option.None;
import com.manchickas.optionated.option.Some;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.time.LocalDate;
import java.time.Month;

public final class Fuse implements ModInitializer {

    private static final Version VERSION = new Version(1, 0, 0);
    private static final Text PREFIX = Text.literal("🔥 ")
            .setStyle(Style.EMPTY.withColor(0xe9b843));
    private static final Style OUTDATED_MAJOR = Style.EMPTY.withColor(Formatting.RED);
    private static final Style OUTDATED_MINOR = Style.EMPTY.withColor(Formatting.GOLD);
    private static final Style OUTDATED_PATCH = Style.EMPTY.withColor(Formatting.YELLOW);
    private static final Style UP_TO_DATE = Style.EMPTY.withColor(Formatting.GREEN);
    private static final Style UNRELEASED = Style.EMPTY.withColor(0x5098ef);

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onInitialize() {
        ResourceManagerHelperImpl.get(ResourceType.SERVER_DATA)
                .registerReloadListener(ScriptLoader.getInstance());
        ServerLifecycleEvents.SERVER_STARTING.register(ScriptLoader.getInstance()::updateCommandTree);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            dispatcher.register(ScriptLoader.getInstance()
                    .buildFuseCommand());
            dispatcher.register(CommandManager.literal("icanhasfuse")
                    .executes(ctx -> {
                        Version.fetchLatest().thenAccept(result -> {
                            var src = ctx.getSource();
                            var style = result instanceof Some<Version>(var latest)
                                    ? VERSION.compareTo(latest) >= 1
                                    ? UNRELEASED
                                    : latest.major() > VERSION.major()
                                    ? OUTDATED_MAJOR
                                    : latest.minor() > VERSION.minor()
                                    ? OUTDATED_MINOR
                                    : latest.patch() > VERSION.patch()
                                    ? OUTDATED_PATCH
                                    : UP_TO_DATE
                                    : Style.EMPTY.withColor(Formatting.WHITE);
                            src.sendMessage(PREFIX.copy().append(Text.literal("You are running version ").formatted(Formatting.GRAY)
                                    .append(Text.literal(VERSION.toString()).setStyle(style))
                                    .append(Text.literal(" of the ever-burning flame.").formatted(Formatting.GRAY))));
                        });
                        return 1;
                    }));
            var date = LocalDate.now();
            if (date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1)
                dispatcher.register(CommandManager.literal("icanhasbukkit")
                        .executes((ctx) -> {
                            var src = ctx.getSource();
                            src.sendMessage(Text.literal("No, you cannot.").formatted(Formatting.RED));
                            return 1;
                        }));
        });
    }

    public static Identifier withPath(String path) {
        return Identifier.of("fuse", path);
    }
}

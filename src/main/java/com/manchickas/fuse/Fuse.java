package com.manchickas.fuse;

import com.manchickas.fuse.script.ScriptLoader;
import com.manchickas.source.span.LineSpan;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.net.URI;

public final class Fuse implements ModInitializer {

    private static final URI HOMEPAGE = URI.create("https://fusemc.dev");

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
                        var src = ctx.getSource();
                        src.sendMessage(Text.literal("This server is running ")
                                .append(Text.literal("Fuse " + Fuse.VERSION).setStyle(Style.EMPTY.withUnderline(true)
                                        .withClickEvent(new ClickEvent.OpenUrl(Fuse.HOMEPAGE))))
                                .append(Text.literal(".")));
                        return 1;
                    }));
        });
    }

    public String fetchLatestVersion() {

    }

    public static Identifier withPath(String path) {
        return Identifier.of("fuse", path);
    }
}

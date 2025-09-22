package com.manchickas.fuse.event;

import com.manchickas.fuse.Fuse;
import com.manchickas.fuse.event.type.JoinEvent;
import com.manchickas.fuse.type.FunctionalType;
import com.manchickas.fuse.type.RegistryType;
import com.manchickas.zet.type.Type;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public record EventType<L extends EventListener>(Type<L, L> type, Class<L> clazz) {

    private static final RegistryKey<Registry<EventType<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Fuse.withPath("event_type"));
    public static final Registry<EventType<?>> REGISTRY = new SimpleRegistry<>(EventType.REGISTRY_KEY, Lifecycle.stable());
    public static final Type<EventType<?>, EventType<?>> TYPE = new RegistryType<>(EventType.REGISTRY);

    public static EventType<JoinEvent.Listener> JOIN = EventType.register(Identifier.ofVanilla("join"),
            JoinEvent.Listener.class, "(player: Player) => void");

    private static <L extends EventListener> EventType<L> register(Identifier identifier,
                                                                   Class<L> clazz,
                                                                   String signature) {
        return Registry.register(
                EventType.REGISTRY,
                identifier,
                new EventType<>(new FunctionalType<>(clazz, signature), clazz)
        );
    }
}

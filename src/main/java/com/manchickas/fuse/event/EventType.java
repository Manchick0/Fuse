package com.manchickas.fuse.event;

import com.manchickas.fuse.Fuse;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public record EventType<L extends EventListener>(Class<L> clazz) {

    private static final RegistryKey<Registry<EventType<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Fuse.withPath("event_type"));
    public static final Registry<EventType<?>> REGISTRY = new SimpleRegistry<>(EventType.REGISTRY_KEY, Lifecycle.stable());

    private static <L extends EventListener> EventType<L> register(String path, Class<L> clazz) {
        return Registry.register(EventType.REGISTRY, Fuse.withPath(path), new EventType<>(clazz));
    }
}

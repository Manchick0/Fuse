package com.manchickas.fuse;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public final class Fuse implements ModInitializer {

    @Override
    public void onInitialize() {
    }

    public static Identifier withPath(String path) {
        return Identifier.of("fuse", path);
    }
}

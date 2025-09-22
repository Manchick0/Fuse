package com.manchickas.fuse.std.server;

import com.manchickas.fuse.std.ScriptWrapper;
import net.minecraft.server.MinecraftServer;

public final class ScriptServer extends ScriptWrapper<MinecraftServer> {

    public ScriptServer(MinecraftServer server) {
        super(server);
    }
}

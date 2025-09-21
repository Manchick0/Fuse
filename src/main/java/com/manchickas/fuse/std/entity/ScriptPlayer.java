package com.manchickas.fuse.std.entity;

import com.manchickas.fuse.type.codec.ForwardingType;
import com.manchickas.zet.Zet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextCodecs;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public final class ScriptPlayer extends ScriptLivingEntity<ServerPlayerEntity> {

    private ScriptPlayer(ServerPlayerEntity wrapped) {
        super(wrapped);
    }

    public static ScriptPlayer wrap(ServerPlayerEntity player) {
        return new ScriptPlayer(player);
    }

    @HostAccess.Export
    public void sendMessage(Value message) {
        var type = new ForwardingType<>(TextCodecs.CODEC);
        this.wrapped.sendMessage(Zet.expect(type, message));
    }

    @HostAccess.Export
    public String name() {
        return this.wrapped.getGameProfile()
                .getName();
    }
}

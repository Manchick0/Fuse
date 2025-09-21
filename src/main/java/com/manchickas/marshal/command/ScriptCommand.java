package com.manchickas.marshal.command;

import com.manchickas.fuse.type.FunctionalType;
import com.manchickas.marshal.segment.ParameterSegment;
import com.manchickas.zet.type.Type;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.graalvm.polyglot.HostAccess;

public final class ScriptCommand implements Command<ServerCommandSource> {

    private final ParameterSegment[] parameters;
    private final Callback callback;

    public ScriptCommand(ParameterSegment[] parameters, Callback callback) {
        this.parameters = parameters;
        this.callback = callback;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        var result = this.callback.execute(ScriptCommandContext.wrap(context, this.parameters));
        return result == null ? 0 : result;
    }

    @FunctionalInterface
    @HostAccess.Implementable
    public interface Callback {

        Type<Callback, Callback> TYPE = new FunctionalType<>(Callback.class, "(ctx: CommandContext) => number | undefined | null | void");

        @HostAccess.Export
        Integer execute(ScriptCommandContext context);
    }
}

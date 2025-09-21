package com.manchickas.marshal.command;

import com.google.common.collect.ImmutableMap;
import com.manchickas.fuse.std.ScriptWrapper;
import com.manchickas.fuse.type.codec.ForwardingType;
import com.manchickas.marshal.segment.ParameterSegment;
import com.manchickas.zet.Undefined;
import com.manchickas.zet.Zet;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TextCodecs;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public final class ScriptCommandContext extends ScriptWrapper<CommandContext<ServerCommandSource>> {

    private final Map<String, Function<CommandContext<ServerCommandSource>, ?>> accessors;

    private ScriptCommandContext(CommandContext<ServerCommandSource> wrapped,
                                 ParameterSegment... parameters) {
        super(wrapped);
        this.accessors = Arrays.stream(parameters)
                .collect(ImmutableMap.<@NotNull ParameterSegment, String, Function<CommandContext<ServerCommandSource>, ?>>toImmutableMap(
                        ParameterSegment::name,
                        segment -> segment::access
                ));
    }

    public static ScriptCommandContext wrap(CommandContext<ServerCommandSource> ctx, ParameterSegment... arguments) {
        return new ScriptCommandContext(ctx, arguments);
    }

    @HostAccess.Export
    public void sendMessage(Value message) {
        var _message = Zet.expect(new ForwardingType<>(TextCodecs.CODEC), message);
        this.wrapped.getSource().sendMessage(_message);
    }

    @HostAccess.Export
    public Value arg(String name) {
        var accessor = this.accessors.get(name);
        if (accessor == null)
            return Undefined.UNDEFINED;
        return Value.asValue(accessor.apply(this.wrapped));
    }

    @HostAccess.Export
    public Value args() {
        var builder = ImmutableMap.<String, Value>builder();
        for (var entry : this.accessors.entrySet()) {
            var name = entry.getKey();
            var accessor = entry.getValue();
            builder.put(name, Value.asValue(accessor.apply(this.wrapped)));
        }
        return Value.asValue(builder.build());
    }
}

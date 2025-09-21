package com.manchickas.marshal.command;

import com.manchickas.fuse.ArrayBuilder;
import com.manchickas.fuse.exception.ScriptException;
import com.manchickas.marshal.lexer.Lexer;
import com.manchickas.marshal.parser.Parser;
import com.manchickas.marshal.prefix.Prefix;
import com.manchickas.marshal.segment.ParameterSegment;
import com.manchickas.marshal.segment.LiteralSegment;
import com.manchickas.marshal.segment.Segment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class CommandPath {

    private final @Nullable Prefix prefix;
    private final LiteralSegment[] redirects;
    private final Segment[] segments;

    public CommandPath(@Nullable Prefix prefix,
                       LiteralSegment[] redirects,
                       Segment[] segments) {
        this.prefix = prefix;
        this.segments = segments;
        this.redirects = redirects;
    }

    public static CommandPath compile(String source) throws ScriptException {
        var lexer = new Lexer(source);
        var parser = new Parser(lexer);
        return parser.parse();
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, ScriptCommand.Callback callback) {
        var node = dispatcher.register(this.build(callback));
        for (var redirect : this.redirects)
            dispatcher.register(redirect.build().redirect(node));
    }

    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<ServerCommandSource> build(ScriptCommand.Callback callback) {
        var length = this.segments.length;
        var builder = this.segments[length - 1].build();
        var parameters = ArrayBuilder.<ParameterSegment>builder(length);
        if (length > 1) {
            for (var i = this.segments.length - 2; i >= 0; i--) {
                var segment = this.segments[i];
                if (segment instanceof ParameterSegment argseg)
                    parameters.append(argseg);
                builder = segment.build().then(builder);
            }
        }
        if (this.prefix != null)
            builder.requires(this.prefix);
        return (LiteralArgumentBuilder<ServerCommandSource>) builder.executes(new ScriptCommand(parameters.build(ParameterSegment[]::new), callback));
    }

    @Override
    public String toString() {
        return "CommandPath{" +
                "prefix=" + prefix +
                ", segments=" + Arrays.toString(segments) +
                ", redirects=" + Arrays.toString(redirects) +
                '}';
    }
}

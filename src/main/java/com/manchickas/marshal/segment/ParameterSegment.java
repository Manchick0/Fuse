package com.manchickas.marshal.segment;

import com.manchickas.marshal.function.ParameterFunction;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public record ParameterSegment(String name, ParameterFunction<?> function) implements Segment {

    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build() {
        return this.function.build(this.name);
    }

    public Object access(CommandContext<ServerCommandSource> context) {
        return this.function.get(context, this.name);
    }
}

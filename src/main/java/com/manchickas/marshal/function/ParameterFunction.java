package com.manchickas.marshal.function;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public interface ParameterFunction<T> {

    RequiredArgumentBuilder<ServerCommandSource, ?> build(String name);
    T get(CommandContext<ServerCommandSource> ctx, String name);
}

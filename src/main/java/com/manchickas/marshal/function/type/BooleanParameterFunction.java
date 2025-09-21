package com.manchickas.marshal.function.type;

import com.manchickas.marshal.function.ParameterFunction;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public record BooleanParameterFunction() implements ParameterFunction<Boolean> {

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> build(String name) {
        return CommandManager.argument(name, BoolArgumentType.bool());
    }

    @Override
    public Boolean get(CommandContext<ServerCommandSource> ctx, String name) {
        return BoolArgumentType.getBool(ctx, name);
    }
}

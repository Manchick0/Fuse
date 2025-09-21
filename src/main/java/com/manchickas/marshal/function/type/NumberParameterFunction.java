package com.manchickas.marshal.function.type;

import com.manchickas.marshal.function.ParameterFunction;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public record NumberParameterFunction(double min, double max) implements ParameterFunction<Double> {

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> build(String name) {
        return CommandManager.argument(name, DoubleArgumentType.doubleArg(min, max));
    }

    @Override
    public Double get(CommandContext<ServerCommandSource> ctx, String name) {
        return DoubleArgumentType.getDouble(ctx, name);
    }
}

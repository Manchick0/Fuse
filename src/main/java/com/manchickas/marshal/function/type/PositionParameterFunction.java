package com.manchickas.marshal.function.type;

import com.manchickas.fuse.std.util.ScriptPosition;
import com.manchickas.marshal.function.ParameterFunction;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Supplier;

public record PositionParameterFunction(Type type) implements ParameterFunction<ScriptPosition> {

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> build(String name) {
        return CommandManager.argument(name, this.type.build());
    }

    @Override
    public ScriptPosition get(CommandContext<ServerCommandSource> ctx, String name) {
        if (this.type == Type.DIRECT) {
            var pos = Vec3ArgumentType.getVec3(ctx, name);
            return ScriptPosition.wrap(pos);
        }
        var pos = BlockPosArgumentType.getBlockPos(ctx, name);
        return ScriptPosition.wrap(pos);
    }

    public enum Type {

        DIRECT(Vec3ArgumentType::vec3),
        ALIGNED(BlockPosArgumentType::blockPos);

        private final Supplier<ArgumentType<PosArgument>> builder;

        Type(Supplier<ArgumentType<PosArgument>> builder) {
            this.builder = builder;
        }

        public ArgumentType<PosArgument> build() {
            return this.builder.get();
        }
    }
}

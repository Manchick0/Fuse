package com.manchickas.marshal.segment;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public record LiteralSegment(String literal) implements Segment {

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal(this.literal);
    }
}

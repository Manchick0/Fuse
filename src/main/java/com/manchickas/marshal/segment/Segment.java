package com.manchickas.marshal.segment;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public sealed interface Segment permits ParameterSegment, LiteralSegment {

    ArgumentBuilder<ServerCommandSource, ?> build();
}

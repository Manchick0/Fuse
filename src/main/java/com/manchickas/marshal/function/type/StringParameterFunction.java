package com.manchickas.marshal.function.type;

import com.manchickas.marshal.function.ParameterFunction;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public record StringParameterFunction(String... suggestions) implements ParameterFunction<String>, SuggestionProvider<ServerCommandSource> {

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> build(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests(this);
    }

    @Override
    public String get(CommandContext<ServerCommandSource> ctx, String name) {
        return StringArgumentType.getString(ctx, name);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(this.suggestions, builder);
    }
}

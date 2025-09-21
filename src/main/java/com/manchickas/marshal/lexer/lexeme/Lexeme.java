package com.manchickas.marshal.lexer.lexeme;

import com.manchickas.optionated.option.Option;
import com.manchickas.source.span.Span;
import org.jetbrains.annotations.NotNull;

public record Lexeme<T>(LexemeType<T> type, T value, Span span) {

    public boolean isOf(LexemeType<?> type) {
        return this.type.equals(type);
    }

    public <V> boolean isOf(LexemeType<V> type, V value) {
        return this.type.equals(type) && this.value.equals(value);
    }

    public <V> Option<V> parse(LexemeType<V> type) {
        if (this.type.equals(type))
            return type.parse(this.value);
        return Option.none();
    }

    @Override
    public @NotNull String toString() {
        return this.type.toString() + '(' + this.value + ')';
    }
}

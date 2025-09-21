package com.manchickas.fuse.type.codec;

import com.manchickas.optionated.option.Option;
import com.manchickas.zet.SetChain;
import com.manchickas.zet.type.Type;
import com.mojang.serialization.Codec;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ForwardingType<T> implements Type<T, T> {

    private final @NotNull Codec<T> codec;

    public ForwardingType(@NotNull Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public @NotNull Option<T> parse(Value value) {
        var result = this.codec.parse(JSOps.getInstance(), value);
        if (result.hasResultOrPartial())
            return result.resultOrPartial()
                    .map(Option::some)
                    .orElse(Option.none());
        return Option.none();
    }

    @Override
    public @NotNull Option<Value> serialize(@Nullable T t) {
        var result = this.codec.encodeStart(JSOps.getInstance(), t);
        if (result.hasResultOrPartial())
            return result.resultOrPartial()
                    .map(Option::some)
                    .orElse(Option.none());
        return Option.none();
    }

    @Override
    public @NotNull String name(@NotNull SetChain<Integer> setChain) {
        return "...";
    }
}

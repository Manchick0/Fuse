package com.manchickas.fuse.type;

import com.manchickas.optionated.option.Option;
import com.manchickas.zet.SetChain;
import com.manchickas.zet.type.Type;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FunctionalType<I> implements Type<I, I> {

    private final Class<I> prototype;
    private final String signature;

    public FunctionalType(Class<I> prototype,
                          String signature) {
        this.prototype = prototype;
        this.signature = signature;
    }

    @Override
    public @NotNull Option<I> parse(Value value) {
        if (value.canExecute()) {
            try {
                return Option.some(value.as(this.prototype));
            } catch (ClassCastException e) {
                return Option.none();
            }
        }
        return Option.none();
    }

    @Override
    public @NotNull Option<Value> serialize(@Nullable I i) {
        return Option.some(Value.asValue(i));
    }

    @Override
    public @NotNull String name(@NotNull SetChain<Integer> setChain) {
        return this.signature;
    }
}

package com.manchickas.fuse.type;

import com.manchickas.fuse.std.util.ScriptIdentifier;
import com.manchickas.optionated.option.Option;
import com.manchickas.zet.SetChain;
import com.manchickas.zet.type.Type;
import net.minecraft.registry.Registry;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistryType<T> implements Type<T, T> {

    private final Registry<T> registry;

    public RegistryType(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Option<T> parse(Value value) {
        if (value.isString())
            return ScriptIdentifier.compile(value.asString())
                    .map(ScriptIdentifier::toVanilla)
                    .bind(id -> Option.fromNullable(this.registry.get(id)));
        return Option.none();
    }

    @Override
    public @NotNull Option<Value> serialize(@Nullable T value) {
        return Option.fromNullable(this.registry.getId(value))
                .map(Object::toString)
                .map(Value::asValue);
    }

    @Override
    public @NotNull String name(@NotNull SetChain<Integer> encountered) {
        return this.registry.getKey()
                .getValue()
                .toString();
    }
}

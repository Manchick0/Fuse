package com.manchickas.fuse.type.codec;

import com.google.common.collect.ImmutableMap;
import com.manchickas.fuse.ArrayBuilder;
import com.manchickas.zet.TypeException;
import com.manchickas.zet.Undefined;
import com.manchickas.zet.Zet;
import com.manchickas.zet.type.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public final class JSOps implements DynamicOps<Value> {

    private static final MapLike<Value> EMPTY = new MapLike<>() {

        @Override
        public Value get(Value key) {
            return null;
        }

        @Override
        public Value get(String key) {
            return null;
        }

        @Override
        public Stream<Pair<Value, Value>> entries() {
            return Stream.empty();
        }
    };
    private static final JSOps INSTANCE = new JSOps();

    private JSOps() {
    }

    public static JSOps getInstance() {
        return JSOps.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Value input) {
        if (input.isNumber()) {
            if (input.fitsInByte())
                return outOps.createByte(input.asByte());
            if (input.fitsInShort())
                return outOps.createShort(input.asShort());
            if (input.fitsInInt())
                return outOps.createInt(input.asInt());
            if (input.fitsInLong())
                return outOps.createLong(input.asLong());
            if (input.fitsInFloat())
                return outOps.createFloat(input.asFloat());
            return outOps.createDouble(input.asDouble());
        }
        if (input.isString())
            return outOps.createString(input.asString());
        if (input.isBoolean())
            return outOps.createBoolean(input.asBoolean());
        if (input.hasMembers() && !input.hasArrayElements() || input.isHostObject() && input.asHostObject() instanceof Map<?,?>)
            return this.convertMap(outOps, input);
        if (input.hasArrayElements() || input.isHostObject() && input.asHostObject() instanceof Value[])
            return this.convertList(outOps, input);
        return outOps.empty();
    }

    @Override
    public DataResult<Number> getNumberValue(Value input) {
        try {
            var result = Zet.expect(Type.NUMBER, input);
            return DataResult.success(result);
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public Value createNumeric(Number i) {
        return Value.asValue(i);
    }

    @Override
    public DataResult<String> getStringValue(Value input) {
        try {
            var result = Zet.expect(Type.STRING, input);
            return DataResult.success(result);
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public Value createString(String value) {
        return Value.asValue(value);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Value input) {
        try {
            var result = Zet.expect(Type.BOOLEAN, input);
            return DataResult.success(result);
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public Value createBoolean(boolean value) {
        return Value.asValue(value);
    }

    @Override
    public DataResult<Value> mergeToList(Value input, Value value) {
        try {
            var array = Zet.expect(Type.ANY.array(Value[]::new), input);
            return DataResult.success(Value.asValue(ArrayBuilder.builder(array.length + 1)
                    .appendAll(array)
                    .append(value)
                    .build(Value[]::new)));
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public DataResult<Value> mergeToMap(Value input, Value key, Value value) {
        try {
            var map = Zet.expect(Type.map(Type.ANY), input);
            var builder = ImmutableMap.<String, Value>builder();
            for (var entry : map.entrySet()) {
                builder.put(entry.getKey(), entry.getValue());
            }
            builder.put(key.asString(), value);
            return DataResult.success(Value.asValue(builder.build()));
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public DataResult<Stream<Pair<Value, Value>>> getMapValues(Value input) {
        try {
            var map = Zet.expect(Type.map(Type.ANY), input);
            return DataResult.success(map.entrySet().stream()
                    .map(entry -> new Pair<>(
                            Value.asValue(entry.getKey()),
                            entry.getValue()
                    )));
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public DataResult<MapLike<Value>> getMap(Value input) {
        if (input.isNull())
            return DataResult.success(JSOps.EMPTY);
        try {
            var map = Zet.expect(Type.map(Type.ANY), input);
            return DataResult.success(new MapLike<>() {

                @Override
                public Value get(Value key) {
                    return this.get(key.asString());
                }

                @Override
                public Value get(String key) {
                    return map.get(key);
                }

                @Override
                public Stream<Pair<Value, Value>> entries() {
                    return map.entrySet().stream()
                            .map(entry -> new Pair<>(
                                    Value.asValue(entry.getKey()),
                                    entry.getValue()
                            ));
                }
            });
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public Value createMap(Stream<Pair<Value, Value>> map) {
        return Value.asValue(map.collect(ImmutableMap.<@NotNull Pair<Value, Value>, String, Value>toImmutableMap(pair -> pair.getFirst().asString(), Pair::getSecond)));
    }

    @Override
    public DataResult<Stream<Value>> getStream(Value input) {
        try {
            var array = Zet.expect(Type.ANY.array(Value[]::new), input);
            return DataResult.success(Arrays.stream(array));
        } catch (TypeException e) {
            return DataResult.error(e::getMessage);
        }
    }

    @Override
    public Value createList(Stream<Value> input) {
        return Value.asValue(input.collect(ArrayBuilder.toArray(Value[]::new)));
    }

    @Override
    public Value remove(Value input, String key) {
        var map = Zet.expect(Type.map(Type.ANY), input);
        var builder = ImmutableMap.<String, Value>builder();
        for (var entry : map.entrySet()) {
            var _key = entry.getKey();
            if (key.equals(_key))
                continue;
            builder.put(_key, entry.getValue());
        }
        return Value.asValue(builder.build());
    }

    @Override
    public Value empty() {
        return Undefined.UNDEFINED;
    }
}

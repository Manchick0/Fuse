package com.manchickas.fuse.script.property;

import com.google.common.collect.ImmutableMap;
import com.manchickas.fuse.ArrayBuilder;
import com.manchickas.fuse.exception.ExceptionTemplate;
import com.manchickas.fuse.exception.ScriptException;
import com.manchickas.fuse.script.property.operation.Getter;
import com.manchickas.fuse.script.property.operation.Operation;
import com.manchickas.fuse.script.property.operation.Setter;
import com.manchickas.fuse.std.util.ScriptIdentifier;
import com.manchickas.fuse.std.util.ScriptPosition;
import com.manchickas.zet.SetChain;
import com.manchickas.zet.Undefined;
import com.manchickas.zet.Zet;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class Property {

    private static final ExceptionTemplate CIRCULAR_REFERENCE = ScriptException.template("Encountered a circular reference while serializing property '{}'.");
    private static final ExceptionTemplate UNSERIALIZABLE = ScriptException.template("Attempted to serialize property '{}' set to an unserializable type '{}'.");
    private static final ExceptionTemplate UNRECOGNIZED_TAG = ScriptException.template("Encountered an unrecognized tag '0x{}' while deserializing property '{}'.");
    private static final ExceptionTemplate INVALID_STRUCTURE = ScriptException.template("Encountered an invalid structure while deserializing property '{}'.");
    private static final ExceptionTemplate TAG_MISMATCH = ScriptException.template("Encountered a mismatch between the tag '0x{}' and the present value while deserializing property '{}'.");
    private static final ExceptionTemplate UNTAGGED_COMPOUND = ScriptException.template("Encountered an untagged element while deserializing property '{}'.");

    private final Identifier identifier;
    private Value value;

    public Property(Identifier identifier, Value value) {
        this.identifier = identifier;
        this.value = value;
    }

    public static Value deserialize(Identifier identifier, NbtElement element) throws ScriptException {
        if (element instanceof NbtCompound compound) {
            var type = compound.getByte("type")
                    .orElseThrow(() -> UNTAGGED_COMPOUND.build(identifier));
            return switch (type) {
                case 0x04 -> Value.asValue(compound.getByte("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("04", identifier)));
                case 0x05 -> Value.asValue(compound.getShort("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("05", identifier)));
                case 0x06 -> Value.asValue(compound.getInt("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("06", identifier)));
                case 0x07 -> Value.asValue(compound.getLong("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("07", identifier)));
                case 0x08 -> Value.asValue(compound.getFloat("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("08", identifier)));
                case 0x09 -> Value.asValue(compound.getDouble("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("09", identifier)));
                case 0x0A -> Value.asValue(compound.getString("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("0A", identifier)));
                case 0x0B -> {
                    var wrapped = compound.getList("value")
                            .orElseThrow(() -> TAG_MISMATCH.build("0B", identifier));
                    var builder = ArrayBuilder.<Value>builder(wrapped.size());
                    for (var el : wrapped)
                        builder.append(Property.deserialize(identifier, el));
                    yield Value.asValue(builder.build(Value[]::new));
                }
                case 0x0C -> {
                    var wrapped = compound.getCompound("value")
                            .orElseThrow(() -> TAG_MISMATCH.build("0C", identifier));
                    var builder = ImmutableMap.<String, Value>builder();
                    for (var entry : wrapped.entrySet()) {
                        var key = entry.getKey();
                        var el = entry.getValue();
                        builder.put(key, Property.deserialize(identifier, el));
                    }
                    yield Value.asValue(builder.build());
                }
                case 0x0D -> Value.asValue(ScriptIdentifier.compile(compound.getString("value")
                        .orElseThrow(() -> TAG_MISMATCH.build("0D", identifier))));
                case 0x0E -> {
                    var wrapped = compound.getList("value")
                            .orElseThrow(() -> TAG_MISMATCH.build("0E", identifier));
                    if (wrapped.size() == 3) {
                        var x = wrapped.getDouble(0).orElseThrow(() -> TAG_MISMATCH.build("0E", identifier));
                        var y = wrapped.getDouble(1).orElseThrow(() -> TAG_MISMATCH.build("0E", identifier));
                        var z = wrapped.getDouble(2).orElseThrow(() -> TAG_MISMATCH.build("0E", identifier));
                        yield Value.asValue(ScriptPosition.wrap(x, y, z));
                    }
                    throw TAG_MISMATCH.build("0E", identifier);
                }
                default -> throw Property.UNRECOGNIZED_TAG.build(Integer.toHexString(type)
                        .toUpperCase(), identifier);
            };
        }
        if (element instanceof NbtByte(var tag))
            return switch (tag) {
                case 0x00 -> Value.asValue(null);
                case 0x01 -> Value.asValue(Undefined.UNDEFINED);
                case 0x02 -> Value.asValue(true);
                case 0x03 -> Value.asValue(false);
                default -> throw Property.INVALID_STRUCTURE.build(Integer.toHexString(tag).toUpperCase(), identifier);
            };
        throw Property.INVALID_STRUCTURE.build(identifier);
    }

    private static NbtElement serialize(Identifier identifier, Value value, SetChain<Value> encountered) throws ScriptException {
        if (Undefined.isStrictlyNull(value))
            return Property.tag((byte) 0x00, null);
        if (Undefined.isStrictlyUndefined(value))
            return Property.tag((byte) 0x01, null);
        if (value.isBoolean()) {
            if (value.asBoolean())
                return Property.tag((byte) 0x02, null);
            return Property.tag((byte) 0x03, null);
        }
        if (value.isNumber()) {
            if (value.fitsInByte())
                return Property.tag((byte) 0x04, NbtByte.of(value.asByte()));
            if (value.fitsInShort())
                return Property.tag((byte) 0x05, NbtShort.of(value.asShort()));
            if (value.fitsInInt())
                return Property.tag((byte) 0x06, NbtInt.of(value.asInt()));
            if (value.fitsInLong())
                return Property.tag((byte) 0x07, NbtLong.of(value.asLong()));
            if (value.fitsInFloat())
                return Property.tag((byte) 0x08, NbtFloat.of(value.asFloat()));
            return Property.tag((byte) 0x09, NbtDouble.of(value.asDouble()));
        }
        if (value.isString())
            return Property.tag((byte) 0x0A, NbtString.of(value.asString()));
        if (value.hasArrayElements()) {
            if (encountered.add(value)) {
                var result = new NbtList();
                for (int i = 0; i < value.getArraySize(); i++) {
                    var element = value.getArrayElement(i);
                    result.add(Property.serialize(identifier, element, new SetChain<>(encountered)));
                }
                return Property.tag((byte) 0x0B, result);
            }
            throw Property.CIRCULAR_REFERENCE.build(identifier);
        }
        if (value.hasMembers()) {
            if (encountered.add(value)) {
                var result = new NbtCompound();
                for (var key : value.getMemberKeys()) {
                    var element = value.getMember(key);
                    result.put(key, Property.serialize(identifier, element, new SetChain<>(encountered)));
                }
                return Property.tag((byte) 0x0C, result);
            }
            throw Property.CIRCULAR_REFERENCE.build(identifier);
        }
        if (value.isHostObject()) {
            var host = value.asHostObject();
            if (host instanceof Value[] array) {
                var result = new NbtList();
                for (var element : array)
                    result.add(Property.serialize(identifier, element, new SetChain<>(encountered)));
                return Property.tag((byte) 0x0B, result);
            }
            if (host instanceof Map<?, ?> map) {
                var result = new NbtCompound();
                for (var entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        if (entry.getValue() instanceof Value element) {
                            result.put(key, Property.serialize(identifier, element, new SetChain<>(encountered)));
                            continue;
                        }
                    }
                    throw Property.UNSERIALIZABLE.build(identifier, Zet.describe(value));
                }
                return Property.tag((byte) 0x0C, result);
            }
            if (host instanceof ScriptIdentifier id)
                return Property.tag((byte) 0x0D, NbtString.of(id.toString()));
            if (host instanceof ScriptPosition position) {
                var result = new NbtList();
                result.add(NbtDouble.of(position.x));
                result.add(NbtDouble.of(position.y));
                result.add(NbtDouble.of(position.z));
                return Property.tag((byte) 0x0E, result);
            }
        }
        throw Property.UNSERIALIZABLE.build(identifier, Zet.describe(value));
    }

    private static NbtElement tag(byte type, @Nullable NbtElement element) {
        if (element != null) {
            var compound = new NbtCompound();
            compound.putByte("type", type);
            compound.put("value", element);
            return compound;
        }
        return NbtByte.of(type);
    }

    @HostAccess.Export
    public void set(Value value) {
        if (value.canExecute()) {
            this.value = value.execute(this.value);
            return;
        }
        this.value = value;
    }

    @HostAccess.Export
    public Value get() {
        return this.value;
    }

    public Operation[] operations() {
        return new Operation[] {
                (Getter) this::get,
                (Setter) this::set
        };
    }

    public NbtElement serialize() throws ScriptException {
        return Property.serialize(this.identifier, this.value, new SetChain<>());
    }
}

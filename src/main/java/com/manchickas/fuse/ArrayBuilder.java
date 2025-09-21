package com.manchickas.fuse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collector;

public final class ArrayBuilder<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private T[] buffer;
    private int capacity;
    private int length;

    @SuppressWarnings("unchecked")
    private ArrayBuilder(int capacity) {
        this.buffer = (T[]) new Object[capacity];
        this.capacity = capacity;
        this.length = 0;
    }

    @SafeVarargs
    public static <T> ArrayBuilder<T> builder(T first, T... rest) {
        return new ArrayBuilder<T>(rest.length + 1)
                .append(first)
                .appendAll(rest);
    }

    @Contract(pure = true)
    public static <T> ArrayBuilder<T> builder() {
        return new ArrayBuilder<>(DEFAULT_CAPACITY);
    }

    @Contract(pure = true)
    public static <T> ArrayBuilder<T> builder(int capacity) {
        return new ArrayBuilder<>(capacity);
    }

    @Contract(pure = true)
    public static <T> Collector<T, ArrayBuilder<T>, T[]> toArray(@NotNull IntFunction<T[]> factory) {
        Objects.requireNonNull(factory);
        return Collector.of(
                ArrayBuilder::builder,
                ArrayBuilder::append,
                ArrayBuilder::appendAll,
                builder -> builder.build(factory)
        );
    }

    @Contract("_ -> this")
    public ArrayBuilder<T> appendAll(@NotNull T[] elements) {
        Objects.requireNonNull(elements);
        return this.appendAll(elements, 0, elements.length);
    }

    @Contract("_, _ -> this")
    public ArrayBuilder<T> appendAll(@NotNull T[] elements, int offset) {
        Objects.requireNonNull(elements);
        return this.appendAll(elements, offset, elements.length - offset);
    }

    @Contract("_, _, _ -> this")
    public ArrayBuilder<T> appendAll(@NotNull T[] elements, int offset, int length) {
        Objects.requireNonNull(elements);
        for (var i = offset; i < offset + length; i++)
            this.append(elements[i]);
        return this;
    }

    @Contract("_ -> this")
    public ArrayBuilder<T> appendAll(@NotNull Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        for (var element : iterable)
            this.append(element);
        return this;
    }

    @Contract("_ -> this")
    public ArrayBuilder<T> appendAll(@NotNull ArrayBuilder<T> builder) {
        Objects.requireNonNull(builder);
        return this.appendAll(builder.buffer, 0, builder.length);
    }

    @Contract("_ -> this")
    public ArrayBuilder<T> append(T element) {
        if (this.length >= this.capacity)
            this.buffer = Arrays.copyOf(this.buffer, this.capacity *= 2);
        this.buffer[this.length++] = element;
        return this;
    }

    @Contract("_ -> this")
    public ArrayBuilder<T> trim(int amount) {
        this.length -= Math.clamp(amount, 0, this.length);
        return this;
    }

    @Contract("-> this")
    public ArrayBuilder<T> clear() {
        this.length = 0;
        return this;
    }

    @Contract(pure = true)
    public T[] build(@NotNull IntFunction<T[]> factory) {
        var result = Objects.requireNonNull(factory)
                .apply(this.length);
        System.arraycopy(this.buffer, 0, result, 0, this.length);
        return result;
    }

    @Contract(pure = true)
    public T[] build(@NotNull T[] empty) {
        var buffer = Arrays.copyOf(Objects.requireNonNull(empty), this.length);
        System.arraycopy(this.buffer, 0, buffer, 0, this.length);
        return buffer;
    }
}

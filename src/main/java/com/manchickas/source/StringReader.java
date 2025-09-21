package com.manchickas.source;

import com.manchickas.source.span.LineSpan;
import com.manchickas.source.span.Span;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import net.minecraft.util.Util;

public class StringReader {

    private static final Int2CharMap ESCAPE_MAP = Util.make(new Int2CharOpenHashMap(), map -> {
        map.put('"', '"');
        map.put('\n', 'n');
        map.put('\r', 'r');
        map.put('\t', 't');
        map.put('\b', 'b');
        map.put('\f', 'f');
        map.put('\\', '\\');
    });
    protected static final IntSet WHITESPACE = IntSet.of(' ', '\t', '\n', '\r');

    protected final ObjectArrayFIFOQueue<Position> stamps;
    protected final Int2ObjectMap<String> lineCache;
    protected final IntArrayFIFOQueue lineBounds;
    protected final String source;
    private int cursor;
    private int column;
    private int line;

    public StringReader(String source) {
        this.stamps = new ObjectArrayFIFOQueue<>(16);
        this.lineCache = new Int2ObjectOpenHashMap<>();
        this.lineBounds = new IntArrayFIFOQueue(16);
        this.source = source;
        this.cursor = 0;
        this.column = 1;
        this.line = 1;
    }

    public static String escape(String source) {
        var reader = new StringReader(source);
        var buffer = new StringBuilder();
        while (reader.canRead()) {
            var c = reader.read();
            if (ESCAPE_MAP.containsKey(c)) {
                buffer.append('\\')
                        .append(ESCAPE_MAP.get(c));
                continue;
            }
            buffer.appendCodePoint(c);
        }
        return buffer.toString();
    }

    public int peek() {
        return this.source.codePointAt(this.cursor);
    }

    public int peek(int offset) {
        var cursor = this.cursor;
        while (offset-- > 0)
            cursor += Character.charCount(this.source.codePointAt(cursor));
        return this.source.codePointAt(cursor);
    }

    public int read() {
        var c = this.peek();
        this.cursor += Character.charCount(c);
        if (c == '\n') {
            this.line++;
            this.column = 1;
            this.lineBounds.enqueueFirst(this.cursor);
            return c;
        }
        this.column++;
        return c;
    }

    public boolean canRead() {
        return this.cursor < this.source.length();
    }

    public boolean skipWhitespace() {
        while (this.canRead() && WHITESPACE.contains(this.peek()))
            this.read();
        return this.canRead();
    }

    public String queryLine() {
        var cached = this.lineCache.get(this.line);
        if (cached == null) {
            var bound = this.lineBounds.isEmpty() ? 0
                    : this.lineBounds.firstInt();
            this.pushStamp();
            this.cursor = bound;
            var builder = new StringBuilder();
            while (this.canRead()) {
                var c = this.peek();
                if (c == '\n')
                    break;
                builder.appendCodePoint(c);
                this.read();
            }
            var line = builder.toString();
            this.lineCache.put(this.line, line);
            this.backtrack(this.popStamp());
            return line;
        }
        return cached;
    }

    public void pushStamp() {
        var position = new Position(this.cursor, this.line, this.column);
        this.stamps.enqueueFirst(position);
    }

    public Position popStamp() {
        return this.stamps.dequeue();
    }

    public void backtrack(Position position) {
        this.cursor = position.cursor();
        this.line = position.line();
        this.column = position.column();
    }

    public Span charSpan() {
        return new LineSpan(this.queryLine(), this.line, this.column, this.column + 1);
    }

    public Span span(Position from) {
        return new LineSpan(this.queryLine(), this.line, from.column(), this.column + 1);
    }

    public record Position(int cursor, int line, int column) {
    }
}

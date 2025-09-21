package com.manchickas.marshal.parser;

import com.manchickas.fuse.ArrayBuilder;
import com.manchickas.fuse.exception.ExceptionTemplate;
import com.manchickas.fuse.exception.ScriptException;
import com.manchickas.fuse.std.util.ScriptPosition;
import com.manchickas.marshal.command.CommandPath;
import com.manchickas.marshal.function.ParameterFunction;
import com.manchickas.marshal.function.type.BooleanParameterFunction;
import com.manchickas.marshal.function.type.PositionParameterFunction;
import com.manchickas.marshal.function.type.StringParameterFunction;
import com.manchickas.marshal.lexer.Lexer;
import com.manchickas.marshal.lexer.lexeme.Lexeme;
import com.manchickas.marshal.lexer.lexeme.LexemeType;
import com.manchickas.marshal.prefix.Prefix;
import com.manchickas.marshal.segment.ParameterSegment;
import com.manchickas.marshal.segment.LiteralSegment;
import com.manchickas.marshal.segment.Segment;
import com.manchickas.optionated.option.Option;
import com.manchickas.optionated.option.Some;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.jetbrains.annotations.Nullable;

public final class Parser {

    private static final ExceptionTemplate TYPE_MISMATCH = ScriptException.template("Encountered a lexeme of type '{}' where '{}' was expected.");
    private static final ExceptionTemplate VALUE_TYPE_MISMATCH = ScriptException.template("Encountered a lexeme of type '{}' where '{}' ({}) was expected.");
    private static final ExceptionTemplate UNEXPECTED_EOF = ScriptException.template("Encountered an unexpected end of input while parsing a command path.");
    private static final ExceptionTemplate UNRECOGNIZED_PARAMETER_FUNCTION = ScriptException.template("Encountered an unrecognized parameter function type '{}'.");
    private static final ExceptionTemplate UNRECOGNIZED_POSITION_TYPE = ScriptException.template("Encountered an unrecognized position type '{}'.");

    private final Lexer lexer;
    private final ObjectArrayFIFOQueue<Lexeme<?>> buffer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.buffer = new ObjectArrayFIFOQueue<>(16);
    }

    public CommandPath parse() throws ScriptException {
        this.skipIf(LexemeType.SEPARATOR, '/');
        if (this.peeksAt(LexemeType.PREFIX)) {
            var prefix = this.readAs(LexemeType.PREFIX)
                    .map(pref -> pref.equals("*")
                            ? Prefix.ASTERISK
                            : Prefix.name(pref))
                    .unwrap();
            this.expect(LexemeType.SEPARATOR, '/');
            return this.parseInitial(prefix);
        }
        return this.parseInitial(null);
    }

    public CommandPath parseInitial(@Nullable Prefix prefix) throws ScriptException {
        var literal = this.expect(LexemeType.IDENTIFIER);
        if (this.peeksAt(LexemeType.SEPARATOR, '|')) {
            var builder = ArrayBuilder.<LiteralSegment>builder();
            while (this.canRead() && !this.peeksAt(LexemeType.SEPARATOR, '/')) {
                this.expect(LexemeType.SEPARATOR, '|');
                var redirect = this.expect(LexemeType.IDENTIFIER);
                builder.append(new LiteralSegment(redirect));
            }
            return this.parseSegments(prefix, new LiteralSegment(literal), builder.build(LiteralSegment[]::new));
        }
        return this.parseSegments(prefix, new LiteralSegment(literal), new LiteralSegment[0]);
    }

    public CommandPath parseSegments(@Nullable Prefix prefix, LiteralSegment initial, LiteralSegment[] redirects) throws ScriptException {
        var builder = ArrayBuilder.<Segment>builder(initial);
        if (this.peeksAt(LexemeType.SEPARATOR, '/')) {
            while (this.canRead() && this.peeksAt(LexemeType.SEPARATOR, '/')) {
                this.read();
                if (this.canRead()) {
                    builder.append(this.parseSegment());
                    continue;
                }
                break;
            }
        }
        return new CommandPath(prefix, redirects, builder.build(Segment[]::new));
    }

    public Segment parseSegment() throws ScriptException {
        var literal = this.expect(LexemeType.IDENTIFIER);
        if (this.skipIf(LexemeType.SEPARATOR, ':')) {
            var lexeme = this.peek();
            var type = this.expect(LexemeType.KEYWORD);
            var function = switch (type) {
                case "string" -> this.parseStringParameterFunction();
                case "position" -> this.parsePositionParameterFunction();
                case "boolean" -> this.parseBooleanParameterFunction();
                default -> throw UNRECOGNIZED_PARAMETER_FUNCTION.build(type)
                        .attachSpan(lexeme.span());
            };
            return new ParameterSegment(literal, function);
        }
        return new LiteralSegment(literal);
    }

    // StringParameterFunction = "string" , [ "(", [ String , { "," , String } ] ")" ]
    public ParameterFunction<String> parseStringParameterFunction() throws ScriptException {
        if (this.skipIf(LexemeType.SEPARATOR, '(')) {
            if (this.skipIf(LexemeType.SEPARATOR, ')'))
                return new StringParameterFunction();
            var builder = ArrayBuilder.builder(this.expect(LexemeType.STRING));
            while (this.canRead() && !this.peeksAt(LexemeType.SEPARATOR, ')')) {
                this.expect(LexemeType.SEPARATOR, ',');
                var suggestion = this.expect(LexemeType.STRING);
                builder.append(suggestion);
            }
            this.expect(LexemeType.SEPARATOR, ')');
            return new StringParameterFunction(builder.build(String[]::new));
        }
        return new StringParameterFunction();
    }

    // PositionParameterFunction = "position" , [ "(" , [ "direct" | "aligned" ] , ")" ]
    public ParameterFunction<ScriptPosition> parsePositionParameterFunction() throws ScriptException {
        if (this.skipIf(LexemeType.SEPARATOR, '(')) {
            if (this.skipIf(LexemeType.SEPARATOR, ')'))
                return new PositionParameterFunction(PositionParameterFunction.Type.DIRECT);
            var lexeme = this.peek();
            var type = this.expect(LexemeType.KEYWORD);
            var function = switch (type) {
                case "direct" -> new PositionParameterFunction(PositionParameterFunction.Type.DIRECT);
                case "aligned" -> new PositionParameterFunction(PositionParameterFunction.Type.ALIGNED);
                default -> throw UNRECOGNIZED_POSITION_TYPE.build(type)
                        .attachSpan(lexeme.span());
            };
            this.expect(LexemeType.SEPARATOR, ')');
            return function;
        }
        return new PositionParameterFunction(PositionParameterFunction.Type.DIRECT);
    }

    // BooleanParameterFunction = "boolean" , [ "()" ]
    public ParameterFunction<Boolean> parseBooleanParameterFunction() throws ScriptException {
        if (this.skipIf(LexemeType.SEPARATOR, '(')) {
            this.expect(LexemeType.SEPARATOR, ')');
            return new BooleanParameterFunction();
        }
        return new BooleanParameterFunction();
    }

    public Lexeme<?> peek() throws ScriptException {
        if (this.buffer.isEmpty()) {
            var lexeme = this.lexer.next();
            this.buffer.enqueue(lexeme);
            return lexeme;
        }
        return this.buffer.first();
    }

    public boolean peeksAt(LexemeType<?> type) throws ScriptException {
        return this.canRead() && this.peek().isOf(type);
    }

    public <V> boolean peeksAt(LexemeType<V> type, V value) throws ScriptException {
        return this.canRead() && this.peek().isOf(type, value);
    }

    public <V> boolean skipIf(LexemeType<V> type, V value) throws ScriptException {
        if (this.peeksAt(type, value)) {
            this.read();
            return true;
        }
        return false;
    }

    public boolean canRead() {
        return !this.buffer.isEmpty() || this.lexer.canRead();
    }

    public Lexeme<?> read() throws ScriptException {
        if (this.buffer.isEmpty())
            return this.lexer.next();
        return this.buffer.dequeue();
    }

    public <V> V expect(LexemeType<V> type) throws ScriptException {
        if (this.canRead()) {
            var lexeme = this.read();
            if (lexeme.isOf(type))
                return lexeme.parse(type)
                        .unwrap();
            throw TYPE_MISMATCH.build(lexeme.type().toString(), type.toString())
                    .attachSpan(lexeme.span());
        }
        throw UNEXPECTED_EOF.build();
    }

    public <V> V expect(LexemeType<V> type, V value) throws ScriptException {
        if (this.canRead()) {
            var lexeme = this.read();
            if (lexeme.isOf(type, value))
                return lexeme.parse(type)
                        .unwrap();
            throw VALUE_TYPE_MISMATCH.build(lexeme.type().toString(), value, type.toString())
                    .attachSpan(lexeme.span());
        }
        throw UNEXPECTED_EOF.build();
    }

    public <V> Option<V> readAs(LexemeType<V> type) throws ScriptException {
        return this.read().parse(type);
    }
}

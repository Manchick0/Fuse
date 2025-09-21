package com.manchickas.marshal.lexer;

import com.manchickas.fuse.exception.ExceptionTemplate;
import com.manchickas.fuse.exception.ScriptException;
import com.manchickas.marshal.lexer.lexeme.Lexeme;
import com.manchickas.marshal.lexer.lexeme.LexemeType;
import com.manchickas.source.StringReader;
import it.unimi.dsi.fastutil.ints.Int2CharMap;
import it.unimi.dsi.fastutil.ints.Int2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.Util;

import java.util.Set;
import java.util.regex.Pattern;

public final class Lexer extends StringReader {

    private static final ExceptionTemplate UNTERMINATED_STRING = ScriptException.template("Encountered an unterminated string literal while parsing command path '{}'.");
    private static final ExceptionTemplate NON_COMPLIANT_NUMBER = ScriptException.template("Encountered a non-compliant number literal '{}' while parsing command path '{}'.");
    private static final ExceptionTemplate INVALID_IDENTIFIER = ScriptException.template("Encountered an invalid identifier '{}' while parsing command path '{}'.");

    private static final IntSet SEPARATORS = IntSet.of('/', '|', ':', '.', ',', '(', ')');
    private static final Set<String> KEYWORDS = ObjectSet.of(
            "number", "integer", "string", "boolean", "position",
            "selector", "aligned", "direct", "any", "player"
    );
    private static final Int2CharMap ESCAPE_MAP = Util.make(new Int2CharOpenHashMap(), map -> {
        map.put('n', '\n');
        map.put('r', '\r');
        map.put('t', '\t');
        map.put('b', '\b');
        map.put('f', '\f');
        map.put('\\', '\\');
    });
    private static final Pattern IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public Lexer(String source) {
        super(source);
    }

    private static boolean isSign(int codePoint) {
        return codePoint == '-' || codePoint == '+';
    }

    public Lexeme<?> next() throws ScriptException {
        if (this.skipWhitespace()) {
            var c = this.peek();
            if (SEPARATORS.contains(c)) {
                this.read();
                return new Lexeme<>(LexemeType.SEPARATOR, (char) c, this.charSpan());
            }
            if (c == '@')
                return this.readPrefix();
            if (c == '"')
                return this.readString('"');
            if (c == '\'')
                return this.readString('\'');
            if (c >= '0' && c <= '9' || Lexer.isSign(c) && this.peek(1) >= '0' && this.peek(1) <= '9')
                return this.readNumber();
            return this.readGeneric();
        }
        return null;
    }

    private Lexeme<?> readGeneric() throws ScriptException {
        this.pushStamp();
        var buffer = new StringBuilder();
        while (this.canRead()) {
            var c = this.peek();
            if (SEPARATORS.contains(c) || WHITESPACE.contains(c) || c == '@' || c == '"' || c == '\'')
                break;
            buffer.appendCodePoint(c);
            this.read();
        }
        var lexeme = buffer.toString();
        var span = this.span(this.popStamp());
        if (KEYWORDS.contains(lexeme))
            return new Lexeme<>(LexemeType.KEYWORD, buffer.toString(), span);
        if (IDENTIFIER.matcher(lexeme).matches())
            return new Lexeme<>(LexemeType.IDENTIFIER, buffer.toString(), span);
        throw Lexer.INVALID_IDENTIFIER.build(lexeme, this.source)
                .attachSpan(span);
    }

    private Lexeme<Double> readNumber() throws ScriptException {
        this.pushStamp();
        var buffer = new StringBuilder();
        boolean readingDecimal = false,
            readingExponent = false;
        if (Lexer.isSign(this.peek())) {
            if (this.peek() == '-')
                buffer.append('-');
            this.read();
        }
        while (this.canRead()) {
            var c = this.peek();
            if (c == '.') {
                if (readingDecimal)
                    break;
                buffer.appendCodePoint('.');
                readingDecimal = true;
                this.read();
                continue;
            }
            if (c == 'e' || c == 'E') {
                if (readingExponent)
                    break;
                buffer.appendCodePoint('e');
                readingExponent = true;
                this.read();
                if (Lexer.isSign(this.peek())) {
                    if (this.peek() == '-')
                        buffer.append('-');
                    this.read();
                }
                continue;
            }
            if (c >= '0' && c <= '9') {
                buffer.appendCodePoint(c);
                this.read();
                continue;
            }
            break;
        }
        var span = this.span(this.popStamp());
        var lexeme = buffer.toString();
        try {
            return new Lexeme<>(LexemeType.NUMBER, Double.parseDouble(lexeme), span);
        } catch (NumberFormatException e) {
            throw Lexer.NON_COMPLIANT_NUMBER.build(lexeme, this.source)
                    .attachSpan(span);
        }
    }

    private Lexeme<String> readString(char quote) throws ScriptException {
        this.pushStamp();
        this.read(); // Consume the quote
        var buffer = new StringBuilder();
        while (this.canRead()) {
            var c = this.read();
            if (c == '\\') {
                var d = this.read();
                if (d == quote) {
                    buffer.append(quote);
                    continue;
                }
                if (ESCAPE_MAP.containsKey(d)) {
                    var escape = ESCAPE_MAP.get(d);
                    buffer.append(escape);
                    continue;
                }
            }
            if (c == quote) {
                var span = this.span(this.popStamp());
                return new Lexeme<>(LexemeType.STRING, buffer.toString(), span);
            }
            buffer.appendCodePoint(c);
        }
        throw Lexer.UNTERMINATED_STRING.build(this.source);
    }

    private Lexeme<String> readPrefix() {
        this.pushStamp();
        this.read(); // Consume the '@'
        if (this.peek() != '*') {
            var buffer = new StringBuilder();
            while (this.canRead()) {
                var c = this.peek();
                if (SEPARATORS.contains(c) || WHITESPACE.contains(c) || c == '@' || c == '"' || c == '\'')
                    break;
                buffer.appendCodePoint(c);
                this.read();
            }
            var span = this.span(this.popStamp());
            return new Lexeme<>(LexemeType.PREFIX, buffer.toString(), span);
        }
        this.read();
        var span = this.span(this.popStamp());
        return new Lexeme<>(LexemeType.PREFIX, "*", span);
    }
}

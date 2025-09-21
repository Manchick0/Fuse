package com.manchickas.marshal.lexer.lexeme;

import com.manchickas.optionated.option.Option;

public interface LexemeType<T> {

    LexemeType<String> STRING = LexemeType.ofString("string");
    LexemeType<Double> NUMBER = new LexemeType<>() {

        @Override
        public Option<Double> parse(Object value) {
            if (value instanceof Double d)
                return Option.some(d);
            return Option.none();
        }

        @Override
        public String toString() {
            return "number";
        }
    };
    LexemeType<Boolean> BOOLEAN = new LexemeType<>() {

        @Override
        public Option<Boolean> parse(Object value) {
            if (value instanceof Boolean b)
                return Option.some(b);
            return Option.none();
        }

        @Override
        public String toString() {
            return "boolean";
        }
    };
    LexemeType<String> KEYWORD = LexemeType.ofString("keyword");
    LexemeType<String> IDENTIFIER = LexemeType.ofString("identifier");
    LexemeType<Character> SEPARATOR = new LexemeType<>() {

        @Override
        public Option<Character> parse(Object value) {
            if (value instanceof Character c)
                return Option.some(c);
            return Option.none();
        }

        @Override
        public String toString() {
            return "separator";
        }
    };
    LexemeType<String> PREFIX = LexemeType.ofString("prefix");

    Option<T> parse(Object value);

    @Override
    String toString();

    private static LexemeType<String> ofString(String name) {
        return new LexemeType<>() {

            @Override
            public Option<String> parse(Object value) {
                if (value instanceof String s)
                    return Option.some(s);
                return Option.none();
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}

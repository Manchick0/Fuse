package com.manchickas.source.span;

public final class LineSpan implements Span {

    private final String source;
    private final int line;
    private final int start;
    private final int end;

    public LineSpan(String source,
                    int line,
                    int start,
                    int end) {
        this.line = line;
        this.source = source;
        this.start = start;
        this.end = end;
    }

    @Override
    public String underlineSource(boolean includeLineNumbers) {
        var builder = new StringBuilder();
        var offset = this.start;
        if (includeLineNumbers) {
            var number = this.line + " | ";
            offset += number.length() - 1;
            builder.append(number);
        }
        return builder.append(this.source)
                .append('\n')
                .repeat(' ', offset)
                .repeat('^', this.end - this.start - 1)
                .toString();
    }

    @Override
    public String toLocationString() {
        return String.format("(%d:%d-%d)", this.line, this.start, this.end);
    }
}

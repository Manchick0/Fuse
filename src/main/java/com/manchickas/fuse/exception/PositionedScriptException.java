package com.manchickas.fuse.exception;

import com.manchickas.source.span.Span;

public class PositionedScriptException extends ScriptException {

    private final Span span;

    PositionedScriptException(String message, Span span) {
        super(message);
        this.span = span;
    }

    @Override
    public ScriptException attachSpan(Span span) {
        return this;
    }

    @Override
    public String getMessage() {
        return String.format("%s\n%s %s",
                this.span.underlineSource(true),
                this.span.toLocationString(),
                super.getMessage());
    }
}

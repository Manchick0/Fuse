package com.manchickas.fuse.exception;

import com.manchickas.source.span.Span;

public class ScriptException extends Exception {

    ScriptException(String message) {
        super(message);
    }

    public static ExceptionTemplate template(String pattern) {
        return new ExceptionTemplate(pattern);
    }

    public ScriptException attachSpan(Span span) {
        return new PositionedScriptException(this.getMessage(), span);
    }
}

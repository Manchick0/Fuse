package com.manchickas.source.span;

public interface Span {

    String underlineSource(boolean includeLineNumbers);

    String toLocationString();
}

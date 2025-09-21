package com.manchickas.fuse.script.property.operation;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

@FunctionalInterface
public non-sealed interface Setter extends Operation {

    @HostAccess.Export
    void set(Value value);
}

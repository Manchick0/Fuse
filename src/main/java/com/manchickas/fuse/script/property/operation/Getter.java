package com.manchickas.fuse.script.property.operation;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

@FunctionalInterface
public non-sealed interface Getter extends Operation {

    @HostAccess.Export
    Value get();
}

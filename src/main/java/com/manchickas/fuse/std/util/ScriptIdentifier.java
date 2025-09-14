package com.manchickas.fuse.std.util;

public final class ScriptIdentifier {

    public final String namespace;
    public final String path;

    public ScriptIdentifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public ScriptIdentifier compile(String identifier) {

    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }
}

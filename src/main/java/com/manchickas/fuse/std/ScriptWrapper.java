package com.manchickas.fuse.std;

public abstract class ScriptWrapper<T> {

    protected final T wrapped;

    protected ScriptWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    public T unwrap() {
        return this.wrapped;
    }
}

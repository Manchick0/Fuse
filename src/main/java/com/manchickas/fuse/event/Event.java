package com.manchickas.fuse.event;

public abstract class Event<L extends EventListener> {

    public void tryProcess(EventListener listener) {
        var clazz = this.type().clazz();
        if (clazz.isInstance(listener))
            this.process(clazz.cast(listener));
    }

    public abstract void process(L listener);

    public abstract EventType<L> type();
}

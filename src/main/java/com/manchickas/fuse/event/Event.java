package com.manchickas.fuse.event;

public abstract class Event<L extends EventListener> {

    public Event<L> dispatch(Iterable<EventListener> listeners) {
        var clazz = this.type().clazz();
        for (var listener : listeners) {
            if (clazz.isInstance(listener))
                this.process(clazz.cast(listener));
        }
        return this;
    }

    public abstract void process(L listener);

    public abstract EventType<L> type();
}

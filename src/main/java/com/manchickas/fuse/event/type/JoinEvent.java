package com.manchickas.fuse.event.type;

import com.manchickas.fuse.event.Event;
import com.manchickas.fuse.event.EventListener;
import com.manchickas.fuse.event.EventType;

public final class JoinEvent extends Event<JoinEvent.Listener> {

    @Override
    public void process(Listener listener) {

    }

    @Override
    public EventType<Listener> type() {
        return null;
    }

    public interface Listener extends EventListener {

        void onJoin(JoinEvent event);
    }
}

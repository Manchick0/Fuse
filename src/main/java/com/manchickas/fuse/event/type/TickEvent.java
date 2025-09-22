package com.manchickas.fuse.event.type;

import com.manchickas.fuse.event.Event;
import com.manchickas.fuse.event.EventListener;
import com.manchickas.fuse.event.EventType;
import com.manchickas.fuse.std.entity.ScriptPlayer;
import org.graalvm.polyglot.HostAccess;

public final class TickEvent extends Event<TickEvent.Listener> {

    private final ScriptWor player;

    public TickEvent(ScriptPlayer player) {
        this.player = player;
    }

    @Override
    public void process(Listener listener) {
        listener.onJoin(this.player);
    }

    @Override
    public EventType<Listener> type() {
        return EventType.JOIN;
    }

    @HostAccess.Implementable
    public interface Listener extends EventListener {

        void onJoin(ScriptPlayer player);
    }
}

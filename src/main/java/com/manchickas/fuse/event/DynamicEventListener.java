package com.manchickas.fuse.event;

import com.manchickas.fuse.type.FunctionalType;
import com.manchickas.zet.type.Type;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

@HostAccess.Implementable
public interface DynamicEventListener extends EventListener {

    Type<DynamicEventListener, DynamicEventListener> TYPE = new FunctionalType<>(DynamicEventListener.class, "(...suggestions: any) => any");

    Value on(Value... args);
}

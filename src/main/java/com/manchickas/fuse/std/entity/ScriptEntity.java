package com.manchickas.fuse.std.entity;

import com.manchickas.fuse.std.ScriptWrapper;
import com.manchickas.fuse.std.util.ScriptPosition;
import net.minecraft.entity.Entity;
import org.graalvm.polyglot.HostAccess;

public class ScriptEntity<E extends Entity> extends ScriptWrapper<E> {

    protected ScriptEntity(E wrapped) {
        super(wrapped);
    }

    @HostAccess.Export
    public ScriptPosition position() {
        return ScriptPosition.wrap(this.wrapped.getPos());
    }
}

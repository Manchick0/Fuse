package com.manchickas.fuse.script.scheduler.task;

import com.manchickas.fuse.script.scheduler.Callback;
import net.minecraft.util.Identifier;

public final class LabeledTask extends Task {

    private final Identifier label;

    public LabeledTask(Identifier label, Callback callback, long target) {
        super(callback, target);
        this.label = label;
    }

    public Identifier label() {
        return this.label;
    }
}

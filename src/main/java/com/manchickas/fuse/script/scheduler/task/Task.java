package com.manchickas.fuse.script.scheduler.task;

import com.manchickas.fuse.script.scheduler.Callback;
import org.jetbrains.annotations.NotNull;

public class Task implements Comparable<Task> {

    private final Callback callback;
    private final long target;
    private boolean cancelled;

    public Task(Callback callback, long target) {
        this.callback = callback;
        this.cancelled = false;
        this.target = target;
    }

    public void run() {
        if (!this.cancelled)
            this.callback.run();
    }

    public boolean isDue(long tick) {
        return tick >= this.target;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public int compareTo(@NotNull Task o) {
        return Long.compare(this.target, o.target);
    }
}

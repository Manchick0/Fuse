package com.manchickas.fuse.script.scheduler;

import com.manchickas.fuse.script.scheduler.task.LabeledTask;
import com.manchickas.fuse.script.scheduler.task.Task;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

public final class Scheduler {

    private final PriorityQueue<Task> tasks;
    private final Map<Identifier, LabeledTask> labeled;
    private long relative;

    public Scheduler() {
        this.tasks = new PriorityQueue<>(Comparator.naturalOrder());
        this.labeled = new Object2ObjectOpenHashMap<>();
        this.relative = 0;
    }

    public void tick() {
        Task task;
        while ((task = this.tasks.peek()) != null && task.isDue(this.relative)) {
            task.run();
            if (task instanceof LabeledTask l)
                this.labeled.remove(l.label());
            this.tasks.poll();
        }
        this.relative++;
    }

    public void schedule(Callback callback, long delay) {
        var task = new Task(callback, delay);
        this.tasks.add(task);
    }

    public void unschedule(Identifier label) {
        var scheduled = this.labeled.remove(label);
        if (scheduled != null)
            scheduled.cancel();
    }

    public void schedule(Identifier identifier, Callback callback, long delay) {
        var task = new LabeledTask(identifier, callback, this.relative + delay);
        this.tasks.add(task);
    }
}

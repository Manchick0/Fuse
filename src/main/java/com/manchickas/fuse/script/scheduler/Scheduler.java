package com.manchickas.fuse.script.scheduler;

import com.manchickas.fuse.script.scheduler.task.LabeledTask;
import com.manchickas.fuse.script.scheduler.task.Task;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;

public final class Scheduler implements SuggestionProvider<ServerCommandSource> {

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

    public boolean unschedule(Identifier label) {
        var scheduled = this.labeled.remove(label);
        if (scheduled != null) {
            scheduled.cancel();
            return true;
        }
        return false;
    }

    public void schedule(Identifier identifier, Callback callback, long delay) {
        var task = new LabeledTask(identifier, callback, this.relative + delay);
        var previous = this.labeled.put(identifier, task);
        if (previous != null)
            previous.cancel();
        this.tasks.add(task);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestIdentifiers(this.labeled.keySet(), builder);
    }
}

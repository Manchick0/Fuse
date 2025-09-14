package com.manchickas.fuse.script;

import com.google.common.collect.ImmutableMap;
import com.manchickas.fuse.event.EventListener;
import com.manchickas.fuse.event.EventType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public final class ScriptLoader extends SinglePreparationResourceReloader<Map<Identifier, Source>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptLoader.class);
    private static final ResourceFinder FINDER = new ResourceFinder("script", ".js");
    private static final HostAccess ACCESS = HostAccess.newBuilder()
            .allowAccessAnnotatedBy(HostAccess.Export.class)
            .allowImplementationsAnnotatedBy(HostAccess.Implementable.class)
            .allowAccessInheritance(true)
            .allowArrayAccess(true)
            .allowListAccess(true)
            .allowMapAccess(true)
            .build();

    private final Map<Identifier, Context> scripts;
    private final Map<EventType<?>, Set<EventListener>> listeners;

    public ScriptLoader() {
        this.scripts = new Object2ObjectOpenHashMap<>();
        this.listeners = new Object2ObjectOpenHashMap<>();
    }

    @Override
    protected Map<Identifier, Source> prepare(ResourceManager manager, Profiler profiler) {
        var resources = FINDER.findResources(manager);
        var result = ImmutableMap.<Identifier, Source>builderWithExpectedSize(resources.size());
        for (var entry : resources.entrySet()) {
            var identifier = FINDER.toResourceId(entry.getKey());
            var resource = entry.getValue();
            try {
                var source = Source.newBuilder("js", resource.getReader(), identifier.toString())
                        .mimeType("application/javascript")
                        .cached(true)
                        .build();
                result.put(identifier, source);
            } catch (IOException e) {
                LOGGER.error("Failed to load script {}", identifier, e);
            }
        }
        return result.buildKeepingLast();
    }

    @Override
    protected void apply(Map<Identifier, Source> prepared, ResourceManager manager, Profiler profiler) {
        var builder = Context.newBuilder("js")
                .allowHostAccess(ScriptLoader.ACCESS)
                .allowIO(IOAccess.NONE)
                .out(OutputStream.nullOutputStream())
                .err(OutputStream.nullOutputStream())
                .in(InputStream.nullInputStream())
                .useSystemExit(false);
        this.clear();
        for (var entry : prepared.entrySet()) {
            var identifier = entry.getKey();
            var source = entry.getValue();
            try {
                var context = builder.build();
                this.scripts.put(identifier, context);
                context.getBindings("js")
                        .putMember("script", this);
                context.eval(source);
            } catch (Exception e) {
                LOGGER.error("Failed to evaluate script {}", identifier, e);
            }
        }
        LOGGER.info("Loaded {} scripts", this.scripts.size());
    }

    public void on(Value identifier, Value listener) {
    }

    private void clear() {
        var iterator = this.scripts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.getValue().close(true);
            iterator.remove();
        }
        this.listeners.clear();
    }
}

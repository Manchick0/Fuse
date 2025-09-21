package com.manchickas.fuse.script;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.manchickas.fuse.ArrayBuilder;
import com.manchickas.fuse.Fuse;
import com.manchickas.fuse.event.DynamicEventListener;
import com.manchickas.fuse.event.Event;
import com.manchickas.fuse.event.EventListener;
import com.manchickas.fuse.event.EventType;
import com.manchickas.fuse.exception.ExceptionTemplate;
import com.manchickas.fuse.exception.ScriptException;
import com.manchickas.fuse.script.property.Property;
import com.manchickas.fuse.script.property.operation.Operation;
import com.manchickas.fuse.std.util.ScriptIdentifier;
import com.manchickas.fuse.type.codec.JSOps;
import com.manchickas.marshal.command.CommandPath;
import com.manchickas.marshal.command.ScriptCommand;
import com.manchickas.optionated.option.Some;
import com.manchickas.zet.Zet;
import com.manchickas.zet.type.Type;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public final class ScriptLoader extends SinglePreparationResourceReloader<Map<Identifier, Source>> implements IdentifiableResourceReloadListener {

    private static final ExceptionTemplate CAUGHT_DURING_DISPATCH = ScriptException.template("Couldn't dispatch an event of type '{}' to one of its listeners. The listener will be removed from further dispatching until the next reload.");

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

    private static ScriptLoader instance;

    private final Map<Identifier, Context> scripts;
    private final Map<EventType<?>, Set<EventListener>> typedListeners;
    private final Map<Identifier, Set<DynamicEventListener>> dynamicListeners;
    private final Map<Identifier, Property> properties;
    private final Map<Identifier, NbtElement> stagedProperties;
    private final Map<CommandPath, ScriptCommand.Callback> commands;

    public ScriptLoader() {
        this.scripts = new Object2ObjectOpenHashMap<>();
        this.typedListeners = new Object2ObjectOpenHashMap<>();
        this.dynamicListeners = new Object2ObjectOpenHashMap<>();
        this.properties = new Object2ObjectOpenHashMap<>();
        this.stagedProperties = new Object2ObjectOpenHashMap<>();
        this.commands = new Object2ObjectOpenHashMap<>();
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildFuseCommand() {
        return CommandManager.literal("fuse")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("property")
                        .then(CommandManager.argument("name", IdentifierArgumentType.identifier())
                                .suggests((ctx, builder) -> CommandSource.suggestIdentifiers(this.properties.keySet(), builder))
                                .executes(ctx -> {
                                    var name = IdentifierArgumentType.getIdentifier(ctx, "name");
                                    var property = this.properties.get(name);
                                    var src = ctx.getSource();
                                    if (property != null) {
                                        var value = property.get();
                                        src.sendMessage(Text.literal("Property '%s' is currently set to '%s'".formatted(name,
                                                value.isHostObject() ? value.asHostObject().toString() : value.toString())));
                                        return 1;
                                    }
                                    src.sendError(Text.literal("Property '%s' is not declared".formatted(name)));
                                    return -1;
                                })
                                .then(CommandManager.argument("value", NbtElementArgumentType.nbtElement())
                                        .executes(ctx -> {
                                            var name = IdentifierArgumentType.getIdentifier(ctx, "name");
                                            var value = NbtOps.INSTANCE.convertTo(JSOps.getInstance(),
                                                    NbtElementArgumentType.getNbtElement(ctx, "value"));
                                            var property = this.properties.get(name);
                                            var src = ctx.getSource();
                                            if (property != null) {
                                                property.set(value);
                                                src.sendMessage(Text.literal("Property '%s' has been updated to '%s'".formatted(name,
                                                        value.isHostObject() ? value.asHostObject().toString() : value.toString())));
                                                return 1;
                                            }
                                            src.sendError(Text.literal("Property '%s' is not declared".formatted(name)));
                                            return -1;
                                        }))));
    }

    public static ScriptLoader getInstance() {
        if (ScriptLoader.instance == null) {
            synchronized (ScriptLoader.class) {
                ScriptLoader.instance = new ScriptLoader();
            }
        }
        return instance;
    }

    public <E extends Event<?>> E dispatch(E event) {
        var listeners = this.typedListeners.get(event.type());
        if (listeners != null) {
            var snapshot = ImmutableSet.copyOf(listeners);
            for (var listener : snapshot) {
                try {
                    event.tryProcess(listener);
                } catch (PolyglotException e) {
                    if (e.isHostException() && e.asHostException() instanceof Error err)
                        throw err;
                    LOGGER.error(CAUGHT_DURING_DISPATCH.message(EventType.REGISTRY.getId(event.type())));
                    LOGGER.error(e.getMessage());
                    listeners.remove(listener);
                }
            }
        }
        return event;
    }

    public void updateCommandTree(MinecraftServer server) {
        var manager = server.getPlayerManager();
        var dispatcher = server.getCommandManager()
                .getDispatcher();
        for (var entry : this.commands.entrySet()) {
            var path = entry.getKey();
            var callback = entry.getValue();
            path.register(dispatcher, callback);
        }
        if (manager != null)
            manager.getPlayerList().forEach(manager::sendCommandTree);
        LOGGER.info("Registered '{}' commands.", this.commands.size());
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
                LOGGER.error("Couldn't load script '{}'. It will be ignored until the next reload.", identifier, e);
            }
        }
        return result.buildKeepingLast();
    }

    @Override
    protected void apply(Map<Identifier, Source> prepared, ResourceManager manager, Profiler profiler) {
        var builder = Context.newBuilder("js")
                // Some people find Python fast enough,
                // let them use their HotSpotVM
                .option("engine.WarnInterpreterOnly", "false")
                .allowHostAccess(ScriptLoader.ACCESS)
                .allowIO(IOAccess.NONE)
                .out(OutputStream.nullOutputStream())
                .err(OutputStream.nullOutputStream())
                .in(InputStream.nullInputStream())
                .useSystemExit(false);
        this.clear(true);
        var entrypoint = new Entrypoint();
        for (var entry : prepared.entrySet()) {
            var identifier = entry.getKey();
            var source = entry.getValue();
            try {
                var context = builder.build();
                this.scripts.put(identifier, context);
                context.getBindings("js")
                        .putMember("script", entrypoint);
                context.eval(source);
            } catch (Exception e) {
                LOGGER.error("Couldn't evaluate script '{}'. It will be ignored until the next reload.", identifier, e);
            }
        }
        LOGGER.info("Successfully evaluated {} scripts.", this.scripts.size());
        var total = this.typedListeners.size() + this.dynamicListeners.size();
        LOGGER.info("Registered {} event {}. ({}/{})",
                total == 1 ? "an" : total,
                total == 1 ? "listener" : "listeners",
                this.typedListeners.size(),
                this.dynamicListeners.size());
    }

    private void clear(boolean stageProperties) {
        this.typedListeners.clear();
        this.dynamicListeners.clear();
        this.stagedProperties.clear();
        var piterator = this.properties.entrySet().iterator();
        while (piterator.hasNext()) {
            var entry = piterator.next();
            if (stageProperties) {
                var identifier = entry.getKey();
                var property = entry.getValue();
                try {
                    this.stagedProperties.put(identifier, property.serialize());
                } catch (ScriptException e) {
                    LOGGER.error(e.getMessage());
                }
            }
            piterator.remove();
        }
        var iterator = this.scripts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.getValue().close(true);
            iterator.remove();
        }
    }

    @Override
    public Identifier getFabricId() {
        return Fuse.withPath("script");
    }

    public class Entrypoint {

        private Entrypoint() {
        }

        @HostAccess.Export
        public Value[] dispatch(Value identifier, Value... args) {
            var _identifier = Zet.expect(ScriptIdentifier.VANILLA_TYPE, identifier);
            var listeners = ScriptLoader.this.dynamicListeners.get(_identifier);
            if (listeners != null) {
                var snapshot = ImmutableSet.copyOf(listeners);
                var result = ArrayBuilder.<Value>builder(snapshot.size());
                for (var listener : snapshot) {
                    try {
                        result.append(listener.on(args));
                    } catch (PolyglotException e) {
                        if (e.isHostException() && e.asHostException() instanceof Error err)
                            throw err;
                        LOGGER.error(CAUGHT_DURING_DISPATCH.message(_identifier));
                        LOGGER.error(e.getMessage());
                        listeners.remove(listener);
                    }
                }
                return result.build(Value[]::new);
            }
            return new Value[0];
        }

        @HostAccess.Export
        public void on(Value identifier, Value listener) {
            var result = EventType.TYPE.parse(identifier);
            if (result instanceof Some<EventType<?>>(var type)) {
                var _listener = Zet.expect(type.type(), listener);
                ScriptLoader.this.typedListeners.computeIfAbsent(type, __ -> new ObjectOpenHashSet<>())
                        .add(_listener);
                return;
            }
            var _identifier = Zet.expect(ScriptIdentifier.VANILLA_TYPE, identifier);
            var _listener = Zet.expect(DynamicEventListener.TYPE, listener);
            ScriptLoader.this.dynamicListeners.computeIfAbsent(_identifier, __ -> new ObjectOpenHashSet<>())
                    .add(_listener);
        }

        @HostAccess.Export
        public Operation[] onProperty(Value identifier, Value initial) {
            var _identifier = Zet.expect(ScriptIdentifier.VANILLA_TYPE, identifier);
            var property = ScriptLoader.this.properties.get(_identifier);
            if (property == null) {
                var staged = ScriptLoader.this.stagedProperties.get(_identifier);
                if (staged != null) {
                    try {
                        var _property = new Property(_identifier, Property.deserialize(_identifier, staged));
                        ScriptLoader.this.properties.put(_identifier, _property);
                        return _property.operations();
                    } catch (ScriptException e) {
                        LOGGER.error("Couldn't deserialize property '{}'. Defaulting to the initial value of '{}'...", _identifier, initial);
                        LOGGER.error(e.getMessage());
                    }
                }
                var _property = new Property(_identifier, initial);
                ScriptLoader.this.properties.put(_identifier, _property);
                return _property.operations();
            }
            return property.operations();
        }

        @HostAccess.Export
        public void onCommand(Value path, Value callback) {
            var _path = Zet.expect(Type.STRING, path);
            var _callback = Zet.expect(ScriptCommand.Callback.TYPE, callback);
            try {
                var compiled = CommandPath.compile(_path);
                ScriptLoader.this.commands.put(compiled, _callback);
            } catch (ScriptException e) {
                LOGGER.error("Couldn't compile command path '{}'.", _path);
                for (var line : e.getMessage().split("\n"))
                    LOGGER.error("  {}", line);
            }
        }

        @HostAccess.Export
        public void off(Value identifier, Value listener) {
            var result = EventType.TYPE.parse(identifier);
            if (result instanceof Some<EventType<?>>(var type)) {
                var _listener = Zet.expect(type.type(), listener);
                var listeners = ScriptLoader.this.typedListeners.get(type);
                if (listeners != null)
                    listeners.remove(_listener);
                return;
            }
            var _identifier = Zet.expect(ScriptIdentifier.VANILLA_TYPE, identifier);
            var _listener = Zet.expect(DynamicEventListener.TYPE, listener);
            var listeners = ScriptLoader.this.dynamicListeners.get(_identifier);
            if (listeners != null)
                listeners.remove(_listener);
        }
    }
}

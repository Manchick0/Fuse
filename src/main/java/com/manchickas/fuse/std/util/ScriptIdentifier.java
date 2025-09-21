package com.manchickas.fuse.std.util;

import com.manchickas.optionated.option.Option;
import com.manchickas.source.StringReader;
import com.manchickas.zet.type.Type;
import net.minecraft.util.Identifier;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.NotNull;

public record ScriptIdentifier(@HostAccess.Export String namespace, @HostAccess.Export String path) {

    public static final Type<ScriptIdentifier, ScriptIdentifier> TYPE = Type.union(
            Type.STRING.bind(ScriptIdentifier::compile, ScriptIdentifier::toString),
            Type.direct(ScriptIdentifier.class)
    );
    public static final Type<Identifier, Identifier> VANILLA_TYPE = ScriptIdentifier.TYPE.fmap(
            ScriptIdentifier::toVanilla,
            vanilla -> new ScriptIdentifier(vanilla.getNamespace(), vanilla.getPath())
    );

    public static Option<ScriptIdentifier> compile(String source) {
        var reader = new StringReader(source);
        var buffer = new StringBuilder();
        while (reader.canRead()) {
            var c = reader.read();
            if (c == ':')
                return ScriptIdentifier.readPath(buffer.toString(), reader);
            if (c == '/')
                break;
            if (ScriptIdentifier.isValidCharacter(c)) {
                buffer.appendCodePoint(c);
                continue;
            }
            return Option.none();
        }
        return ScriptIdentifier.readPath("minecraft", new StringReader(source));
    }

    private static Option<ScriptIdentifier> readPath(String namespace, StringReader reader) {
        var path = new StringBuilder();
        while (reader.canRead()) {
            var c = reader.read();
            if (ScriptIdentifier.isValidCharacter(c) || c == '/') {
                path.appendCodePoint(c);
                continue;
            }
            return Option.none();
        }
        return Option.some(new ScriptIdentifier(namespace, path.toString()));
    }

    private static boolean isValidCharacter(int c) {
        return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '-' || c == '_';
    }

    @Override
    public @NotNull String toString() {
        return this.namespace + ":" + this.path;
    }

    public Identifier toVanilla() {
        return Identifier.of(this.namespace, this.path);
    }
}

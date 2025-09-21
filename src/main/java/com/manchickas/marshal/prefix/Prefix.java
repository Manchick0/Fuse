package com.manchickas.marshal.prefix;

import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

@FunctionalInterface
public interface Prefix extends Predicate<ServerCommandSource> {

    Prefix ASTERISK = new Prefix() {

        @Override
        public boolean permits(ServerCommandSource source) {
            return source.hasPermissionLevel(2);
        }

        @Override
        public String toString() {
            return "*";
        }
    };

    static Prefix name(String name) {
        return new Prefix() {

            @Override
            public boolean permits(ServerCommandSource source) {
                return source.getName().equals(name);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    boolean permits(ServerCommandSource source);

    @Override
    default boolean test(ServerCommandSource serverCommandSource) {
        return this.permits(serverCommandSource);
    }
}

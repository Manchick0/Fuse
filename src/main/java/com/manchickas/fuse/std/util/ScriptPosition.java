package com.manchickas.fuse.std.util;

import com.manchickas.optionated.option.Some;
import com.manchickas.zet.Zet;
import com.manchickas.zet.type.Associative;
import com.manchickas.zet.type.Type;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

public final class ScriptPosition implements Associative<ScriptPosition> {

    private static final Type<ScriptPosition, ScriptPosition> TYPE = Type.union(
            Type.tuple(
                    Type.DOUBLE.element(pos -> pos.x),
                    Type.DOUBLE.element(pos -> pos.y),
                    Type.DOUBLE.element(pos -> pos.z),
                    ScriptPosition::new
            ),
            Type.direct(ScriptPosition.class)
    );
    public static final Type<Direction, Direction> DIRECTION_TYPE = Type.union(
            Type.literal("north").fmap(__ -> Direction.NORTH, __ -> "north"),
            Type.literal("south").fmap(__ -> Direction.SOUTH, __ -> "south"),
            Type.literal("west").fmap(__ -> Direction.WEST, __ -> "west"),
            Type.literal("east").fmap(__ -> Direction.EAST, __ -> "east"),
            Type.literal("up").fmap(__ -> Direction.UP, __ -> "up"),
            Type.literal("down").fmap(__ -> Direction.DOWN, __ -> "down")
    );
    public static final Type<Double[], Double[]> TWO_TUPLE = Type.tuple(
            Type.DOUBLE.element(tuple -> tuple[0]),
            Type.DOUBLE.element(tuple -> tuple[1]),
            (a, b) -> new Double[]{a, b}
    );

    public final @HostAccess.Export double x;
    public final @HostAccess.Export double y;
    public final @HostAccess.Export double z;

    private ScriptPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static ScriptPosition wrap(double x, double y, double z) {
        return new ScriptPosition(x, y, z);
    }

    public static ScriptPosition wrap(Vec3i vec3i) {
        return new ScriptPosition(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static ScriptPosition wrap(Vec3d vec3d) {
        return new ScriptPosition(vec3d.x, vec3d.y, vec3d.z);
    }

    @HostAccess.Export
    public ScriptPosition add(Value other) {
        var position = Zet.expect(TYPE, other);
        return new ScriptPosition(this.x + position.x, this.y + position.y, this.z + position.z);
    }

    @HostAccess.Export
    public ScriptPosition multiply(Value other) {
        if (TYPE.parse(other) instanceof Some<ScriptPosition>(var position))
            return new ScriptPosition(this.x * position.x, this.y * position.y, this.z * position.z);
        var factor = Zet.expect(Type.DOUBLE, other);
        return new ScriptPosition(this.x * factor, this.y * factor, this.z * factor);
    }

    @HostAccess.Export
    public ScriptPosition offset(Value direction, Value distance) {
        var _distance = Zet.expect(Type.DOUBLE, distance);
        if (ScriptPosition.DIRECTION_TYPE.parse(direction) instanceof Some<Direction>(var _direction))
            return new ScriptPosition(
                    this.x + _direction.getOffsetX() * _distance,
                    this.y + _direction.getOffsetY() * _distance,
                    this.z + _direction.getOffsetZ() * _distance
            );
        var spherical = Zet.expect(TWO_TUPLE, direction);
        var pitch = (spherical[0] + 90) * Math.PI;
        var yaw = Math.toRadians(spherical[1] + 90);
        return new ScriptPosition(
                this.x + Math.sin(pitch) * Math.cos(yaw) * _distance,
                this.y + Math.cos(pitch) * _distance,
                this.z + Math.sin(pitch) * Math.sin(yaw) * _distance
        );
    }

    @HostAccess.Export
    public ScriptPosition floor() {
        return new ScriptPosition(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    @HostAccess.Export
    public ScriptPosition center() {
        return new ScriptPosition(Math.floor(this.x) + 0.5, Math.floor(this.y) + 0.5, Math.floor(this.z) + 0.5);
    }

    @HostAccess.Export
    public ScriptPosition ceil() {
        return new ScriptPosition(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.z));
    }

    @Override
    @HostAccess.Export
    public String toString() {
        return String.format("[%.3f, %.3f, %.3f]", this.x, this.y, this.z);
    }

    @Override
    public @NotNull Type<ScriptPosition, ScriptPosition> type() {
        return Type.direct(ScriptPosition.class);
    }
}

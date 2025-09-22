import { Serializable, Serialize } from "./util";

export abstract class Position implements Serializable {
    x: number;
    y: number;
    z: number;

    private constructor(x: number, y: number, z: number) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    abstract up(): Position;
    abstract down(): Position;
    abstract north(): Position;
    abstract west(): Position;
    abstract south(): Position;
    abstract east(): Position;

    abstract offset(other: Position): Position;
    abstract offset(direction: [number, number], distance: number): Position;
    abstract multiply(other: Position): Position;
    abstract multiply(other: number): Position;
    abstract negate(): Position;

    abstract align(): Position;
    abstract center(): Position;

    abstract squaredDistance(other: Position): number;
    abstract distance(other: Position): number;

    abstract [Serialize](): never;
}

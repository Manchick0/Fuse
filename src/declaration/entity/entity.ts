import { Position } from "../util/position";
import { Text, TextComponent } from "../util/typography";

export abstract class Entity {
    protected constructor() {}

    abstract position(): Position;
    abstract rotation(): [number, number];

    abstract noAI(): boolean;
    abstract noAI(noAI: boolean): void;

    abstract name(): TextComponent;
    abstract name(name: Text): void;
    abstract name(name: Text, visible: boolean): void;
}

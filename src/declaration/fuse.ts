import { Player } from "./entity/player";
import { CommandContext, Path } from "./marshal/command-context";
import { Identifier } from "./util/identifier";
import { Position } from "./util/position";
import { Serializable } from "./util/util";

export declare const script: Entrypoint;

export abstract class Entrypoint {
    private constructor() {}

    abstract on<T extends keyof EventListeners>(
        event: T,
        listener: EventListeners[T]
    ): this;
    abstract onCommand<CommandPath extends string>(
        command: Path<CommandPath>,
        callback: (ctx: CommandContext) => number | undefined | void
    ): this;
    abstract onProperty<T extends PrimitiveSerializable>(
        identifier: string | Identifier,
        initial: T
    ): [() => T, (value: T | ((old: T) => T)) => void];

    /**
     * Schedules an **anonymous** callback to be invoked after the provided amount of ticks.
     *
     * If `delay` is less or equal to `0`, the callback is invoked directly, hence
     * calling `schedule(fn, ..0)` is equivalent to `fn()`.
     *
     * @param callback the callback to invoke after `delay` ticks.
     * @param delay the delay in ticks before invoking the callback.
     */
    abstract schedule(callback: () => void, delay: number): void;
    /**
     * Schedules a **labeled** callback to be invoked after the provided amount of ticks.
     *
     * If `delay` is less or equal to `0`, the callback is invoked directly, hence
     * calling `schedule(fn, ..0, label)` is equivalent to `fn()`.
     *
     * If a callback with the same label is already scheduled, it is replaced by the new one.
     *
     * @param callback the callback to invoke after `delay` ticks.
     * @param delay the delay in ticks before invoking the callback.
     * @param label the label of the scheduled callback.
     * @returns the number of ticks the previous callback had left.
     */
    abstract schedule(
        callback: () => void,
        delay: number,
        label: string | Identifier
    ): number | undefined;
    abstract unschedule(label: string | Identifier): number | undefined;

    abstract useIdentifier(identifier: string | Identifier): Identifier;
    abstract usePosition(
        position: [number, number, number] | Position
    ): Position;
}

type EventListeners = {
    join: (player: Player) => void;
};

type PrimitiveSerializable =
    | string
    | number
    | boolean
    | null
    | undefined
    | Serializable
    | PrimitiveSerializable[]
    | { [key: string]: PrimitiveSerializable };

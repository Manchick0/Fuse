import { Text } from "../util/typography";

export abstract class CommandContext {
    private constructor() {}

    abstract arg(name: string): any;
    abstract args(): { [key: string]: any };

    abstract sendMessage(message: Text): void;
    abstract sendError(message: Text): void;
}

export type Path<
    Source extends string,
    AllowPrefixes extends boolean = true,
    AllowArgs extends boolean = false,
    Rest extends string = Source,
    Seg extends string = ""
> = Rest extends `${infer Head}${infer Tail}`
    ? Tail extends ""
        ? Segment<`${Seg}${Head}`, AllowPrefixes, AllowArgs> extends never
            ? never
            : Source
        : Head extends "/"
        ? Segment<Seg, AllowPrefixes, AllowArgs> extends never
            ? never
            : Path<Source, false, true, Tail, "">
        : Path<Source, AllowPrefixes, AllowArgs, Tail, `${Seg}${Head}`>
    : never;

type Segment<
    Source extends string,
    AllowPrefixes extends boolean,
    AllowArgs extends boolean,
    Name extends string = "",
    Rest extends string = Source
> = Rest extends `${infer Head}${infer Tail}`
    ? Head extends ":"
        ? TrimLeading<Name> extends ""
            ? never
            : AllowArgs extends true
            ? TrimLeading<Tail> extends
                  | NumberArgument
                  | StringArgument
                  | BooleanArgument
                  | PositionArgument
                  | EntityArgumentType
                ? Source
                : never
            : never
        : Head extends "@"
        ? Name extends ""
            ? AllowPrefixes extends true
                ? Tail extends "*" | ">_"
                    ? Source
                    : never
                : never
            : never
        : Head extends LowercaseLetter | UppercaseLetter | Digit | "_" | "-"
        ? Tail extends ""
            ? Source
            : Segment<Source, AllowPrefixes, AllowArgs, `${Name}${Head}`, Tail>
        : never
    : Source;

type TrimLeading<T extends string> = T extends `${infer Head}${infer Tail}`
    ? Head extends " " | "\t" | "\n" | "\r"
        ? TrimLeading<Tail>
        : T
    : "";

type NumberArgument =
    | "number"
    | "number()"
    | `number(${number}..${number})`
    | `number(${number}..)`
    | `number(..${number})`;
type StringArgument = "string" | "string()" | `string(${string})`;
type BooleanArgument = "boolean" | "boolean()";
type PositionArgument =
    | "position"
    | "position()"
    | "position(aligned)"
    | "position(direct)";
type EntityArgumentType =
    | "entity"
    | "entity()"
    | "entity(all)"
    | "entity(player)";

type LowercaseLetter =
    | "a"
    | "b"
    | "c"
    | "d"
    | "e"
    | "f"
    | "g"
    | "h"
    | "i"
    | "j"
    | "k"
    | "l"
    | "m"
    | "n"
    | "o"
    | "p"
    | "q"
    | "r"
    | "s"
    | "t"
    | "u"
    | "v"
    | "w"
    | "x"
    | "y"
    | "z";
type Digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9";
type UppercaseLetter = Uppercase<LowercaseLetter>;

export type Range<
    Lower extends number,
    Higher extends number,
    Inclusive extends boolean = false,
    Accumulator extends number[] = []
> = Accumulator["length"] extends Higher
    ? Lower extends 0
        ? Inclusive extends true
            ? Accumulator[number] | Accumulator["length"]
            : Accumulator[number]
        : Exclude<
              Inclusive extends true
                  ? Accumulator[number] | Accumulator["length"]
                  : Accumulator[number],
              Range<0, Lower>
          >
    : Range<Lower, Higher, Inclusive, [...Accumulator, Accumulator["length"]]>;

export declare const Serialize: unique symbol;

export interface Serializable {
    [Serialize](): never;
}

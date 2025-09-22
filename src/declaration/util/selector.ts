type Selector = {
    type: "*" | (string & {});
    tag?:
        | {
              type: "any" | "all";
              entries: ("!" | (string & {}))[];
          }
        | ("!" | (string & {}))[]
        | ("!" | (string & {}));
    team?:
        | {
              type: "any" | "all";
              entries: ("!" | (string & {}))[];
          }
        | ("!" | (string & {}))[]
        | ("!" | (string & {}));
    distance?: [number, number] | [undefined, number] | [number, undefined];
    selection?: {
        sort?: "unsorted" | "nearest" | "furthest" | "random";
        limit?: number;
    };
};

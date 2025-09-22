import { Identifier } from "../util/identifier";
import { DimensionType, World } from "./world";

export abstract class Server {

    private constructor() {
    }

    abstract world(identifier: DimensionType | Identifier): World;
}
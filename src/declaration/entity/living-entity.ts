import { Entity } from "./entity";

export abstract class LivingEntity extends Entity {

    protected constructor() {
        super();
    }

    abstract health(): number;
    abstract health(value: number): void;
}
import { Item, ItemDefinition } from "../item/item";
import { Text } from "../util/typography";
import { LivingEntity } from "./living-entity";

export abstract class Player extends LivingEntity {
    private constructor() {
        super();
    }

    abstract sendMessage(message: Text): void;
    abstract offer(item: ItemDefinition | Item): void;

    abstract itemAt(slot: string): Item;
}

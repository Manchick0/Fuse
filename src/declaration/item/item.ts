import { DimensionType } from "../server/world";
import { Text, TextComponent } from "../util/typography";
import { Range } from "../util/util";

export abstract class Item {
    private constructor() {}

    abstract name(): TextComponent;

    abstract count(): number;
    abstract count(value: number): void;

    abstract increment(): this;
    abstract decrement(): this;
}

export type ItemDefinition = {
    id: string;
    count: Range<1, 99, true>;
    components?: {
        damage?: number;
        damage_resistant?: `#${string}`;
        death_protection?: {
            death_effects?: ConsumeEffect[];
        };
        debug_stick_state?: {
            [key: string]: string;
        };
        dyed_color?: number | [number, number, number];
        enchantable?: {
            value: number;
        };
        enchantment_glint_override?: boolean;
        enchantments?: {
            [key: string]: number;
        };
        entity_data?: {
            [key: string]: any;
        };
        equippable?: {
            slot:
                | "head"
                | "chest"
                | "legs"
                | "feet"
                | "body"
                | "mainhand"
                | "offhand"
                | "saddle";
            equip_sound?: string;
            asset_id?: string;
            allowed_entities?: string | `#${string}` | string[];
            dispensable?: boolean;
            swappable?: boolean;
            damage_on_hurt?: boolean;
            equip_on_interact?: boolean;
            camera_overlay?: string;
            can_be_sheared?: boolean;
            shearing_sound?: string;
        };
        firework_explosion?: FireworkExplosion;
        fireworks?: {
            explosions?: FireworkExplosion[];
            flight_duration?: number;
        };
        food?: {
            nutrition: number;
            saturation: number;
            can_always_eat?: boolean;
        };
        glider?: {};
        instrument?:
            | string
            | {
                  description: Text;
                  sound_event: string;
                  use_duration: number;
                  range: number;
              };
        intangible_projectile?: {};
        item_model?: string;
        item_name?: Text;
        jukebox_playable?: string;
        lock?: {
            [key: string]: any;
        };
        lodestone_tracker?: {
            target?: {
                pos: [number, number, number];
                dimension: DimensionType;
            };
            tracked?: boolean;
        };
        lore?: Text[];
        map_color?: number;
        map_decorations?: {
            [key: string]: {
                type:
                    | "player"
                    | "frame"
                    | "red_marker"
                    | "blue_marker"
                    | "target_x"
                    | "target_point"
                    | "player_off_map"
                    | "player_off_limits"
                    | "mansion"
                    | "monument"
                    | "banner_white"
                    | "banner_orange"
                    | "banner_magenta"
                    | "banner_light_blue"
                    | "banner_yellow"
                    | "banner_lime"
                    | "banner_pink"
                    | "banner_gray"
                    | "banner_light_gray"
                    | "banner_cyan"
                    | "banner_purple"
                    | "banner_blue"
                    | "banner_brown"
                    | "banner_green"
                    | "banner_red"
                    | "banner_black"
                    | "red_x"
                    | "village_desert"
                    | "village_plains"
                    | "village_savanna"
                    | "village_snowy"
                    | "village_taiga"
                    | "jungle_temple"
                    | "swamp_hut";
                x: number;
                y: number;
                rotation: number;
            };
        };
        map_id?: number;
        max_damage?: number;
        max_stack_size?: Range<1, 99, true>;
    };
};

type ConsumeEffect =
    | {
          type: "apply_effects";
          effects: {
              id: string;
              amplifier?: number;
              duration?: number;
              ambient?: boolean;
              show_particles?: boolean;
              show_icon?: boolean;
          }[];
          probability?: number;
      }
    | {
          type: "remove_effects";
          effcts: string | string[];
      }
    | {
          type: "clear_all_effects";
      }
    | {
          type: "teleport_randomly";
          diameter: 16;
      }
    | {
          type: "play_sound";
          sound: string;
      };

type FireworkExplosion = {
    shape: "small_ball" | "large_ball" | "star" | "creeper" | "burst";
    colors?: number[];
    fade_colors?: number[];
    has_trail?: boolean;
    has_twinkle?: boolean;
};

import { ItemDefinition } from "../item/item";

export type Text = string | TextComponent | (string | TextComponent)[];

export type TextComponent = (
    | PlainTextComponent
    | TranslatableTextComponent
    | ScoreboardTextComponent
    | SelectorTextComponent
    | KeybindTextComponent
    | NBTTextComponent
) & {
    color?:
        | `#${string}`
        | "black"
        | "dark_blue"
        | "dark_green"
        | "dark_aqua"
        | "dark_red"
        | "dark_purple"
        | "gold"
        | "gray"
        | "dark_gray"
        | "blue"
        | "green"
        | "aqua"
        | "red"
        | "light_purple"
        | "yellow"
        | "white";
    font?: string;
    bold?: boolean;
    italic?: boolean;
    underlined?: boolean;
    strikethrough?: boolean;
    obfuscated?: boolean;
    shadow_color?: number | [number, number, number, number];
    insertion?: string;
    click_event?: ClickEvent;
    hover_event?: HoverEvent;
    extra?: TextComponent[];
};

type PlainTextComponent = {
    type: "text";
    text: string;
};

type TranslatableTextComponent = {
    type: "translatable";
    translate: string;
    fallback?: string;
    with?: TextComponent[];
};

type ScoreboardTextComponent = {
    type: "score";
    score: {
        name: string;
        objective: string;
    };
};

type SelectorTextComponent = {
    type: "selector";
    selector: string;
    separator?: TextComponent;
};

type KeybindTextComponent = {
    type: "keybind";
    keybind: string;
};

type NBTTextComponent = {
    type: "nbt";
    nbt: string;
    interpret?: boolean;
} & (
    | {
          source: "block";
          block: string;
      }
    | {
          source: "entity";
          entity: string;
      }
    | {
          source: "storage";
          storage: string;
      }
);

type ClickEvent =
    | {
          action: "open_url";
          url: string;
      }
    | {
          action: "run_command";
          command: string;
      }
    | {
          action: "suggest_command";
          command: string;
      }
    | {
          action: "change_page";
          page: number;
      }
    | {
          action: "copy_to_clipboard";
          value: string;
      }
    | {
          action: "show_dialog";
          dialog: string;
      }
    | {
          action: "custom";
          id: string;
          playload?: string;
      };

type HoverEvent =
    | {
          action: "show_text";
          contents: Text;
      }
    | ({ action: "show_item" } & ItemDefinition)
    | {
          action: "show_entity";
          id: string;
          uuid:
              | `${string}-${string}-${string}-${string}-${string}`
              | [number, number, number, number];
          name?: Text;
      };

export abstract class Identifier {

    /**
     * The namespace of the identifier.
     */
    namespace: string;
    /**
     * The path segments of the identifier, separated by a forward slash (`/`).
     */
    segments: string[];
    /**
     * The path of the identifier.
     */
    path: string;

    private constructor(namespace: string, segments: string[]) {
        this.namespace = namespace;
        this.segments = segments;
        this.path = segments.join('/');
    }

    /**
     * Returns the string representation of the identifier in the form of `namespace:full/path`.
     * 
     * @returns the string representation of the identifier.
     */
    toString(): string {
        return `${this.namespace}:${this.path}`;
    }
}
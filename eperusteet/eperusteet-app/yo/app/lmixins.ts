declare namespace _ {
    interface OwnMixins {
        callAndGive<F>(x: F, ...args: any[]): F;
        print(x: any): any;
        spy<T>(x: T): T;
        print(): any;
        matchStrings(search: string, target: string): boolean;
        fromPairs(x: Array<any>): any;
        append<T>(x: Array<T>, el: T): Array<T>;
        overwrite(to: Object, from: Object): void;
        overwriteData(to: Object, from: Object): void;
        setRemove(from: Object, what: Object): void;
        cset<O, P, V>(o: O, p: P): (v: V) => void;
        fromPairs(): any;
        flattenBy<T>(root: T, field: string): T[];
        flattenTree(root: any, extractor: Function): any;
        zipBy<T>(root: T[], field: any): any[];
    }

    interface LoDashStatic extends OwnMixins {}
    interface LoDashImplicitArrayWrapper<T> extends OwnMixins {}
    interface LoDashImplicitObjectWrapper<T> extends OwnMixins {}
}

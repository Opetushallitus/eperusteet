import * as _ from "lodash";
import { getComponent, inject, getOfType, testModule, mockApp } from "app/testutils";

describe("Algoritmit", () => {
    let Algoritmit: any;

    beforeEach(mockApp);

    beforeEach(async () => {
        Algoritmit = await getComponent("Algoritmit");
    });

    test("Can be injected", () => expect(Algoritmit).toBeTruthy());

    test("match", () => {
        const match = Algoritmit.match;
        expect(match("hello", "Hello World")).toBeTruthy();
        expect(match("", "Hello World")).toBeTruthy();
        expect(match("xyz", "Hello World")).toBeFalsy();
    });
});

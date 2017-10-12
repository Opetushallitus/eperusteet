import * as _ from "lodash";
import { setInput, compiled, getComponent, inject, getOfType, testDirective, testModule, mockApp } from "../../testutils";


describe("Kommentit", () => {
    let Kommentit: any;

    beforeEach(async () => {
        mockApp();
        Kommentit = await getComponent("Kommentit");
    });

    test("Can be injected", () => expect(Kommentit).toBeTruthy());

});

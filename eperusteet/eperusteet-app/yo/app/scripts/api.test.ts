import * as _ from "lodash";
import { getComponent, inject, getOfType, testDirective, testModule, mockApp } from "app/testutils";

describe("Api", () => {
    let Api: any;

    beforeEach(async () => {
        mockApp();
        Api = await getComponent("Api");
    });

    test("Can be injected", () => expect(Api).toBeTruthy());
});

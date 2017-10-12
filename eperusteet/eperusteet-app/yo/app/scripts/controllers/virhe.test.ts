import * as _ from "lodash";
import { getComponent, inject, getOfType, testDirective, testModule, mockApp } from "app/testutils";

describe("virheService", () => {
    let virheService: any;

    beforeEach(async () => {
        mockApp();
        virheService = await getComponent("virheService");
    });

    test("Can be injected", () => expect(virheService).toBeTruthy());

    test("Virheen lähettäminen", () => {
        virheService.virhe("foobar");
    });
});

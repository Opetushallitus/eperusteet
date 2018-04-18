import {
    compiled,
    mockApp
} from "../../testutils";

describe("tiedotteet", () => {

    beforeEach(async () => {
        mockApp();
    });

    // EP-1390
    test("TiedoteViewControllerTest", async () => {
        await compiled(`<div ng-controller="TiedoteViewController"></div>`);
    });

});


import * as _ from "lodash";
import { getComponent, inject, getOfType, testDirective, testModule, mockApp } from "app/testutils";


describe("PerusteenRakenne", () => {
    let PerusteenRakenne: any;

    beforeEach(async () => {
        mockApp();
        PerusteenRakenne = await getComponent("PerusteenRakenne");
    });

    test("Can be injected", () => expect(PerusteenRakenne).toBeTruthy());

});


describe("SuoritustavanSisalto", () => {
    let SuoritustavanSisalto: any;

    beforeEach(async () => {
        mockApp();
        SuoritustavanSisalto = await getComponent("SuoritustavanSisalto");
    });

    test("Can be injected", () => expect(SuoritustavanSisalto).toBeTruthy());

});

// FIXME: https://github.com/react-community/create-react-native-app/issues/51

import { getComponent, inject, getOfType, testModule, mockApp } from "app/testutils";
import * as _ from "lodash";

describe("app", () => {
    beforeEach(mockApp);

    test("Injecting services", () => {
        getOfType("service").forEach(testModule);
    });

    test("Injecting factories", () => {
        getOfType("factory").forEach(testModule);
    });

    test("Injecting filters", () => {
        getOfType("filter").forEach(testModule);
    });

    test("Injecting states", () => {
        getOfType("state").forEach(testModule);
    });

    test("Modules can be injected with getComponent", async () => {
        const $rootScope = await getComponent("$rootScope");
        expect($rootScope).toBeTruthy();
    });

    test("All modules can be injected with inject", () => {
        inject($rootScope => expect($rootScope).toBeTruthy());
    });

});

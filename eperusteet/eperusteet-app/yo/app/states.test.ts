import * as _ from "lodash";
import { getComponent, inject, getOfType, testDirective, testModule, mockApp } from "app/testutils";

describe("$state", () => {
    let $state: any;
    let $stateParams: any;
    let $rootScope: any;
    let $timeout: any;

    beforeEach(async () => {
        mockApp();
        $state = await getComponent("$state");
        $stateParams = await getComponent("$stateParams");
        $rootScope = await getComponent("$rootScope");
        $timeout = await getComponent("$timeout");
    });

    test("Can be injected", () => expect($state).toBeTruthy());

    test("State root.aloitussivu", async () => {
        let visited = false;
        $rootScope.$on("$stateChangeSuccess", () => { visited = true; });
        $state.go("root.aloitussivu", { lang: "fi" });
        $timeout.flush();
        expect($state.current.name).toEqual("root.aloitussivu");
        expect($stateParams.lang).toEqual("fi");
        expect(visited).toEqual(true);
    });
});

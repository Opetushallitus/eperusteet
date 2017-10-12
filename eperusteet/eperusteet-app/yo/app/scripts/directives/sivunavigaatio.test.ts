import * as _ from "lodash";
import { setInput, compiled, getComponent, inject, getOfType, testDirective, testModule, mockApp } from "../../testutils";

describe("sivunavigaatio", () => {
    let $rootScope: any;

    beforeEach(async () => {
        mockApp();
        $rootScope = await getComponent("$rootScope");
    });

    test("sivunavigaatio", async () => {
        let [el, $scope] = await compiled(`<sivunavigaatio items="navi"></sivunavigaatio>`);
        $scope.navi = [{
        }];
        expect(_.isString(el.html()) && _.size(el.html()) > 100).toBeTruthy();
    });

});

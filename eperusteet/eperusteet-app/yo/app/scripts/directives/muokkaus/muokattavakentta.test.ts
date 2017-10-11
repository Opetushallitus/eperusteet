import * as _ from "lodash";
import { setInput, compiled, getComponent, inject, getOfType, testDirective, testModule, mockApp } from "../../../testutils";


describe("slocalized", () => {
    let Kieli: any;

    beforeEach(async () => {
        mockApp();
        Kieli = await getComponent("Kieli");
    });

    test("Emptying normal input", async () => {
        const [el, $scope] = await compiled(`<input type="text" ng-model="kentta"></input>`);
        setInput(el, "moro");
        expect(el.html()).toEqual("moro");
        expect($scope.kentta).toEqual("moro");
        setInput(el, "");
        expect(el.html()).toEqual("");
        expect($scope.kentta).toEqual("");
    });

    test("Tekstin editointi ja tyhjennys", async () => {
        const [el, $scope] = await compiled(`<input type="text" slocalized ng-model="kentta"></input>`);
        expect($scope.kentta).toBeUndefined();
        setInput(el, "moro");
        expect($scope.kentta).toEqual({ fi: "moro" });
        setInput(el, "mor");
        expect($scope.kentta).toEqual({ fi: "mor" });
        setInput(el, "");
        expect($scope.kentta).toEqual({ fi: "" });
    });

    test("Kielen vaihdos editoinnin aikana", async () => {
        const [el, $scope] = await compiled(`<input type="text" slocalized ng-model="kentta"></input>`);
        setInput(el, "moro");
        Kieli.setSisaltokieli("en");
        setInput(el, "hello");
        Kieli.setSisaltokieli("fi");
        Kieli.setSisaltokieli("sv");
        setInput(el, null);
        expect($scope.kentta).toEqual({ fi: "moro", en: "hello", sv: "" });
    });

});

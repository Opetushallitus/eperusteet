import _ from "lodash";
import {
    setInput,
    compiled,
    getComponent,
    inject,
    getOfType,
    testDirective,
    testModule,
    mockApp
} from "../../testutils";

describe("Custom filters", () => {
    beforeEach(async () => {
        mockApp();
    });

    describe("unsafe", () => {
        test("No undefined", async () => {
            let [el, $scope] = await compiled(`<span>{{ '' | unsafe }}</span>`);
            expect(el.text()).toEqual("");
            [el, $scope] = await compiled(`<span>{{ undefined | unsafe }}</span>`);
            expect(el.text()).toEqual("");
            [el, $scope] = await compiled(`<span>{{ null | unsafe }}</span>`);
            expect(el.text()).toEqual("");
        });
    });

    describe("aikaleima", () => {
        test("Default", async () => {
            let [el, $scope] = await compiled(`<span>{{ myDate | aikaleima }}</span>`);
            expect(el.text()).toEqual("");
            const date = new Date();
            $scope.myDate = date.getTime();
            $scope.$digest();
            let minutes: any = date.getMinutes();
            if (_.size(minutes + "") === 1) {
                minutes = "0" + minutes;
            }
            const expectedStr = `${date.getDate()}.${date.getMonth() +
                1}.${date.getFullYear()} ${date.getHours()}:${minutes} (muutama sekunti sitten)`;
            expect(el.text()).toEqual(expectedStr);
        });

        test("Date", async () => {
            let [el, $scope] = await compiled(`<span>{{ myDate | aikaleima:'time':'date' }}</span>`);
            expect(el.text()).toEqual("");
            const date = new Date();
            $scope.myDate = date.getTime();
            $scope.$digest();
            const expectedStr = `${date.getDate()}.${date.getMonth() + 1}.${date.getFullYear()}`;
            expect(el.text()).toEqual(expectedStr);
        });

        test("Ago", async () => {
            let [el, $scope] = await compiled(`<span>{{ myDate | aikaleima:'ago' }}</span>`);
            expect(el.text()).toEqual("");
            const date = new Date();
            $scope.myDate = date.getTime();
            $scope.$digest();
            expect(el.text()).toEqual("muutama sekunti sitten");
        });
    });

    describe("tyhja", () => {
        test("works as intended", async () => {
            let [el, $scope] = await compiled(`<span>{{ undefined | tyhja }}</span>`);
            expect(el.text()).toEqual("Ei asetettu");
            [el, $scope] = await compiled(`<span>{{ null | tyhja }}</span>`);
            expect(el.text()).toEqual("Ei asetettu");
            [el, $scope] = await compiled(`<span>{{ 'Morjens' | tyhja }}</span>`);
            expect(el.text()).toEqual("Morjens");
        });
    });
});

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

describe("Kuvat", () => {
    let EpImageService: any;

    beforeEach(async () => {
        mockApp();
        EpImageService = await getComponent("EpImageService");
    });

    test("Can be injected", () => expect(EpImageService).toBeTruthy());

    describe("kuvalinkit", () => {
        test("No undefined", async () => {
            let [el, $scope] = await compiled(`<span>{{ '' | kuvalinkit }}</span>`);
            expect(el.text()).toEqual("");
            [el, $scope] = await compiled(`<span>{{ undefined | kuvalinkit }}</span>`);
            expect(el.text()).toEqual("");
            [el, $scope] = await compiled(`<span>{{ null | kuvalinkit }}</span>`);
            expect(el.text()).toEqual("");
        });
    });
});

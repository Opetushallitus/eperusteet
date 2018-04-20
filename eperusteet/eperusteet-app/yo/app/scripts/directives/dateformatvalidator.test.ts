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

describe("dateformatvalidator", () => {
    beforeEach(async () => {
        mockApp();
    });

    test("Date formats work", async () => {
        let [el, $scope] = await compiled(`<span dateformatvalidator ng-model="myDate"></span>`);
        $scope.$digest();
    });
});

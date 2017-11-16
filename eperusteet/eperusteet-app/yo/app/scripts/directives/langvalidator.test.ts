import * as _ from "lodash";
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

describe("langvalidator", () => {
    let $rootScope: any;
    let $timeout: any;

    beforeEach(async () => {
        const result = mockApp()
            .service("PerusteprojektiTiedotService", () => {
                return {
                    async getPeruste() {
                        return {
                            kielet: ["fi", "sv"]
                        };
                    }
                };
            });
        $rootScope = await getComponent("$rootScope");
        $timeout = await getComponent("$timeout");
        return result;
    });

    test("langvalidator", async () => {
        let [el, $scope] = await compiled(`<lang-validator kentta="kentta"></lang-validator>`);

        $scope.kentta = {
            fi: "hello"
        };

        $timeout(() => {
            expect(el.html()).toMatch(/Ruotsi/)
            expect(el.html()).not.toMatch(/Suomi/);
        });

        $scope.kentta = {
            fi: "hello",
            sv: "hello"
        };

        $timeout(() => {
            expect(el.html()).not.toMatch(/puutteellinen-kieli/);
        });

        $scope.kentta = { };
        $timeout(() => {
            expect(el.html()).not.toMatch(/Ruotsi/);
            expect(el.html()).not.toMatch(/Suomi/);
        });
    });

    test("multifield langvalidator", async () => {
        let [el, $scope] = await compiled(`<lang-validator kentta="kentta"></lang-validator>`);
        $scope.kentta = [{
            fi: "hello",
        }, {
            sv: "world",
        }];

        $timeout(() => {
            expect(el.html()).toMatch(/Ruotsi/);
            expect(el.html()).toMatch(/Suomi/);
        });

        $scope.kentta = [null, {
            sv: "world",
        }];

        $timeout(() => {
            expect(el.html()).toMatch(/Suomi/);
        });
    });

});


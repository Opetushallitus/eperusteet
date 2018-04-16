import { getDirectives, getComponent, inject, getOfType, testModule, mockApp } from "app/testutils";
import _ from "lodash";


describe("app", () => {
    beforeEach(mockApp);

    test("Injecting services", () => {
        expect(true).toBeTruthy();
    });

    test("Injecting services", () => {
        getOfType("service").forEach(testModule);
    });

    test("Injecting factories", () => {
        getOfType("factory").forEach(testModule);
    });

    test("Injecting filters", () => {
        getOfType("filter").forEach(testModule);
    });

    test("All states use require as template", async () => {
        const $state: any = await getComponent("$state");
        _.each($state.get(), state => {
            if (state.templateUrl) {
                console.error("You should use 'template: require(...)' with state:", state.name);
            }
            expect(state.templateUrl).toBeFalsy();
            expect(!state.template && !state.abstract && _.isEmpty(state.views)).toBeFalsy();

            if (!_.isEmpty(state.views)) {
                _.each(state.views, view => {
                    expect(_.isString(view.template)).toBeTruthy();
                });
            }
        });
    });

    test.skip("Komponentit on nimetty oikein", async () => {
        getOfType("service")
            .forEach(comp => {
                expect(comp).toEqual(expect.stringMatching(/^.+Service$/));
            })
    })

    test("Komponentit on nimetty oikein", async () => {
        getOfType("controller")
            .forEach(comp => {
                expect(comp).toEqual(expect.stringMatching(/^.+(Controller|Ctrl)$/));
            })
    });

    test("Modules can be injected with getComponent", async () => {
        const $rootScope = await getComponent("$rootScope");
        const $timeout: any = await getComponent("$timeout");
        expect($rootScope).toBeTruthy();
        $timeout.flush();
    });

    test("All modules can be injected with inject", () => {
        inject($rootScope => expect($rootScope).toBeTruthy());
    });

});


describe("locale-x", () => {
    const locales = [
        ["fi", require("app/localisation/locale-fi.json")],
        ["sv", require("app/localisation/locale-sv.json")],
        ["en", require("app/localisation/locale-en.json")],
    ];

    test("Ei tyhjiä käännöksiä", () => {
        for (const locale of _.map(locales, l => l[1])) {
            for (const key of _.keys(locale)) {
                expect(locale).toHaveMemberMatching(key, (value) => _.isString(value) && !_.isEmpty(value));
            }
        }
    });

    test.skip("Käännökset lisätty kaikilla kielillä", () => {
        const fails = [];
        for (const key of _.keys(locales[0][1])) {
            for (const locale of _.tail(locales)) {
                if (!_.isString(locale[1][key]) || _.isEmpty(locale[1][key])) {
                    fails.push({
                        kieli: locale[0],
                        avain: key,
                    });
                }
            }
        }
        expect(fails.length).toEqual(0);
    });
});

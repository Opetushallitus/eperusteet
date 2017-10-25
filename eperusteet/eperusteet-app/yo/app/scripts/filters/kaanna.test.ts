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

describe("Kaanna", () => {
    let Kaanna: any;
    let Kieli: any;
    let $state: any;
    let $stateParams: any;
    let KielipreferenssiUpdater: any;

    beforeEach(async () => {
        mockApp();
        Kaanna = await getComponent("Kaanna");
        Kieli = await getComponent("Kieli");
        $state = await getComponent("$state");
        $stateParams = await getComponent("$stateParams");
        KielipreferenssiUpdater = await getComponent("KielipreferenssiUpdater");
    });

    const genLocale = (str: string) => ({
        fi: "fi " + str,
        sv: "sv " + str,
        en: "en " + str
    });

    test("Can be injected", () => expect(Kaanna).toBeTruthy());

    test("Sisältökielen vaihto", () => {
        const obj = genLocale("foobar");
        expect(Kaanna.kaanna(obj)).toEqual("fi foobar");

        Kieli.setSisaltokieli("sv");
        expect(Kieli.getSisaltokieli()).toEqual("sv");
        expect(Kaanna.kaanna(obj)).toEqual("sv foobar");

        Kieli.setSisaltokieli("en");
        expect(Kieli.getSisaltokieli()).toEqual("en");
        expect(Kaanna.kaanna(obj)).toEqual("en foobar");

        Kieli.setSisaltokieli("fi");
        expect(Kieli.getSisaltokieli()).toEqual("fi");
        expect(Kaanna.kaanna(obj)).toEqual("fi foobar");
        expect(obj).toEqual(genLocale("foobar"));
    });

    test("Virheelliset lokalisoidut sisällöt", () => {
        expect(Kaanna.kaanna(undefined)).toEqual("");
        expect(Kaanna.kaanna(null)).toEqual("");
        expect(Kaanna.kaanna("")).toEqual("");
        expect(Kaanna.kaanna(1)).toEqual("1");
        expect(Kaanna.kaanna([])).toEqual("");

        expect(
            Kaanna.kaanna({
                en: "moro"
            })
        ).toEqual("");

        Kieli.setSisaltokieli("sv");
        expect(
            Kaanna.kaanna({
                fi: "moro"
            })
        ).toEqual("[moro]");
    });

    describe("kaannaSisalto", () => {
        test("No undefined", () => {
            expect(Kaanna.kaannaSisalto(undefined)).toEqual("");
            expect(Kaanna.kaannaSisalto(null)).toEqual("");
            expect(Kaanna.kaannaSisalto("")).toEqual("");
            expect(Kaanna.kaannaSisalto({})).toEqual("");
        });
    });

    test("Ui-kielen vaihto", () => {
        const obj = genLocale("foobar");
        expect(Kaanna.kaanna(obj)).toEqual("fi foobar");

        const $stateGoMock = jest.spyOn($state, "go").mockImplementation(() => {});

        Kieli.setUiKieli("sv");
        expect(Kieli.getUiKieli()).toEqual("sv");
        expect($stateParams.lang).toEqual("sv");
        expect(Kaanna.kaanna(obj)).toEqual("fi foobar");

        Kieli.setUiKieli("fi");
        expect(Kieli.getUiKieli()).toEqual("fi");
        expect($stateParams.lang).toEqual("fi");

        expect(Kaanna.kaanna(obj)).toEqual("fi foobar");
        expect(obj).toEqual(genLocale("foobar"));
        $stateGoMock.mockReset();
    });
});

describe("Kieli", () => {
    let Kieli: any;
    let $compile: {}; // Todo tyypitykset
    let $rootScope: {}; // Todo tyypitykset

    beforeEach(async () => {
        mockApp();
        Kieli = await getComponent("Kieli");
        $compile = await getComponent("$compile");
        $rootScope = await getComponent("$rootScope");
    });

    test("Can be injected", () => expect(Kieli).toBeTruthy());

    test("Basic functions", () => {
        expect(Kieli.kieliOrder("fi")).toEqual(0);
        expect(Kieli.kieliOrder("sv")).toEqual(1);

        Kieli.setAvailableSisaltokielet(["fi", "sv", "en"]);
        expect(_.size(Kieli.SISALTOKIELET)).toEqual(5);
        expect(_.size(Kieli.availableSisaltokielet)).toEqual(3);
        Kieli.resetSisaltokielet();
        expect(_.size(Kieli.SISALTOKIELET)).toEqual(5);
        expect(_.size(Kieli.availableSisaltokielet)).toEqual(5);
    });

    test("kielenvaihto", () => {
        testDirective(($compile, $scope) => {
            const el = $compile("<kielenvaihto></kielenvaihto>")($scope);
            expect(el).toBeTruthy();
        });
    });
});

describe("kaanna", () => {
    let Kieli: any;
    let $rootScope: any;
    let $timeout: any;

    beforeEach(async () => {
        mockApp();
        Kieli = await getComponent("Kieli");
        $rootScope = await getComponent("$rootScope");
        $timeout = await getComponent("$timeout");
        await getComponent("LokalisointiLoader");
    });

    describe("filter", () => {
        test("Tuntemattomalla tekstillä", async () => {
            const [el, $scope] = await compiled(`<span ng-bind="'taysintuntematonteksti' | kaanna"></div>`);
            $scope.$digest();
            expect(el.html()).toEqual("taysintuntematonteksti");
        });

        test("Lokalisoidulla oliolla", async () => {
            const $scope = $rootScope.$new();
            $scope.olio = {
                fi: "hello",
                en: "world"
            };
            const [el] = await compiled(`<span ng-bind="olio | kaanna"></div>`, $scope);
            $scope.$digest();
            expect(el.text()).toEqual("hello");
            Kieli.setSisaltokieli("en");
            $scope.$digest();
            expect(el.text()).toEqual("world");
            Kieli.setSisaltokieli("sv");
            $scope.$digest();
            expect(el.text()).toEqual("[hello]");
        });

        test("Lokalisoitu teksti", async () => {
            const [el, $scope] = await compiled(`<span ng-bind="'kieli' | kaanna"></div>`);
            $scope.$digest();
            $timeout.flush();
            expect(el.text()).toEqual("Kieli");
        });
    });

    describe("direktiivi", () => {
        test("Lokalisoidulla oliolla", async () => {
            const $scope = $rootScope.$new();
            $scope.olio = {
                fi: "hello",
                en: "world"
            };
            const [el] = await compiled(`<span kaanna="olio"></div>`, $scope);
            expect(el.text()).toEqual("hello");
            Kieli.setSisaltokieli("en");
            expect(el.text()).toEqual("world");
            Kieli.setSisaltokieli("sv");
            expect(el.text()).toEqual("[hello]");
        });

        test("Tuntemattomalla tekstillä", async () => {
            const [el, $scope] = await compiled(`<span kaanna="'taysintuntematonteksti'"></div>`);
            $scope.$digest();
            expect(el.text()).toEqual("taysintuntematonteksti");
        });

        test("Lokalisoitu teksti", async () => {
            const [el, $scope] = await compiled(`<span kaanna="'kieli'"></div>`);
            $scope.$digest();
            expect(el.text()).toEqual("Kieli");
        });

        test("Postfix muu", async () => {
            const [el, $scope] = await compiled(`<span kaanna-postfix="kylpy" kaanna="'kieli'"></div>`);
            $scope.$digest();
            expect(el.text()).toEqual("Kieli kylpy");
        });

        test("Postfix vaaditaan", async () => {
            const [el, $scope] = await compiled(`<span vaaditaan kaanna="'kieli'"></div>`);
            $scope.$digest();
            expect(el.text()).toEqual("Kieli*");
        });

        test("Ikonien lisäys", async () => {
            const [el, $scope] = await compiled(`<span icon-role="remove" kaanna="'kieli'"></div>`);
            $scope.$digest();
            expect(el.text()).toEqual("Kieli");
        });
    });
});

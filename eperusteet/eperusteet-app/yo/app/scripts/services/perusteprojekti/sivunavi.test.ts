import * as _ from "lodash";
import {
    genId,
    createPerusteprojekti,
    createPeruste,
    getComponent,
    inject,
    getOfType,
    testDirective,
    testModule,
    mockApp
} from "../../../testutils";
import * as T from "../../types";

describe("PerusteProjektiSivunavi", () => {
    let PerusteProjektiSivunavi: any;
    let PerusteprojektiTiedotService: any;
    let $timeout: any;

    beforeEach(() => {
        return mockApp().service("PerusteprojektiTiedotService", async function() {
            const peruste = createPeruste(T.Tpo, { id: 1 });
            const projekti = createPerusteprojekti(peruste.id, { id: 2 });
            const sisalto = {
                id: genId(),
                lapset: [
                    {
                        id: genId(),
                        perusteenOsa: {
                            nimi: {
                                fi: "foo"
                            },
                            osanTyyppi: "tekstikappale"
                        }
                    },
                    {
                        id: genId(),
                        perusteenOsa: {
                            nimi: {
                                fi: "bar"
                            },
                            osanTyyppi: "taiteenala"
                        }
                    }
                ]
            };

            return {
                getProjekti: () => projekti,
                getPeruste: () => peruste,
                getSisalto: () => sisalto
            };
        });
    });

    beforeEach(mockApp);

    beforeEach(async () => {
        PerusteProjektiSivunavi = await getComponent("PerusteProjektiSivunavi");
        PerusteprojektiTiedotService = await getComponent("PerusteprojektiTiedotService");
        const $httpBackend: any = await getComponent("$httpBackend");
        $timeout = await getComponent("$timeout");
        // $httpBackend.when("GET", /cas\/me/).respond({});
        // $httpBackend.when("GET", /views.+/).respond({});
        // $httpBackend.when("GET", /localisation.+/).respond({});
        // $httpBackend.when("GET", /eperusteet-service\/api.+/).respond({});
    });

    test("Can be injected", () => expect(PerusteProjektiSivunavi).toBeTruthy());

    test("Visibility", () => {
        expect(PerusteProjektiSivunavi.isVisible()).toEqual(false);
        PerusteProjektiSivunavi.setVisible(true);
        expect(PerusteProjektiSivunavi.isVisible()).toEqual(true);
        PerusteProjektiSivunavi.setVisible(false);
        expect(PerusteProjektiSivunavi.isVisible()).toEqual(false);
    });

    test("Updating items", async () => {
        let items = [];

        const sisalto = PerusteprojektiTiedotService.getSisalto();

        const obj = {
            changed(value) {
                items = value;
            },
            typeChanged(tyyppi) {}
        };

        const spyChanged = jest.spyOn(obj, "changed");
        const spyTypeChanged = jest.spyOn(obj, "typeChanged");

        try {
            PerusteProjektiSivunavi.register("itemsChanged", obj.changed);
            PerusteProjektiSivunavi.register("typeChanged", obj.typeChanged);
            await PerusteProjektiSivunavi.refresh(true);
            $timeout.flush();

            expect(spyChanged).toHaveBeenCalled();
            expect(spyTypeChanged).toHaveBeenCalledWith("ESI");
            expect(items[0].label.fi).toEqual("foo");
            expect(items[1].label.fi).toEqual("bar");
            expect(items[0].link).toEqual([
                "root.perusteprojekti.suoritustapa.tekstikappale",
                {
                    perusteenOsaViiteId: sisalto.lapset[0].id,
                    versio: null
                }
            ]);
            expect(items[1].link).toEqual([
                "root.perusteprojekti.suoritustapa.taiteenala",
                {
                    perusteenOsaViiteId: sisalto.lapset[1].id,
                    versio: null
                }
            ]);
        } catch (err) {}
    });
});

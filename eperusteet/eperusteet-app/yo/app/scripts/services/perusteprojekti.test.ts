import * as _ from "lodash";
import {
    createPerusteprojekti,
    createPeruste,
    getComponent,
    inject,
    getOfType,
    testModule,
    mockApp
} from "../../testutils";
import * as T from "../types";

describe("PerusteProjektiService", () => {
    let PerusteProjektiService: any;
    let PerusteprojektiTiedotService: any;
    let Api: any;
    let $httpBackend: any;

    beforeEach(mockApp);

    beforeEach(inject(async function($injector) {
        $httpBackend = $injector.get("$httpBackend");
        Api = $injector.get("Api");
        $httpBackend.when("GET", "/eperusteet-service/api/kayttajaprofiili")
            .respond({});
        $httpBackend.when("GET", "/cas/me")
            .respond({});
        // $httpBackend.when("GET", "/eperusteet-service/api/perusteet")
        //     .respond([]);
        $httpBackend.when("GET", "/eperusteet-service/api/perusteet/2/suoritustavat/perusopetus/sisalto")
            .respond({
                "id" : 1180,
                "lapset" : [ {
                    "id" : 1181,
                    "perusteenOsa" : {
                        "id" : 1200,
                        "luotu" : 1508758929376,
                        "muokattu" : 1508758929376,
                        "muokkaaja" : "test",
                        "nimi" : {
                            "_tunniste" : "ede4f580-1e06-458e-b297-af121da2f9c9",
                            "fi" : "Laaja-alaiset osaamiset",
                            "_id" : "1190"
                        },
                        "tila" : "luonnos",
                        "tunniste" : "laajaalainenosaaminen",
                        "valmis" : null,
                        "kaannettava" : null,
                        "osanTyyppi" : "tekstikappale"
                    },
                    "lapset" : [ ],
                    "_perusteenOsa" : "1200"
                } ]
            });
        $httpBackend.when("GET", "/eperusteet-service/api/perusteprojektit/1")
            .respond({
                "id" : 1,
                "nimi" : "Perusopetus",
                "_peruste" : "2",
                "diaarinumero" : "x",
                "paatosPvm" : null,
                "toimikausiAlku" : null,
                "toimikausiLoppu" : null,
                "tehtavaluokka" : null,
                "tehtava" : null,
                "yhteistyotaho" : null,
                "tila" : "laadinta",
                "ryhmaOid" : "x.y.z",
                "esikatseltavissa" : false
            });

        $httpBackend.when("GET", "/eperusteet-service/api/perusteet/2")
            .respond({
                "id" : 2,
                "globalVersion" : {
                    "aikaleima" : 1508758929851
                },
                "nimi" : null,
                "koulutustyyppi" : "koulutustyyppi_16",
                "koulutukset" : [ ],
                "kielet" : [ "sv", "fi" ],
                "kuvaus" : null,
                "maarayskirje" : null,
                "muutosmaaraykset" : [ ],
                "diaarinumero" : null,
                "voimassaoloAlkaa" : null,
                "siirtymaPaattyy" : null,
                "voimassaoloLoppuu" : null,
                "paatospvm" : null,
                "luotu" : 1508758929298,
                "muokattu" : 1508758929298,
                "tila" : "luonnos",
                "tyyppi" : "normaali",
                "koulutusvienti" : false,
                "korvattavatDiaarinumerot" : [ ],
                "osaamisalat" : [ ],
                "tyotehtavatJoissaVoiToimia" : null,
                "suorittaneenOsaaminen" : null,
                "kvliite" : null
            });

        $httpBackend.when("GET", /.*/)
            .respond((a, b) => {
                console.log("UNHANDLED GET", a, b);
            });

    }));

    afterEach(() => {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    beforeEach(async () => {
        PerusteProjektiService = await getComponent("PerusteProjektiService");
        PerusteprojektiTiedotService = await getComponent("PerusteprojektiTiedotService");
        // $httpBackend.whenRoute("GET", /perusteprojektit/)
        //     .respond(async (param) => {
        //         console.log("Trying to get perusteprojekti", param);
        //     });
    });

    test("Can be injected", () => expect(PerusteProjektiService).toBeTruthy());

    test("Reformin urlin generointi", () => {
        const peruste = createPeruste(T.Ammattitutkinto, { id: 1, reforminMukainen: true });
        const projekti = createPerusteprojekti(peruste.id, { id: 2 });
        expect(PerusteProjektiService.getUrl(projekti, peruste)).toEqual("#/fi/perusteprojekti/2/reformi/sisalto");
    });

    test("urlFn", () => {
        const assertUrl = (
            expectedUrl: string,
            koulutustyyppi: T.Koulutustyyppi,
            perusteCfg = {},
            projektiCfg = {}
        ) => {
            const peruste = createPeruste(koulutustyyppi, { id: 1, ...perusteCfg });
            const projekti = createPerusteprojekti(peruste.id, { id: 2, ...projektiCfg });
            expect(PerusteProjektiService.getUrl(projekti, peruste)).toEqual(expectedUrl);
        };

        assertUrl("#/fi/perusteprojekti/2/ops/sisalto", T.Perustutkinto);
        assertUrl("#/fi/perusteprojekti/2/naytto/sisalto", T.Ammattitutkinto);
        assertUrl("#/fi/perusteprojekti/2/naytto/sisalto", T.Erikoisammattitutkinto);
        assertUrl("#/fi/perusteprojekti/2/lisaopetus/losisalto", T.Lisaopetus);
        assertUrl("#/fi/perusteprojekti/2/esiopetus/eosisalto", T.Esiopetus);
        assertUrl("#/fi/perusteprojekti/2/ops/sisalto", T.Telma);
        assertUrl("#/fi/perusteprojekti/2/ops/sisalto", T.Valma);
        assertUrl("#/fi/perusteprojekti/2/varhaiskasvatus/vksisalto", T.Varhaiskasvatus);
        assertUrl("#/fi/perusteprojekti/2/perusopetus/posisalto", T.Perusopetus);
        assertUrl("#/fi/perusteprojekti/2/aipe/aipesisalto", T.Aikuistenperusopetus);
        assertUrl("#/fi/perusteprojekti/2/lukiokoulutus/lukiosisalto", T.Lukiokoulutus);
        assertUrl("#/fi/perusteprojekti/2/lukiokoulutus/lukiosisalto", T.Lukiovalmistavakoulutus);
        assertUrl("#/fi/perusteprojekti/2/lukiokoulutus/lukiosisalto", T.Aikuistenlukiokoulutus);
        assertUrl("#/fi/perusteprojekti/2/esiopetus/eosisalto", T.Perusopetusvalmistava);
        assertUrl("#/fi/perusteprojekti/2/tpo/tposisalto", T.Tpo, { suoritustavat: [] });

        // Perusteen suoritustavalla päättely
        assertUrl("#/fi/perusteprojekti/2/reformi/sisalto", T.Perustutkinto, {
            suoritustavat: [
                {
                    suoritustapakoodi: "reformi"
                }
            ]
        });

        // Perusteprojektin suoritustavalla päättely
        assertUrl(
            "#/fi/perusteprojekti/2/reformi/sisalto",
            T.Perustutkinto,
            {},
            {
                suoritustavat: ["reformi"]
            }
        );

        // Perusteella ja perusteprojektilla suoritustapakoodit (valitaan peruste)
        assertUrl(
            "#/fi/perusteprojekti/2/reformi/sisalto",
            T.Perustutkinto,
            {
                suoritustavat: [
                    {
                        suoritustapakoodi: "reformi"
                    }
                ]
            },
            {
                suoritustavat: ["ops"]
            }
        );

        // Perusteella ja perusteprojektilla suoritustapakoodit (valitaan peruste)
        assertUrl(
            "#/fi/perusteprojekti/2/reformi/sisalto",
            T.Perustutkinto,
            {
                suoritustavat: [
                    {
                        suoritustapakoodi: "reformi"
                    }
                ]
            },
            {
                suoritustavat: ["ops"]
            }
        );

        // Oppaalla ei koulutustyyppia
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perustutkinto, {
            tyyppi: "opas",
            koulutustyyppi: undefined
        });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Aikuistenlukiokoulutus, { tyyppi: "opas" });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perusopetusvalmistava, { tyyppi: "opas" });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perustutkinto, { tyyppi: "opas" });
    });


    function tilaHelper() {

    }

    test.only("Kaiken tyyppinen sisältö ladataan", async (next) => {
        $httpBackend.flush();
        const p = PerusteprojektiTiedotService.alustaProjektinTiedot({
            lang: "fi",
            perusteProjektiId: "1"
        })
        .then(res => {
            PerusteprojektiTiedotService.haeSisalto(2, "perusopetus")
                .then(sisalto => {
                    expect(sisalto).toBeTruthy();
                    next();
                })
            next();
        })
        $httpBackend.flush();

        // console.log("Got sisalto", sisalto);
    });

});

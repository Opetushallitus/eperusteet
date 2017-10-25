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

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/perusopetus\/laajaalaisetosaamiset/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/perusopetus\/oppiaineet/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/perusopetus\/vuosiluokkakokonaisuudet/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/.+\/aihekokonaisuudet/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/.+\/oppiaineet/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/.+\/kurssit/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/17\/.+\/laajaalaiset/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/17\/.+\/vaiheet/)
            .respond([]);

        $httpBackend.when("GET", /eperusteet-service\/api\/perusteet\/\d+\/suoritustavat\/.+\/sisalto/)
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

        _.each(T.Koulutustyypit(), (kt) => {
            const ktkoodi = _.parseInt(kt.split("_")[1]);
            $httpBackend.when("GET", "/eperusteet-service/api/perusteprojektit/" + ktkoodi)
                .respond(createPerusteprojekti(ktkoodi));
            $httpBackend.when("GET", "/eperusteet-service/api/perusteet/" + ktkoodi)
                .respond(createPeruste(("koulutustyyppi_" + ktkoodi) as T.Koulutustyyppi, {
                    id: ktkoodi
                }));
        });

        $httpBackend.when("GET", /.*/)
            .respond((a, b) => {
                console.log("UNHANDLED", a, b);
            });
    }));

    // afterEach(() => {
    //     $httpBackend.verifyNoOutstandingExpectation();
    //     $httpBackend.verifyNoOutstandingRequest();
    // });

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

    function testPerusteprojektiTiedotByTyyppi(id: number, { suoritustapa }, next) {
        $httpBackend.flush();
        const p = PerusteprojektiTiedotService.alustaProjektinTiedot({
            lang: "fi",
            perusteProjektiId: "" + id,
            suoritustapa
        })
        .then(res => {
            return PerusteprojektiTiedotService.haeSisalto(id, suoritustapa)
                .then(sisalto => {
                    expect(sisalto).toBeTruthy();
                    next();
                })
        })
        $httpBackend.flush();
        return p;
    }

    test("Perusteprojektin näkymä latautuu (esiopetus)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(15, { suoritustapa: "esiopetus" }, next);
    });

    test("Perusteprojektin näkymä latautuu (lisaopetus)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(6, { suoritustapa: "lisaopetus" }, next);
    });

    test("Perusteprojektin näkymä latautuu (naytto)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(1, { suoritustapa: "naytto" }, next);
    });

    test("Perusteprojektin näkymä latautuu (aipe)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(17, { suoritustapa: "aipe" }, next);
    });

    test("Perusteprojektin näkymä latautuu (lukio)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(2, { suoritustapa: "lukio" }, next);
    });

    test("Perusteprojektin näkymä latautuu (perusopetus)", async (next) => {
        testPerusteprojektiTiedotByTyyppi(16, { suoritustapa: "perusopetus" }, next);
    });

});

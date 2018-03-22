import * as yd from "./yleinenData";
import _ from "lodash";

import {
    getComponent, inject, getOfType, testDirective, testModule, mockApp, createPeruste,
    createPerusteprojekti
} from "app/testutils";

import * as T from "../types";
import {koulutustyypit} from "./yleinenData";

describe("YleinenData", async () => {
    let YleinenData: any;

    beforeEach(async () => {
        mockApp();
        YleinenData = await getComponent("YleinenData");
    });

    test("Can be injected", () => expect(YleinenData).toBeTruthy());

    test("dateOptions", async () => {
        yd.dateOptions["year-format"] === "yy";
    });

    test("koulutustyyppiInfo avaimet", () => {
        expect(_.size(yd.koulutustyyppiInfo)).toBeGreaterThanOrEqual(15);
    });

    test("koulutustyyppiInfo arvot", () => {
        _.values(yd.koulutustyyppiInfo).forEach((ki: any) => {
            expect(ki).toHaveProperty("nimi");
            expect(_.isString(ki.nimi)).toEqual(true);

            expect(ki).toHaveProperty("oletusSuoritustapa");
            expect(_.isString(ki.oletusSuoritustapa)).toEqual(true);

            expect(ki).toHaveProperty("hasTutkintonimikkeet");
            expect(_.isBoolean(ki.hasTutkintonimikkeet)).toEqual(true);

            expect(ki).toHaveProperty("hasLaajuus");
            expect(_.isBoolean(ki.hasLaajuus)).toEqual(true);

            expect(ki).toHaveProperty("hakuState");
            expect(_.isString(ki.hakuState)).toEqual(true);

            expect(ki).toHaveProperty("sisaltoTunniste");
            expect(_.isString(ki.sisaltoTunniste)).toEqual(true);

            expect(ki).toHaveProperty("hasPdfCreation");
            expect(_.isBoolean(ki.hasPdfCreation)).toEqual(true);
        });
    });

    test("koulutustyyppiInfo uniikit arvot", () => {
        const uniikitNimet = _(yd.koulutustyyppiInfo)
            .values()
            .map("nimi")
            .uniq()
            .size();
        expect(uniikitNimet).toEqual(_.size(yd.koulutustyyppiInfo));
    });

    test("default export is a function", () => {
        expect(_.isFunction(yd.default)).toBeTruthy();
    });

    test("Perusteiden esikatselulinkkien generoiminen", () => {
        const assertSomeUrl = (
            koulutustyyppi: T.Koulutustyyppi,
            optionals: {
                suoritustapa?: string,
                perusteCfg?: {},
                projektiCfg?: {}
            } = {}
        ) => {
            const peruste = createPeruste(koulutustyyppi, { id: 1, ...optionals.perusteCfg });
            const projekti = createPerusteprojekti(peruste.id, { id: 2, ...optionals.projektiCfg });
            expect(YleinenData.getPerusteEsikatseluLink(projekti, peruste, optionals.suoritustapa)).toBeTruthy();
        };

        const assertUrl = (
            expectedUrl: string,
            koulutustyyppi: T.Koulutustyyppi,
            optionals: {
                suoritustapa?: string,
                projektiCfg?: {},
                perusteCfg?: {}
            } = {}
        ) => {
            const peruste = createPeruste(koulutustyyppi, { id: 1, ...optionals.perusteCfg });
            const projekti = createPerusteprojekti(peruste.id, { id: 2, ...optionals.projektiCfg });
            expect(YleinenData.getPerusteEsikatseluLink(projekti, peruste, optionals.suoritustapa)).toEqual(expectedUrl);
        };

        // Ei esikatselussa
        assertUrl(null, T.Perustutkinto);

        // Valmiilla perusteella on esikatselulinkki
        assertSomeUrl(T.Perustutkinto, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: false
            },
            perusteCfg: {
                tila: "valmis"
            }
        });

        // Testataan, että jokaiselle koulutustyypille löytyy esikatselulinkki jos on esikatseltavissa
        _.each(T.Koulutustyypit(), koulutustyyppi => {
            assertSomeUrl(koulutustyyppi, {
                suoritustapa: "reformi",
                projektiCfg: {
                    esikatseltavissa: true
                }
            });
        });

        // Testataan linkit koulututyypeille

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esitys/1/reformi/tiedot", T.Perustutkinto, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/lukio/1/tiedot", T.Lukiokoulutus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esitys/1/reformi/tiedot", T.Telma, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/lisaopetus/1/tiedot", T.Lisaopetus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esitys/1/reformi/tiedot", T.Ammattitutkinto, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esitys/1/reformi/tiedot", T.Erikoisammattitutkinto, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/lukio/1/tiedot", T.Aikuistenlukiokoulutus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esiopetus/1/tiedot", T.Esiopetus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/perusopetus/1/tiedot", T.Perusopetus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/aipe/1/tiedot", T.Aikuistenperusopetus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/esitys/1/reformi/tiedot", T.Valma, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/varhaiskasvatus/1/tiedot", T.Varhaiskasvatus, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/pvalmistava/1/tiedot", T.Perusopetusvalmistava, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        assertUrl("https://eperusteet.opintopolku.fi/#/fi/lukio/1/tiedot", T.Lukiovalmistavakoulutus, {
            suoritustapa: "reformi",
            projektiCfg: {
                esikatseltavissa: true
            }
        });

        // Todo: aipelle oma url
        assertUrl("https://eperusteet.opintopolku.fi/#/fi/tpo/1/tiedot", T.Tpo, {
            projektiCfg: {
                esikatseltavissa: true
            }
        });
    })
});

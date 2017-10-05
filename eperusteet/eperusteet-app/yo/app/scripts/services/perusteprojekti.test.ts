import * as _ from "lodash";
import { createPerusteprojekti, createPeruste, getComponent, inject, getOfType, testModule, mockApp } from "app/testutils";
import * as T from "scripts/types.ts";


describe("PerusteProjektiService", () => {
    let PerusteProjektiService: any;

    beforeEach(mockApp);

    beforeEach(async () => {
        PerusteProjektiService = await getComponent("PerusteProjektiService")
    });

    test("Can be injected", () => expect(PerusteProjektiService).toBeTruthy());

    test("urlFn", () => {
        const assertUrl = (expectedUrl: string, koulutustyyppi: T.Koulutustyyppi, perusteCfg = {}, projektiCfg = {}) => {
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
        // assertUrl("#/fi/perusteprojekti/2/tpo/tposisalto", T.Tpo);

        // Oppaalla ei koulutustyyppia
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perustutkinto, { tyyppi: "opas", koulutustyyppi: undefined });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Aikuistenlukiokoulutus, { tyyppi: "opas" });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perusopetusvalmistava, { tyyppi: "opas" });
        assertUrl("#/fi/perusteprojekti/2/opas/opassisalto", T.Perustutkinto, { tyyppi: "opas" });
    });
});

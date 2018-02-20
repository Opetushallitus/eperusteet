import * as _ from "lodash";

import "angular";
import "angular-ui-router";
import "angular-sanitize";
import "angular-resource";
import "angular-translate";
import "angular-animate";
import "angular-i18n/angular-locale_fi-fi";
import "angular-cache";
import "angular-local-storage";
import "angular-translate-loader-static-files";
import "angular-ui-sortable";
import "angular-ui-tree";
import "angular-elastic";
import "ui-select";
import "angular-ui-bootstrap";
import "angular-loading-bar";
import "restangular";
import "ng-file-upload";
import "es6-promise";
import "angular-mocks";

import "eperusteet-frontend-utils/mathdisplay";

import "moment/locale/fi.js";
import "moment/locale/sv.js";

import "scripts/app";
import "scripts/api";
import "scripts/misc";
import "scripts/directives/valmiskaannos";
import "scripts/directives/sortabletable";
import "scripts/directives/generictree";
import "scripts/directives/epfooter";
import "scripts/controllers/virhe";
import "scripts/controllers/tuotekstikappale";
import "scripts/directives/pagination";
import "scripts/directives/muokkaus/tutke2osa";
import "scripts/directives/readmore";
import "scripts/directives/contenttree";
import "scripts/directives/perusteprojekti/murupolku";
import "scripts/services/utils";
import "scripts/services/kieli";
import "scripts/services/kuvat";
import "scripts/services/leikelauta";
import "scripts/services/koulutuksenosamap";
import "scripts/services/ulkopuoliset";
import "scripts/services/algoritmit";
import "scripts/services/tyoryhmat";
import "scripts/services/projektiryhma";
import "scripts/services/pagination";
import "scripts/services/pdf";
import "scripts/services/excel";
import "scripts/services/muodostumissaannot";
import "scripts/services/lokalisointi";
import "scripts/services/versionhelper";
import "scripts/services/proxyservice";
import "scripts/directives/iconrole";
import "scripts/directives/tagcloud";
import "scripts/directives/rajaus";
import "scripts/directives/ohje";
import "scripts/directives/muokkaus/versiotiedot";
import "scripts/directives/multiinput";
import "scripts/directives/kommentit";
import "scripts/directives/kommenttiviesti";
import "scripts/directives/osalistaus";
import "scripts/directives/muokkaus/leikelauta";
import "scripts/directives/osallinenosa";
import "scripts/directives/muokattavaosio";
import "scripts/directives/perusopetus/tavoitteet";
import "scripts/directives/perusopetus/sisaltoalueet";
import "scripts/directives/perusopetus/arviointi";
import "scripts/controllers/perusteprojekti/osanmuokkaus";
import "scripts/directives/muokkaus/vuosiluokkakokonaisuus";
import "scripts/directives/muokkaus/oppiaine";
import "scripts/directives/muokkaus/perusopetusosaaminen";
import "scripts/directives/muokkaus/osaaminen";
import "scripts/controllers/excel";
import "scripts/controllers/tiedotteet";
import "scripts/directives/spinner";
import "scripts/controllers/modals/uudelleenohjaus";
import "scripts/controllers/modals/muodostumisryhma";
import "scripts/directives/fileselect";
import "scripts/directives/formfield";
import "scripts/directives/followscroll";
import "scripts/services/perusteet";
import "scripts/services/lukko";
import "scripts/services/perusteenosat";
import "scripts/services/varihyrra";
import "scripts/services/colorcalculator";
import "scripts/controllers/haku";
import "scripts/controllers/perusteprojekti/projektinTila";
import "scripts/filters/kaanna";
import "scripts/filters/tutkintokoodi";
import "scripts/filters/koulutusalakoodi";
import "scripts/filters/customfilters";
import "scripts/directives/koodisto";
import "scripts/directives/tree";
import "scripts/controllers/modals/aikakatko";
import "scripts/directives/sivunavigaatio";
import "scripts/services/notifikaatiot";
import "scripts/directives/statusbadge";
import "scripts/directives/sticky";
import "scripts/services/suosikit";
import "scripts/services/haku";
import "scripts/services/kayttajaprofiilit";
import "scripts/services/koulutusalat";
import "scripts/controllers/suosikit";
import "scripts/services/yleinenData";
import "scripts/services/arviointi"
import "scripts/services/navigaatiopolku"
import "scripts/directives/editointikontrollit"
import "scripts/directives/editointiKontrolli"
import "scripts/directives/tuotutkinnonosa"
import "scripts/services/editointikontrollit"
import "scripts/controllers/muokkaus"
import "scripts/directives/muokkaus/lukko"
import "scripts/directives/muokkaus/revertnote"
import "scripts/controllers/perusteprojekti/tutkinnonosa"
import "scripts/controllers/perusteprojekti/koulutuksenosa"
import "scripts/controllers/perusteprojekti/tekstikappale"
import "scripts/directives/pikamenu"
import "scripts/directives/editinplace"
import "scripts/directives/termistoviitteet"
import "scripts/services/lukiokoulutus"
import "scripts/services/perusteprojekti"
import "scripts/controllers/perusteprojekti/projektinTiedot"
import "scripts/controllers/perusteprojekti/muodostumissaannot"
import "scripts/controllers/perusteprojekti/tutkinnonosat"
import "scripts/controllers/perusteprojekti/projektiryhma"
import "scripts/controllers/perusteprojekti/projektinperustiedot"
import "scripts/controllers/perusteprojekti/termisto"
import "scripts/controllers/omatperusteprojektit"
import "scripts/services/perusopetus"
import "scripts/controllers/perusopetus"
import "scripts/controllers/esiopetus"
import "scripts/controllers/lukiokoulutus"
import "scripts/controllers/kurssit"
import "scripts/directives/datecomparisonvalidator"
import "scripts/directives/muokkaus/muokattavakentta"
import "scripts/directives/arviointi"
import "scripts/directives/ammattitaitovaatimukset"
import "scripts/directives/valmaarviointi"
import "scripts/controllers/modals/ilmoitusdialogi"
import "scripts/controllers/perusteprojekti/perusteenTiedot"
import "scripts/services/opintoalat"
import "scripts/services/kommentit"
import "scripts/directives/muokkaus/kenttalistaus"
import "scripts/directives/dateformatvalidator"
import "scripts/controllers/aloitussivu"
import "scripts/controllers/perusteprojekti/perusteprojekti"
import "scripts/services/perusteprojekti/sivunavi"
import "scripts/controllers/perusteprojekti/sisalto"
import "scripts/controllers/perusteprojekti/kommentit"
import "scripts/controllers/perusteprojekti/toimikausi"
import "scripts/services/varmistusdialogi"
import "scripts/controllers/admin"
import "scripts/directives/numberinput"
import "scripts/directives/dateformatter"
import "scripts/services/koulutusalat"
import "scripts/controllers/modals/tilanvaihtovirhe"
import "scripts/directives/diaarinumerouniikki"
import "scripts/directives/oikeustarkastelu"
import "scripts/controllers/modals/rakenneosamodal"
import "scripts/controllers/perusteprojekti/tutkinnonosaosaalue"
import "scripts/controllers/perusteprojekti/esiopetus"
import "scripts/controllers/perusteprojekti/tpoopetus"
import "scripts/directives/hallintalinkki"
import "scripts/directives/muokkaus/aihekokonaisuudet"
import "scripts/directives/muokkaus/lukiokoulutusyleisettavoitteet"
import "scripts/states/opas/state"
import "scripts/states/perusteprojekti/suoritustapa/aipesisalto/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosalistaus/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/state"
import "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/kurssi/state"
import "scripts/services/aipe"
import "scripts/directives/muokkaus/vaihe"
import "scripts/directives/perustetoisetprojektit"
import "scripts/components/tavoitteet/tavoitteet"

import { Koulutustyyppi } from "scripts/types";

export const testModule = (name: string) => {
    angular.mock.inject([name, (injected) => {
        expect(injected).toBeTruthy();
    }]);
};

export const getDirectives = () => {
    return _.chain(angular.module("eperusteApp")._invokeQueue)
        .filter(val => val[1] === "directive")
        .map(val => val[2][1])
        .value();
};

export const getOfType = (type: "service" | "factory" | "filter" | "directive" | "state" | "controller") => {
    return _.chain((angular.module("eperusteApp") as any)._invokeQueue)
        .filter(val => type === "controller" ? val[0] === "$controllerProvider" : val[1] === type)
        .map(val => val[2][0])
        .value();
};

export const mockApp = () => {
    angular.mock.module("eperusteApp");
    return angular.module("eperusteApp");
};

// Get injected component
export async function getComponent(name: string) {
    return new Promise((resolve, reject) => {
        return angular.mock.inject([name, async (injectedModule) => {
            const realModule = injectedModule;
            if (_.isObject(realModule) && realModule.$$state && realModule.$$state.value) {
                resolve(realModule.$$state.value);
            }
            resolve(realModule);
        }]);
    });
}

export function setInput(el: JQuery, text: string) {
    el.text(text).val(text).trigger("input");
    return el;
}

export async function compiled(template, scope?: angular.IScope): Promise<[JQuery, angular.IScope]> {
    const $rootScope = await getComponent("$rootScope");
    const $scope = scope || ($rootScope as any).$new();
    const $compile: any = await getComponent("$compile");
    const el = $compile(angular.element(template))($scope);
    $scope.$digest();
    return [el, $scope];
}

/// Inject angular modules into function parameters
export function inject(fn: Function) {
    return angular.mock.inject(fn);
}

/// Inject angular modules into function parameters
export function testDirective(fn: Function) {
    inject(($compile, $rootScope) => fn($compile, $rootScope.$new()));
}

export function genId() {
    return _.parseInt(_.uniqueId());
}

/// Create a perusteprojekti
export function createPerusteprojekti(perusteId: number, config?: Object) {
    return {
        id: genId(),
        nimi: "Perusteprojekti",
        _peruste: "" + perusteId,
        diaarinumero: "1234",
        paatosPvm: null,
        toimikausiAlku: null,
        toimikausiLoppu: null,
        tehtavaluokka: null,
        tehtava: null,
        yhteistyotaho: null,
        tila: "laadinta",
        ryhmaOid: "1.2.246.562.28.23416071860",
        esikatseltavissa: false,
        ...config
    };
}

/// Create a peruste
export function createPeruste(koulutustyyppi: Koulutustyyppi, config?: Object) {
    return {
        id: genId(),
        globalVersion: {
            aikaleima: 1507038252978
        },
        nimi: null,
        koulutustyyppi,
        koulutukset: [ ],
        kielet: [ "fi", "sv" ],
        kuvaus: null,
        maarayskirje: null,
        muutosmaaraykset: [ ],
        diaarinumero: null,
        voimassaoloAlkaa: null,
        siirtymaPaattyy: null,
        voimassaoloLoppuu: null,
        paatospvm: null,
        luotu: 1506691022357,
        muokattu: 1506691022357,
        tila: "luonnos",
        tyyppi: "normaali",
        koulutusvienti: false,
        korvattavatDiaarinumerot: [ ],
        osaamisalat: [ ],
        suoritustavat: [],
        kvliite: {
            id: genId(),
            suorittaneenOsaaminen: null,
            tyotehtavatJoissaVoiToimia: null,
            tutkinnonVirallinenAsema: null,
            tutkintotodistuksenAntaja: null,
            _arvosanaAsteikko: null,
            jatkoopintoKelpoisuus: null,
            kansainvalisetSopimukset: null,
            saadosPerusta: null,
            pohjakoulutusvaatimukset: null,
            lisatietoja: null,
            tutkintotodistuksenSaaminen: null,
            tutkinnonTaso: null,
            tutkinnostaPaattavaViranomainen: null
        },
        ...config
    }
}

expect.extend({
    toHaveMemberMatching(received: Object, field: string, matcher: Function) {
        if (!_.isObject(received)) {
            return {
                message() {
                    return `expected an object`;
                },
                pass: false
            }
        }
        else if (!received.hasOwnProperty(field)) {
            return {
                message() {
                    return `expected object to contain field "${field}"`;
                },
                pass: false
            }
        }
        else {
            const pass = matcher(received[field]);
            return {
                message() {
                    return `expected object field "${field}" value "${received[field]}" to match criteria`;
                },
                pass
            }
        }
    }
});


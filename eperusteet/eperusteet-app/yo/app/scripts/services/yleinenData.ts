/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

import * as angular from "angular";
import * as _ from "lodash";
import moment from "moment";

export const Diaariformaatit = [/^OPH-\d{1,5}-\d{4}$/, /^\d{1,3}\/\d{3}\/\d{4}$/];

export const dateOptions = {
    "year-format": "yy",
    //'month-format': 'M',
    //'day-format': 'd',
    "starting-day": 1
};

export const isDiaariValid = (diaarinumero: string) => {
    return (
        !diaarinumero ||
        (_.isString(diaarinumero) && _.some(Diaariformaatit, formaatti => diaarinumero.match(formaatti)))
    );
};

export const kommenttiMaxLength = 1024;

export const kontekstit = ["ammatillinenperuskoulutus", "ammatillinenaikuiskoulutus"];

export const rakenneRyhmaRoolit = ["määritelty", "määrittelemätön", "vieras"];

export const osaamisalaRooli = "osaamisala";

export const yksikot = ["OSAAMISPISTE", "OPINTOVIIKKO"];

export const yksikotMap = {
    osp: "OSAAMISPISTE",
    ov: "OPINTOVIIKKO",
    kurssi: "KURSSI"
};

export const ammatillisetSuoritustavat = ["ops", "naytto", "reformi"];

export const suoritustavat = ["ops", "naytto", "reformi", "lukiokoulutus"];

export const koulutustyyppiInfo = {
    koulutustyyppi_1: {
        nimi: "perustutkinto",
        oletusSuoritustapa: "ops",
        hasLaajuus: true,
        hasTutkintonimikkeet: true,
        hakuState: "root.selaus.ammatillinenperuskoulutus",
        sisaltoTunniste: "sisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_999907: {
        nimi: "tpo",
        oletusSuoritustapa: "tpo",
        hasTutkintonimikkeet: false,
        hasLaajuus: false,
        hakuState: "root.selaus.ammatillinenaikuiskoulutus",
        sisaltoTunniste: "tposisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_11: {
        nimi: "ammattitutkinto",
        oletusSuoritustapa: "naytto",
        hasTutkintonimikkeet: true,
        hasLaajuus: true,
        hakuState: "root.selaus.ammatillinenaikuiskoulutus",
        sisaltoTunniste: "sisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_5: {
        nimi: "telma",
        hasLaajuus: true,
        oletusSuoritustapa: "ops",
        hasTutkintonimikkeet: true,
        hakuState: "root.selaus.ammatillinenaikuiskoulutus",
        sisaltoTunniste: "sisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_18: {
        nimi: "velma",
        hasLaajuus: true,
        oletusSuoritustapa: "ops",
        hasTutkintonimikkeet: true,
        hakuState: "root.selaus.ammatillinenaikuiskoulutus",
        sisaltoTunniste: "sisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_12: {
        nimi: "erikoisammattitutkinto",
        oletusSuoritustapa: "naytto",
        hasLaajuus: true,
        hasTutkintonimikkeet: true,
        hakuState: "root.selaus.ammatillinenaikuiskoulutus",
        sisaltoTunniste: "sisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_15: {
        nimi: "esiopetus",
        oletusSuoritustapa: "esiopetus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.tpolista",
        sisaltoTunniste: "eosisalto",
        hasLaajuus: false,
        hasPdfCreation: false
    },
    koulutustyyppi_16: {
        nimi: "perusopetus",
        oletusSuoritustapa: "perusopetus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.perusopetuslista",
        hasLaajuus: false,
        sisaltoTunniste: "posisalto",
        hasPdfCreation: false
    },
    koulutustyyppi_17: {
        nimi: "aikuistenperusopetus",
        oletusSuoritustapa: "aipe",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.aikuisperusopetuslista",
        sisaltoTunniste: "aipesisalto",
        hasLaajuus: false,
        hasPdfCreation: true
    },
    koulutustyyppi_6: {
        nimi: "lisaopetus",
        oletusSuoritustapa: "lisaopetus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.lisaopetuslista",
        hasLaajuus: false,
        sisaltoTunniste: "losisalto",
        hasPdfCreation: true
    },
    koulutustyyppi_2: {
        nimi: "lukiokoulutus",
        oletusSuoritustapa: "lukiokoulutus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.lukiokoulutuslista",
        hasLaajuus: false,
        sisaltoTunniste: "lukiosisalto",
        hasPdfCreation: false
    },
    koulutustyyppi_23: {
        nimi: "lukiokoulutus",
        oletusSuoritustapa: "lukiokoulutus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.lukiokoulutuslista",
        hasLaajuus: false,
        sisaltoTunniste: "lukiosisalto",
        hasPdfCreation: false
    },
    koulutustyyppi_20: {
        nimi: "varhaiskasvatus",
        oletusSuoritustapa: "varhaiskasvatus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.varhaisopetuslista",
        hasLaajuus: false,
        sisaltoTunniste: "vksisalto",
        hasPdfCreation: false
    },
    koulutustyyppi_22: {
        nimi: "esiopetus",
        oletusSuoritustapa: "esiopetus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.esiopetuslista",
        hasLaajuus: false,
        sisaltoTunniste: "eosisalto",
        hasPdfCreation: false
    },
    koulutustyyppi_14: {
        nimi: "lukiokoulutus",
        oletusSuoritustapa: "lukiokoulutus",
        hasTutkintonimikkeet: false,
        hakuState: "root.selaus.lukiokoulutuslista",
        hasLaajuus: false,
        sisaltoTunniste: "lukiosisalto",
        hasPdfCreation: false
    }
};

export const naviOmit = ["root", "editoi", "suoritustapa", "sisalto", "aloitussivu", "selaus", "esitys"];

export const koulutustyypit = _.keys(koulutustyyppiInfo);

export const ammatillisetkoulutustyypit = ["koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"];

export function laajuudellisetKoulutustyypit() {
    return _(koulutustyyppiInfo)
        .keys()
        .filter(function(key) {
            return !koulutustyyppiInfo || !koulutustyyppiInfo[key] ? false : koulutustyyppiInfo[key].hasLaajuus;
        })
        .value();
}

export const kvliitekielet = ["fi", "sv", "en"];

export const kielet = {
    suomi: "fi",
    ruotsi: "sv",
    englanti: "en"
};

export const kieli = "fi";

export const defaultItemsInModal = 10;

export const dateFormatDatepicker = "d.M.yyyy";

export const dateFormatMomentJS = "D.M.YYYY";

export const isReformoitava = koulutustyyppi =>
    _.includes(["koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"], koulutustyyppi);

export const isPerusopetus = function(peruste) {
    return peruste.koulutustyyppi === "koulutustyyppi_16";
};

export const isAipe = function(peruste) {
    return peruste.koulutustyyppi === "koulutustyyppi_17";
};

export const isValmaTelma = function(koulutustyyppiTaiPeruste) {
    if (koulutustyyppiTaiPeruste) {
        const ortherKoulutustyyppiTaiPeruste = _.isString(koulutustyyppiTaiPeruste)
            ? koulutustyyppiTaiPeruste
            : koulutustyyppiTaiPeruste.koulutustyyppi;
        return (
            ortherKoulutustyyppiTaiPeruste === "koulutustyyppi_18" ||
            ortherKoulutustyyppiTaiPeruste === "koulutustyyppi_5"
        );
    }
    return false;
};

export const isAmmatillinen = koulutustyyppi => isReformoitava(koulutustyyppi) || isValmaTelma(koulutustyyppi);

export const isLisaopetus = function(peruste) {
    return peruste.koulutustyyppi === "koulutustyyppi_6";
};

export const isTpo = function(peruste) {
    return peruste.koulutustyyppi === "koulutustyyppi_999907";
};

export const isVarhaiskasvatus = function(peruste) {
    return peruste.koulutustyyppi === "koulutustyyppi_20";
};

export const isOpas = function(peruste) {
    return peruste.tyyppi === "opas";
};

export const yhteisetTutkinnonOsat = ["tutke2", "reformi_tutke2"];

export const isTutke2 = (viite) => {
    if (viite == null || viite.tutkinnonOsa == null || viite.tutkinnonOsa.tyyppi == null) {
        return false;
    }

    return _.includes(yhteisetTutkinnonOsat, viite.tutkinnonOsa.tyyppi);
};

export const isEsiopetus = function(peruste) {
    return _.any(["koulutustyyppi_15", "koulutustyyppi_22"], tyyppi => tyyppi === peruste.koulutustyyppi);
};

export const isLukiokoulutus = function(peruste) {
    return _.any(
        ["koulutustyyppi_2", "koulutustyyppi_23", "koulutustyyppi_14"],
        tyyppi => tyyppi === peruste.koulutustyyppi
    );
};

export const isSimple = function(peruste) {
    return (
        isOpas(peruste) || isEsiopetus(peruste) || isLisaopetus(peruste) || isVarhaiskasvatus(peruste) || isTpo(peruste)
    );
};

export const validSuoritustapa = function(peruste, suoritustapa) {
    // Deprecated, TODO: poista, käytä koulutustyyppiInfoa
    return peruste.koulutustyyppi === "koulutustyyppi_12" ? "naytto" : suoritustapa;
};

export const valitseSuoritustapaKoulutustyypille = function(koulutustyyppi, reformi) {
    if (koulutustyyppiInfo[koulutustyyppi]) {
        if (reformi) {
            return "reformi";
        } else {
            return koulutustyyppiInfo[koulutustyyppi].oletusSuoritustapa;
        }
    }
    return "ops";
};

export function showKoulutukset(peruste) {
    return peruste.koulutustyyppi !== "koulutustyyppi_16";
}

export default function($rootScope, $translate, Arviointiasteikot, Notifikaatiot, Kaanna, $q, $location, Kieli) {
    return {
        dateOptions,
        isDiaariValid,
        naviOmit,
        kommenttiMaxLength,
        kontekstit,
        rakenneRyhmaRoolit,
        osaamisalaRooli,
        yksikot,
        yksikotMap,
        ammatillisetSuoritustavat,
        suoritustavat,
        koulutustyyppiInfo,
        koulutustyypit,
        ammatillisetkoulutustyypit,
        laajuudellisetKoulutustyypit,
        kvliitekielet,
        kielet,
        kieli,
        arviointiasteikot: undefined,
        defaultItemsInModal,
        dateFormatDatepicker,
        dateFormatMomentJS,
        isReformoitava,
        isPerusopetus,
        isAipe,
        isValmaTelma,
        isLisaopetus,
        isAmmatillinen,
        isVarhaiskasvatus,
        isOpas,
        isEsiopetus,
        isLukiokoulutus,
        isSimple,
        isTutke2,
        validSuoritustapa,
        valitseSuoritustapaKoulutustyypille,
        showKoulutukset,

        getPerusteEsikatseluHost() {
            var host = $location.host();
            var kieli = Kieli.getSisaltokieli();
            if (host.indexOf("localhost") > -1) {
                //localhost - dev
                return "http://localhost:9020/#" + kieli;
            } else if (host.indexOf("testi.virkailija.opintopolku.fi") > -1) {
                // QA
                return "https://testi-eperusteet.opintopolku.fi/#" + kieli;
            } else {
                // Tuotanto
                return "https://eperusteet.opintopolku.fi/#" + kieli;
            }
        },

        haeArviointiasteikot() {
            if (this.arviointiasteikot === undefined) {
                var self = this;
                Arviointiasteikot.list(
                    {},
                    function(tulos) {
                        self.arviointiasteikot = _.indexBy(tulos, "id");
                        $rootScope.$broadcast("arviointiasteikot");
                    },
                    Notifikaatiot.serverCb
                );
            } else {
                $rootScope.$broadcast("arviointiasteikot");
            }
        },

        vaihdaKieli(kielikoodi) {
            var loytyi = false;
            for (var avain in this.kielet) {
                if (this.kielet.hasOwnProperty(avain) && this.kielet[avain] === kielikoodi) {
                    loytyi = true;
                    break;
                }
            }
            // Jos kielikoodi ei löydy listalta niin käytetään suomea.
            if (!loytyi) {
                kielikoodi = "fi";
            }
            if (this.kielikoodi !== kielikoodi) {
                moment.locale(kielikoodi);
                $translate.use(kielikoodi);
                this.kieli = kielikoodi;
                $rootScope.$broadcast("notifyCKEditor");
                $rootScope.$broadcast("changed:uikieli");
            }
        },

        valitseKieli(teksti) {
            return Kaanna.kaannaSisalto(teksti);
        },

        async lukioKurssityypit() {
            return [
                {
                    nimi: {
                        fi: "Pakollinen"
                    },
                    koodi: "kurssityyppi_FOO",
                    tyyppi: "PAKOLLINEN"
                },
                {
                    nimi: {
                        fi: "Valtakunnallinen syventävä"
                    },
                    koodi: "kurssityyppi_BAR",
                    tyyppi: "VALTAKUNNALLINEN_SYVENTAVA"
                },
                {
                    nimi: {
                        fi: "Valtakunnallinen soveltava"
                    },
                    koodi: "kurssityyppi_BAZ",
                    tyyppi: "VALTAKUNNALLINEN_SOVELTAVA"
                }
            ];
        }
    };
}

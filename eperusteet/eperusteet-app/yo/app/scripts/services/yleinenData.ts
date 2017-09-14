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

angular
    .module("eperusteApp")
    .service("YleinenData", function(
        $rootScope,
        $translate,
        Arviointiasteikot,
        Notifikaatiot,
        Kaanna,
        $q,
        $location,
        Kieli
    ) {
        this.dateOptions = {
            "year-format": "yy",
            //'month-format': 'M',
            //'day-format': 'd',
            "starting-day": 1
        };

        const Diaariformaatit = [/^OPH-\d{1,5}-\d{4}$/, /^\d{1,3}\/\d{3}\/\d{4}$/];

        this.isDiaariValid = (diaarinumero: string) => {
            return (
                !diaarinumero ||
                (_.isString(diaarinumero) && _.some(Diaariformaatit, formaatti => diaarinumero.match(formaatti)))
            );
        };

        this.getPerusteEsikatseluHost = function() {
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
        };

        this.naviOmit = ["root", "editoi", "suoritustapa", "sisalto", "aloitussivu", "selaus", "esitys"];

        this.kommenttiMaxLength = 1024;

        this.rakenneRyhmaRoolit = ["määritelty", "määrittelemätön", "vieras"];

        this.osaamisalaRooli = "osaamisala";

        this.yksikot = ["OSAAMISPISTE", "OPINTOVIIKKO"];
        this.yksikotMap = {
            osp: "OSAAMISPISTE",
            ov: "OPINTOVIIKKO",
            kurssi: "KURSSI"
        };

        this.ammatillisetSuoritustavat = ["ops", "naytto", "reformi"];

        this.suoritustavat = ["ops", "naytto", "reformi", "lukiokoulutus"];

        this.koulutustyyppiInfo = {
            koulutustyyppi_1: {
                nimi: "perustutkinto",
                oletusSuoritustapa: "ops",
                hasLaajuus: true,
                hasTutkintonimikkeet: true,
                hakuState: "root.selaus.ammatillinenperuskoulutus",
                sisaltoTunniste: "sisalto",
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
                hakuState: "root.selaus.esiopetuslista",
                sisaltoTunniste: "eosisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_16: {
                nimi: "perusopetus",
                oletusSuoritustapa: "perusopetus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.perusopetuslista",
                sisaltoTunniste: "posisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_17: {
                nimi: "aikuistenperusopetus",
                oletusSuoritustapa: "aipe",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.aikuisperusopetuslista",
                sisaltoTunniste: "aipesisalto",
                hasPdfCreation: true
            },
            koulutustyyppi_6: {
                nimi: "lisaopetus",
                oletusSuoritustapa: "lisaopetus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.lisaopetuslista",
                sisaltoTunniste: "losisalto",
                hasPdfCreation: true
            },
            koulutustyyppi_2: {
                nimi: "lukiokoulutus",
                oletusSuoritustapa: "lukiokoulutus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.lukiokoulutuslista",
                sisaltoTunniste: "lukiosisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_23: {
                nimi: "lukiokoulutus",
                oletusSuoritustapa: "lukiokoulutus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.lukiokoulutuslista",
                sisaltoTunniste: "lukiosisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_20: {
                nimi: "varhaiskasvatus",
                oletusSuoritustapa: "varhaiskasvatus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.varhaisopetuslista",
                sisaltoTunniste: "vksisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_22: {
                nimi: "esiopetus",
                oletusSuoritustapa: "esiopetus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.esiopetuslista",
                sisaltoTunniste: "eosisalto",
                hasPdfCreation: false
            },
            koulutustyyppi_14: {
                nimi: "lukiokoulutus",
                oletusSuoritustapa: "lukiokoulutus",
                hasTutkintonimikkeet: false,
                hakuState: "root.selaus.lukiokoulutuslista",
                sisaltoTunniste: "lukiosisalto",
                hasPdfCreation: false
            }
        };

        this.koulutustyypit = _.keys(this.koulutustyyppiInfo);
        this.ammatillisetkoulutustyypit = [
            "koulutustyyppi_1",
            "koulutustyyppi_5",
            "koulutustyyppi_11",
            "koulutustyyppi_12",
            "koulutustyyppi_18"
        ];
        var me = this;
        this.laajuudellisetKoulutustyypit = _(this.koulutustyyppiInfo)
            .keys()
            .filter(function(key) {
                return !me.koulutustyyppiInfo || !me.koulutustyyppiInfo[key]
                    ? false
                    : me.koulutustyyppiInfo[key].hasLaajuus;
            })
            .value();

        this.kvliitekielet = ["fi", "sv", "en"];

        this.kielet = {
            suomi: "fi",
            ruotsi: "sv",
            englanti: "en"
        };

        this.kieli = "fi";

        this.arviointiasteikot = undefined;

        this.defaultItemsInModal = 10;

        this.dateFormatDatepicker = "d.M.yyyy";
        this.dateFormatMomentJS = "D.M.YYYY";

        this.isReformoitava = koulutustyyppi =>
            _.includes(["koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"], koulutustyyppi);

        this.isPerusopetus = function(peruste) {
            return peruste.koulutustyyppi === "koulutustyyppi_16";
        };

        this.isAipe = function(peruste) {
            return peruste.koulutustyyppi === "koulutustyyppi_17";
        };

        this.isValmaTelma = function(koulutustyyppiTaiPeruste) {
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

        this.isLisaopetus = function(peruste) {
            return peruste.koulutustyyppi === "koulutustyyppi_6";
        };

        this.isVarhaiskasvatus = function(peruste) {
            return peruste.koulutustyyppi === "koulutustyyppi_20";
        };

        this.isOpas = function(peruste) {
            return peruste.tyyppi === "opas";
        };

        this.isEsiopetus = function(peruste) {
            return _.any(["koulutustyyppi_15", "koulutustyyppi_22"], tyyppi => tyyppi === peruste.koulutustyyppi);
        };

        this.isLukiokoulutus = function(peruste) {
            return _.any(
                ["koulutustyyppi_2", "koulutustyyppi_23", "koulutustyyppi_14"],
                tyyppi => tyyppi === peruste.koulutustyyppi
            );
        };

        this.isSimple = function(peruste) {
            return (
                this.isOpas(peruste) ||
                this.isEsiopetus(peruste) ||
                this.isLisaopetus(peruste) ||
                this.isVarhaiskasvatus(peruste)
            );
        };

        this.validSuoritustapa = function(peruste, suoritustapa) {
            // Deprecated, TODO: poista, käytä koulutustyyppiInfoa
            return peruste.koulutustyyppi === "koulutustyyppi_12" ? "naytto" : suoritustapa;
        };

        this.valitseSuoritustapaKoulutustyypille = function(koulutustyyppi, reformi) {
            if (this.koulutustyyppiInfo[koulutustyyppi]) {
                if (reformi) {
                    return "reformi";
                } else {
                    return this.koulutustyyppiInfo[koulutustyyppi].oletusSuoritustapa;
                }
            }
            return "ops";
        };

        this.showKoulutukset = function(peruste) {
            return peruste.koulutustyyppi !== "koulutustyyppi_16";
        };

        this.haeArviointiasteikot = function() {
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
        };

        this.vaihdaKieli = function(kielikoodi) {
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
        };

        this.valitseKieli = function(teksti) {
            return Kaanna.kaannaSisalto(teksti);
        };

        var kurssityypitDefer = $q.defer();
        /**
     * @returns Promise<LukiokurssityyppiSelectOption>
     */
        this.lukioKurssityypit = _.constant(kurssityypitDefer.promise);
        // TODO: get form backend/koodisto?
        kurssityypitDefer.resolve([
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
        ]);
    });

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

declare var XLSX: any;

import * as angular from "angular";
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .service("ExcelSheetService", function() {
        function generoiSaraketunnisteet(sheet) {
            var columns = _(sheet)
                .keys()
                .map(function(cell) {
                    return cell.replace(/[0-9]/g, "");
                })
                .unique()
                .tail() // poistaa kentän '!ref'
                .value();
            return columns;
        }

        function sheetHeight(sheet) {
            return sheet["!ref"].replace(/^.+:/, "").replace(/[^0-9]/g, "");
        }

        function getOsaAnchors(data, tyyppi, osatutkintoMap) {
            var height = sheetHeight(data);
            var anchors = [];
            for (var i = 2; i < height; i++) {
                var celldata = data[osatutkintoMap[tyyppi].kentat[1] + i];
                if (celldata && celldata.v) {
                    anchors.push(i);
                }
            }
            return anchors;
        }

        function poistaSarake(sheet, sarake) {
            var height = sheetHeight(sheet);
            var tunnisteet = generoiSaraketunnisteet(sheet);

            var startIndex = 0;
            for (startIndex = 0; startIndex < _.size(tunnisteet) && tunnisteet[startIndex] !== sarake; ++startIndex) {}

            _.map(_.range(startIndex + 1, _.size(tunnisteet)), function(col) {
                _.map(_.range(1, height), function(row) {
                    sheet[tunnisteet[col - 1] + row] = sheet[tunnisteet[col] + row];
                });
            });

            return sheet;
        }

        return {
            sheetHeight: sheetHeight,
            poistaSarake: poistaSarake,
            getOsaAnchors: getOsaAnchors
        };
    })
    .service("ExcelService", function($q, Algoritmit, ExcelSheetService) {
        var notifier = angular.noop;

        // Tutkintojen/perusteiden parsiminen
        var tutkintoMap: any = {
            // Lopullisen backendille lähetettävän perusteen rakenne
            parsittavatKentat: {
                1: "nimi",
                2: "tutkintokoodi",
                3: "laajuus",
                4: "yksikko",
                5: "diaarinumero"
                // 5: 'opintoalat',
                // 6: 'paivays',
            },
            // Perustutkintoon liittyvät tiedot
            perustutkinto: {
                // Parsittavan kentän ja solun suhde
                kentat: {
                    1: "A",
                    2: "B",
                    3: "D",
                    4: "E",
                    5: "C"
                },
                tekstikentat: [
                    "F",
                    "G",
                    "H",
                    "I",
                    "J",
                    "K",
                    "L",
                    "M",
                    "N",
                    "O",
                    "P",
                    "Q",
                    "R",
                    "S",
                    "T",
                    "U",
                    "V",
                    "W",
                    "X",
                    "Y",
                    "Z",
                    "AA",
                    "AB",
                    "AC",
                    "AD",
                    "AE",
                    "AF",
                    "AG",
                    "AH"
                ]
            },
            ammattitutkinto: {
                kentat: {
                    1: "A",
                    2: "B",
                    5: "C"
                },
                // Tekstikappaleet/perusteen osat
                tekstikentat: ["D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S"]
            }
        };
        // Kentät jotka pitää lokalisoida (laittaa oliossa fi:n alle)
        tutkintoMap.lokalisointi = [1];

        var osatutkintoMap = {
            parsittavatKentat: {
                1: "nimi",
                2: "ammattitaitovaatimukset",
                3: "opintoluokitus",
                5: "osoittamistavat",
                6: "kohdealueet",
                7: "kohteet",
                8: "arviointikriteerit",
                9: "tyydyttava",
                10: "hyvä",
                11: "kiitettävä",
                12: "erillispätevyys",
                13: "laajuus"
            },
            virheet: {
                1: "Perusteen osan nimeä ei ole määritetty"
            },
            varoitukset: {
                2: "Ammattitaitovaatimuksien kuvausta ei ole määritetty.",
                3: "Opintoluokitusta ei ole määritetty."
            },
            info: [1, 2, 3, 13],
            lokalisointi: [1, 2],
            ammattitutkinto: {
                kentat: {
                    1: "V",
                    2: "Y",
                    3: "U",
                    4: "X",
                    5: "AF",
                    6: "AA",
                    7: "AB",
                    8: "AC",
                    12: "Z"
                },
                asetukset: {
                    arviointiasteikko: "1"
                },
                otsikot: {
                    AA1: "Ammattitaitovaatimus",
                    AB1: "Arvioinnin kohde",
                    AC1: "Arviointikriteerit",
                    AF1: "Ammattitaidon osoittamistavat",
                    U1: "Tutkinnon osan opintoluokituskoodi",
                    V1: "Tutkinnon osan nimi",
                    Y1: "Ammattitaitovaatimukset (tiivistelmä/kuvaus ammattitaitovaatimuksista)",
                    Z1: "Erillispätevyys"
                }
            },
            perustutkinto: {
                asetukset: {
                    arviointiasteikko: "2"
                },
                kentat: {
                    1: "AK",
                    2: "AT",
                    3: "AJ",
                    4: "AO",
                    5: "AZ",
                    6: "AU",
                    7: "AV",
                    9: "AW",
                    10: "AX",
                    11: "AY",
                    13: "AL"
                },
                otsikot: {
                    AJ1: "Tutkinnon osan opintoluokituskoodi",
                    AK1: "Tutkinnon osan nimi",
                    AL1: "Tutkinnon osan laajuus",
                    AQ1: "Tutkintonimike",
                    AR1: "Tutkintonimikekoodi",
                    AT1: "Ammattitaitovaatimus / tavoite",
                    AU1: "Arvioinnin kohdealue",
                    AV1: "Arvioinnin kohde",
                    AW1: "Tyydyttävä T1 ",
                    AX1: "Hyvä H2",
                    AY1: "Kiitettävä K3",
                    AZ1: "Ammattitaidon osoittamistavat"
                }
            }
        };

        function validoi() {
            if (_.size(arguments) < 2 || !_.all(_.rest(arguments), _.isFunction)) {
                return false;
            }
            var data = _.first(arguments);
            var validaattorit = _.rest(arguments);
            var virheet = [];

            function next(err?) {
                if (err) {
                    virheet.push(err);
                }
                if (_.isEmpty(validaattorit)) {
                    return virheet;
                } else {
                    var seuraavaValidaattori = _.first(validaattorit);
                    validaattorit = _.rest(validaattorit);
                    return seuraavaValidaattori(data, next);
                }
            }
            return next();
        }

        function rakennaVaroitus(cellnro, name, warning, severe = false) {
            return {
                cell: cellnro,
                name: name,
                warning: warning,
                severe: severe
            };
        }

        function rakennaVirhe(cellnro, expected, actual) {
            return {
                cell: cellnro,
                expected: expected,
                actual: actual
            };
        }

        function puhdistaString(str) {
            return str.trim().toLowerCase();
        }

        // Checks if headers look the same
        function validoiOtsikot(data, tyyppi) {
            return function(data, next) {
                _.forEach(osatutkintoMap[tyyppi].otsikot, function(value, key) {
                    var expected = value;
                    var actual = data[key] ? data[key].v : "";
                    if (puhdistaString(expected) !== puhdistaString(actual)) {
                        return next(rakennaVirhe(key, expected, actual));
                    }
                });
                return next();
            };
        }

        function validoiRivit(data, next) {
            return next();
        }

        function suodataTekstipala(teksti) {
            return Algoritmit.normalizeTeksti(teksti);
        }

        function fify(obj, ids, kentat) {
            var newobj = _.clone(obj);
            _.forEach(ids, function(id) {
                var field: any = [kentat[id]];
                var value = obj[field];
                newobj[field] = {};
                newobj[field].fi = suodataTekstipala(value);
            });
            return newobj;
        }

        function readPerusteet(data, tyyppi) {
            notifier("excel-luetaan-tekstikappaleita");
            var height = ExcelSheetService.sheetHeight(data);
            var kentat = tutkintoMap[tyyppi].kentat;
            var peruste: any = {};

            _.forEach(tutkintoMap.parsittavatKentat, function(value, key) {
                peruste[value] = [];
                for (var i = 2; i < height; ++i) {
                    var solu = kentat[key] + i;
                    var arvo = data[solu];
                    if (arvo && arvo.v) {
                        var suodatettu = suodataTekstipala(arvo.v);
                        peruste[value] = suodatettu;
                    }
                }
            });

            peruste = fify(peruste, tutkintoMap.lokalisointi, tutkintoMap.parsittavatKentat);

            peruste.tekstikentat = [];
            _.forEach(tutkintoMap[tyyppi].tekstikentat, function(col) {
                var otsikko = data[col + 1];

                if (otsikko && otsikko.v) {
                    var tekstikentta = {
                        nimi: {
                            fi: suodataTekstipala(otsikko.v)
                        },
                        teksti: {
                            fi: ""
                        }
                    };
                    for (var i = 2; i < height; ++i) {
                        var solu = data[col + i];
                        if (solu && solu.v) {
                            tekstikentta.teksti.fi += "<p>" + suodataTekstipala(solu.v) + "</p>";
                        }
                    }
                    peruste.tekstikentat.push(tekstikentta);
                }
            });
            return peruste;
        }

        function readOsaperusteet(data, tyyppi) {
            notifier("excel-luetaan-tutkinnonosia");
            var height = ExcelSheetService.sheetHeight(data);
            var anchors = ExcelSheetService.getOsaAnchors(data, tyyppi, osatutkintoMap);
            var osaperusteet: any = [];
            var varoitukset: any = [];
            var virheet: any = [];
            var kentat = osatutkintoMap[tyyppi].kentat;
            var arviointiasteikko = osatutkintoMap[tyyppi].asetukset.arviointiasteikko;
            var tyydyttavat: any = [];
            var hyvat: any = [];
            var kiitettavat: any = [];
            var viimeinenKohde: any = {};

            function filtteroituKentta(id, index) {
                var cell = data[kentat[id] + index];
                return cell ? Algoritmit.normalizeTeksti(suodataTekstipala(cell.v)) : "";
            }

            function lisaaOsaamistasonKriteeri(kohde) {
                if (kohde && kohde._arviointiAsteikko === "2") {
                    kohde.osaamistasonKriteerit = [
                        {
                            _osaamistaso: "2",
                            kriteerit: _.clone(tyydyttavat)
                        },
                        {
                            _osaamistaso: "3",
                            kriteerit: _.clone(hyvat)
                        },
                        {
                            _osaamistaso: "4",
                            kriteerit: _.clone(kiitettavat)
                        }
                    ];
                    tyydyttavat = [];
                    hyvat = [];
                    kiitettavat = [];
                }
            }

            function lisaaKriteeri(osa, index) {
                function perustutkintoHelper(kriteeristo, id) {
                    var k = filtteroituKentta(id, index);
                    if (k) {
                        kriteeristo.push({ fi: k });
                    }
                }

                // Kriteereiden parsiminen kohteille
                if (!_.isEmpty(viimeinenKohde)) {
                    // (Erikois)ammattitutkinto
                    if (arviointiasteikko === "1") {
                        var okt = viimeinenKohde.osaamistasonKriteerit;
                        var kriteeri = data[kentat[8] + index];

                        // Uuden kriteerin lisääminen kohteeseen
                        if (kriteeri && kriteeri.v) {
                            if (_.isEmpty(okt)) {
                                okt.push({
                                    _osaamistaso: "1",
                                    kriteerit: []
                                });
                            }
                            _.last(okt)["kriteerit"].push({
                                fi: suodataTekstipala(kriteeri.v)
                            });
                        }
                    } else {
                        // Perustutkinto
                        perustutkintoHelper(tyydyttavat, 9);
                        perustutkintoHelper(hyvat, 10);
                        perustutkintoHelper(kiitettavat, 11);
                    }
                } else {
                    if (arviointiasteikko === "1") {
                        varoitukset.push(
                            rakennaVaroitus(kentat[8] + index, osa.nimi, "Arvioinnin kohdetta ei löytynyt")
                        );
                    } else if (arviointiasteikko === "2") {
                        varoitukset.push(
                            rakennaVaroitus(
                                kentat[9] + index + ", " + kentat[10] + index + ", " + kentat[11] + index,
                                osa.nimi,
                                "Arvioinnin kohdetta ei löytynyt"
                            )
                        );
                    }
                }
            }

            function lisaaKohdealue(arvioinninKohdealue, index) {
                var kohde = data[kentat[7] + index];
                if (kohde && kohde.v) {
                    if (!_.isEmpty(arvioinninKohdealue)) {
                        if (!_.isEmpty(viimeinenKohde)) {
                            lisaaOsaamistasonKriteeri(viimeinenKohde);
                            arvioinninKohdealue.arvioinninKohteet.push(viimeinenKohde);
                        }
                        viimeinenKohde = {
                            otsikko: {
                                fi: suodataTekstipala(kohde.v)
                            },
                            _arviointiAsteikko: arviointiasteikko,
                            osaamistasonKriteerit: []
                        };
                    } else {
                        varoitukset.push(rakennaVaroitus(kentat[8] + index, "", "Arvioinnin kohdealuetta ei löytynyt"));
                    }
                }
            }

            _.forEach(anchors, function(anchor, index: any) {
                var osa: any = {};

                _.forEach(_.pick(osatutkintoMap.parsittavatKentat, osatutkintoMap.info), function(value: any, key) {
                    var solu = kentat[key] + anchor;
                    var arvo = "";
                    if (data[solu]) {
                        var numero = parseInt(data[solu].v, 10);
                        arvo = _.isNaN(numero) ? data[solu].v : numero;
                    }
                    osa[value] = arvo;

                    var virhe = osatutkintoMap.virheet[key];
                    var varoitus = osatutkintoMap.varoitukset[key];

                    if (!arvo || !value) {
                        var nimi = suodataTekstipala(osa.nimi);
                        if (virhe) {
                            virheet.push(rakennaVaroitus(solu, nimi, virhe, true));
                        } else if (varoitus) {
                            varoitukset.push(rakennaVaroitus(solu, nimi, varoitus));
                        }
                    }
                });

                osa = fify(osa, osatutkintoMap.lokalisointi, osatutkintoMap.parsittavatKentat);

                osa.ammattitaidonOsoittamistavat = {};
                osa.ammattitaidonOsoittamistavat.fi = "";
                osa.arviointi = {};
                osa.arviointi.lisatiedot = {};
                osa.arviointi.lisatiedot.fi = "";
                osa.arviointi.arvioinninKohdealueet = [];

                var nextAnchor = index < anchors.length - 1 ? anchors[index + 1] : height;
                var arvioinninKohdealue: any = {};
                var sizeRange = _.range(anchor, nextAnchor + 1);

                _.forEach(sizeRange, function(j) {
                    // Osoittamistapojen kerääminen
                    var cell = data[kentat[5] + j];
                    if (cell && cell.v) {
                        osa.ammattitaidonOsoittamistavat.fi += "<p>" + suodataTekstipala(cell.v) + "</p>";
                    }

                    // ArvioinninKohdealueiden lisääminen
                    cell = data[kentat[6] + j];
                    if (cell && cell.v) {
                        if (!_.isEmpty(arvioinninKohdealue)) {
                            lisaaOsaamistasonKriteeri(viimeinenKohde);
                            arvioinninKohdealue.arvioinninKohteet.push(viimeinenKohde);
                            viimeinenKohde = {};
                            osa.arviointi.arvioinninKohdealueet.push(_.clone(arvioinninKohdealue));
                        }
                        arvioinninKohdealue = {};
                        arvioinninKohdealue.arvioinninKohteet = [];
                        arvioinninKohdealue.otsikko = {
                            fi: suodataTekstipala(cell.v)
                        };
                    }

                    lisaaKohdealue(arvioinninKohdealue, j);
                    lisaaKriteeri(osa, j);
                });

                if (!_.isEmpty(arvioinninKohdealue)) {
                    lisaaOsaamistasonKriteeri(viimeinenKohde);
                    arvioinninKohdealue.arvioinninKohteet.push(viimeinenKohde);
                    viimeinenKohde = {};
                    if (index === _.size(anchors) - 1) {
                        osa.arviointi.arvioinninKohdealueet.push(_.clone(arvioinninKohdealue));
                    }
                }
                osaperusteet.push(_.clone(osa));
            });

            return {
                osaperusteet: osaperusteet,
                varoitukset: varoitukset,
                virheet: virheet
            };
        }

        // Tässä kannattaa tehdä puukotukset
        function manipuloi(sheet, tyyppi) {
            if (tyyppi === "perustutkinto" && sheet.AO1 && sheet.AO1.v !== "Koulutusohjelma / osaamisala") {
                ExcelSheetService.poistaSarake(sheet, "AO");
            }
        }

        // Konvertoi parsitun XLSX-tiedoston perusteen osiksi.
        // $q:n notifyä käytetään valmistumisen päivittämiseen.
        // Palauttaa lupauksen.
        function toJson(parsedxlsx, tyyppi) {
            if (tyyppi !== "perustutkinto") {
                tyyppi = "ammattitutkinto";
            }

            var deferred = $q.defer();
            if (_.isEmpty(parsedxlsx.SheetNames)) {
                deferred.reject(1);
            } else {
                notifier("excel-parsitaan-perustietoja");
                var name = parsedxlsx.SheetNames[0];
                var sheet = parsedxlsx.Sheets[name];

                manipuloi(sheet, tyyppi);
                // var err = validoi(sheet, validoiOtsikot(sheet, tyyppi), validoiRivit);
                var err = validoi();

                if (err.length > 0) {
                    deferred.reject(err);
                } else {
                    deferred.resolve({
                        peruste: readPerusteet(sheet, tyyppi),
                        osatutkinnot: readOsaperusteet(sheet, tyyppi)
                    });
                }
            }
            return deferred.promise;
        }

        function parseXLSXToOsaperuste(file, tyyppi, notifierCb) {
            notifier = notifierCb || angular.noop;
            notifier("excel-avataan-tiedostoa");
            var tiedosto = XLSX.read(file, { type: "binary" });
            return toJson(tiedosto, tyyppi);
        }

        return {
            parseXLSXToOsaperuste: parseXLSXToOsaperuste
        };
    });

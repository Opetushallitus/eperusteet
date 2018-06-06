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
import _ from "lodash";
import Restangular from "restangular";


angular
    .module("eperusteApp")
    .run(($templateCache) => {
        // $templateCache.put(
        //     "ammattitaitovaatimukset2018.html",
        //     require("views/partials/tutkinnonosa/ammattitaitovaatimukset.pug")());
        // $templateCache.put(
        //     "ammattitaitovaatimus2018.html",
        //     require("views/partials/tutkinnonosa/ammattitaitovaatimus.pug")());
    })
    .controller("tutkinnonOsa2018Ctrl", (
        $stateParams,
        $timeout,
        $scope,
        Algoritmit,
        Kaanna,
        Arviointiasteikot,
        Editointikontrollit,
        Lukitus,
        PerusteenOsat,
        Utils,
        Api,
        perusteprojektiTiedot,
    ) => {
        $scope.loading = true;
        $scope.editEnabled = false;
        const tosaApi: Restangular = Api
            .one("perusteet", perusteprojektiTiedot.getPeruste().id)
            .one("suoritustavat", $stateParams.suoritustapa)
            .all("tutkinnonosat");

        async function lukitse() {
            // return new Promise((resolve, reject) => Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, resolve));
        }

        function splitKohdeList(objs: Array<Object>, lang: String, field: String) {
            const strs: any = _(objs)
                .map(field)
                .map(lang)
                .value();
            const firstWords = Algoritmit.findFirstWords(strs) || [];

            return {
                kohde: firstWords.join(" "),
                lista: _(strs)
                    .map(str => str.split(" "))
                    .map(arr => arr.slice(_.size(firstWords)))
                    .map(arr => arr.join(" "))
                    .value(),
            }
        }

        $scope.lisaaKriteeri = (kriteerit) => {
            kriteerit.push({
                kuvaus: {}
            });
        };

        const lisaaPuuttuvatOsaamistasot = (osa, arviointiasteikot) => {
            for (const vaatimus of osa.ammattitaitovaatimukset) {
                const asteikko = arviointiasteikot[vaatimus.arviointiasteikko_id];
                const loydetyt = _.map(vaatimus.osaamistasot, "osaamistaso_id");
                const vaaditut = _(asteikko.osaamistasot)
                    .keys()
                    .map(_.parseInt)
                    .value();
                for (const id of vaaditut) {
                    if (!_.includes(loydetyt, id)) {
                        vaatimus.osaamistasot.push({
                            osaamistaso_id: id,
                            kriteerit: []
                        });
                    }
                }
            }
        };

        const updateTosa = (tov, arviointiasteikot) => {
            lisaaPuuttuvatOsaamistasot(tov.tutkinnonOsa, arviointiasteikot);
            $scope.tov = tov;
            for (const vaatimus of tov.tutkinnonOsa.ammattitaitovaatimukset) {
                for (const osaamistaso of vaatimus.osaamistasot) {
                    osaamistaso.$$simplifiedKriteerit = splitKohdeList(osaamistaso.kriteerit, "fi", "kuvaus");
                }
            }
            $scope.ammattitaitovaatimukset = splitKohdeList(tov.tutkinnonOsa.ammattitaitovaatimukset, "fi", "nimi");
        };

        $scope.toggleVaatimus = (vaatimus) => {
            vaatimus.$$open = !vaatimus.$$open;
        };

        $scope.toggleArviointi = () => {
            const auki = _.all($scope.tov.tutkinnonOsa.ammattitaitovaatimukset, (vaatimus: any) => vaatimus.$$open);
            for (const vaatimus of $scope.tov.tutkinnonOsa.ammattitaitovaatimukset) {
                vaatimus.$$open = !auki;
            }
        };

        $scope.poistaKriteeri = (kriteerit, kriteeri) => {
            if (kriteeri.$$klikattu) {
                $timeout(() => {
                    _.remove(kriteerit, kriteeri);
                });
            }
            else {
                kriteeri.$$klikattu = true;
                kriteeri.$$poistoTimeout = 5;
                (function poistofn() {
                    if (kriteeri.$$poistoTimeout > 1) {
                        kriteeri.$$poistoTimeout -= 1;
                        $timeout(poistofn, 1000);
                    }
                    else {
                        kriteeri.$$klikattu = false;
                    }
                })();
            }
        };

        // $timeout($scope.toggleArviointi, 500);

        async function load() {
            $scope.loading = true;
            $scope.arviointiasteikot = _(await Arviointiasteikot.list().$promise)
                .map(asteikko => ({
                    ...asteikko,
                    osaamistasot: _.indexBy(asteikko.osaamistasot, "id")
                }))
                .indexBy("id")
                .value();
            updateTosa(await fetch(), $scope.arviointiasteikot);
            $scope.loading = false;
        };
        load();

        $scope.sortableOptionsKriteerit = {
            axis: "y",
            connectWith: ".sortable-kriteerit",
            cursor: "move",
            cursorAt: { top: 10, left: 10 },
            delay: 100,
            forceHelperSize: true,
            forcePlaceholderSize: true,
            handle: ".handle",
            opacity: ".7",
            placeholder: "sortable-placeholder",
            tolerance: "pointer"
        };

        $scope.sortableOptionsAmmattitaitovaatimukset = {
            handle: ".handle",
            cursor: "move",
            cursorAt: { top: 10, left: 10 },
            forceHelperSize: true,
            placeholder: "sortable-placeholder",
            forcePlaceholderSize: true,
            opacity: ".7",
            delay: 100,
            axis: "y",
            tolerance: "pointer"
        };

        async function fetch() {
            return _.cloneDeep(osa);
            // const res = await PerusteenOsat.get({ osanId: $scope.tutkinnonOsaViite.tutkinnonOsa.id }).$promise;
            // , res => {
            //     $scope.tutkinnonOsaViite.tutkinnonOsa = res;
            // }).$promise;
        }

        $scope.muokkaa = async () => {
            // await MuutProjektitService.varmistusdialogi($scope.editableTutkinnonOsaViite.tutkinnonOsa.id);
            try {
                await lukitse();
                const tov = await fetch();
                $timeout(Editointikontrollit.startEditing);
            }
            catch (err) {
                console.log(err);
            }
        };

        // FIXME poista
        // $timeout($scope.muokkaa, 100);

        $scope.ammattitaitovaatimus = {
            aloitaLisays(tov) {
                $scope.uusiAmmattitaitovaatimus = {
                    $$open: true,
                    nimi: {},
                    osaamistasot: [],
                };
            },

            peruutaLisays() {
                $scope.uusiAmmattitaitovaatimus = undefined;
            },

            tallenna(vaatimus) {
                $scope.tov.tutkinnonOsa.ammattitaitovaatimukset.push($scope.uusiAmmattitaitovaatimus);
                lisaaPuuttuvatOsaamistasot($scope.tov.tutkinnonOsa, $scope.arviointiasteikot);
                $scope.uusiAmmattitaitovaatimus = undefined;
            }
        };

        Editointikontrollit.registerCallback({
            async edit() {
                $scope.editEnabled = true;
            },
            asyncValidate(cb) {
                cb();
            },
            async asyncSave(kommentti) {
                osa = _.cloneDeep($scope.tov);
                console.log("sending", $scope.tov);
                const res = await tosaApi.post(
                    $scope.tov,
                    {},
                    { versio: 2 });
                console.log(res);
                $scope.editEnabled = false;
                load();
            },
            async cancel() {
                await load();
                $scope.editEnabled = false;
            },
            notify(mode) {
            }
        });
    });

var osa = {
    tutkinnonOsa: {
        "id" : 2918469,
        "luotu" : 1504251706547,
        "muokattu" : 1530698485270,
        "muokkaaja" : "1.2.246.562.24.43047694054",
        "nimi" : {
        "_tunniste" : "8c13ee48-8a45-47ef-8142-6a180c5f4688",
        "fi" : "Liikunnan toimintaympäristössä toimiminen",
        "_id" : "2943317"
        },
        "tila" : "valmis",
        "valmis" : null,
        "kaannettava" : null,
        "osanTyyppi" : "tutkinnonosa2018",
        ammattitaitovaatimukset: [{
            id: 41,
            arviointiasteikko_id: 1,
            nimi: {
                fi: "Opiskelija suunnittelee kaikkea todella jännittävää."
            },
            osaamistasot: [{
                osaamistaso_id: 1,
                kriteerit: [{
                    id: 200,
                    kuvaus: { fi: "Opiskelija ottaa huomioon kulttuurisen moninaisuuden" }
                }, {
                    id: 201,
                    kuvaus: { fi: "Opiskelija arvioi ja laskee valmistettavien ruokatuotteiden raaka-aineiden menekin ruokaohjeita tai tuotannon ohjausjärjestelmää hyödyntäen" }
                }, {
                    id: 202,
                    kuvaus: { fi: "Opiskelija ajoittaa ja jaksottaa työvuoronsa työtehtävät niin, että ruokatuotteet valmistuvat oikeaan aikaan ja laatutavoitteiden mukaisesti" }
                }, {
                    id: 203,
                    kuvaus: { fi: "Opiskelija suunnittelee oman työnsä kestävän kehityksen mukaisesti huomioiden toimintaympäristön energiatehokkuuden" }
                }, {
                    id: 204,
                    kuvaus: { fi: "Opiskelija suunnittelee oman työpanoksensa niin, että osaa tukea ja hyödyntää työryhmän yhteistä tuotantoprosessia" }
                }, {
                    id: 205,
                    kuvaus: { fi: "Opiskelija selvittää lasten fyysisen aktiivisuuden ja liikunnallisen tason tarkoituksenmukaisilla menetelmillä" }
                }]
            }]
        }, {
            id: 42,
            arviointiasteikko_id: 3,
            nimi: {
                fi: "Opiskelija suunnittelee lasten liikuntaa motoristen perustaitojen kehittämiseksi."
            },
            osaamistasot: [{
                osaamistaso_id: 5,
                kriteerit: [{
                    id: 100,
                    kuvaus: { fi: "Opiskelija ottaa huomioon kulttuurisen moninaisuuden" }
                }, {
                    id: 101,
                    kuvaus: { fi: "Opiskelija arvioi ja laskee valmistettavien ruokatuotteiden raaka-aineiden menekin ruokaohjeita tai tuotannon ohjausjärjestelmää hyödyntäen" }
                }, {
                    id: 102,
                    kuvaus: { fi: "Opiskelija ajoittaa ja jaksottaa työvuoronsa työtehtävät niin, että ruokatuotteet valmistuvat oikeaan aikaan ja laatutavoitteiden mukaisesti" }
                }, {
                    id: 103,
                    kuvaus: { fi: "Opiskelija suunnittelee oman työnsä kestävän kehityksen mukaisesti huomioiden toimintaympäristön energiatehokkuuden" }
                }, {
                    id: 104,
                    kuvaus: { fi: "Opiskelija suunnittelee oman työpanoksensa niin, että osaa tukea ja hyödyntää työryhmän yhteistä tuotantoprosessia" }
                }, {
                    id: 105,
                    kuvaus: { fi: "Opiskelija selvittää lasten fyysisen aktiivisuuden ja liikunnallisen tason tarkoituksenmukaisilla menetelmillä" }
                }]
            }, {
                osaamistaso_id: 6,
                kriteerit: [{
                    id: 106,
                    kuvaus: { fi: "Opiskelija a" }
                }, {
                    id: 107,
                    kuvaus: { fi: "Opiskelija b" }
                }]
            }, {
                osaamistaso_id: 7,
                kriteerit: [{
                    id: 108,
                    kuvaus: { fi: "x" }
                }, {
                    id: 109,
                    kuvaus: { fi: "y" }
                }]
            }]
        }],
        "kuvaus" : {
        "_tunniste" : "2713f71c-dd99-4f25-861b-8ff13714b314",
        "fi" : "<p>Kuvaus</p>",
        "_id" : "3927981"
        },
        "ammattitaidonOsoittamistavat" : {
        "_tunniste" : "2712f71c-dd99-4f25-861b-8ff13714b314",
        "fi" : "<p>Opiskelija osoittaa ammattitaitonsa näytössä käytännön työtehtävissä toteuttamalla tehtäväkokonaisuuden liikunnan toimintaympäristössä. Siltä osin kuin tutkinnon osassa vaadittua ammattitaitoa ei voida arvioida näytön perusteella, ammattitaidon osoittamista täydennetään yksilöllisesti muilla tavoin.</p>",
        "_id" : "3927980"
        },
        "opintoluokitus" : null,
        "koodi" : {
        "nimi" : {
            "fi" : "Liikunnan toimintaympäristössä toimiminen"
        },
        "arvo" : "200790",
        "uri" : "tutkinnonosat_200790",
        "koodisto" : "tutkinnonosat",
        "versio" : null
        },
        "tyyppi" : "normaali",
        "valmaTelmaSisalto" : null
    }
}


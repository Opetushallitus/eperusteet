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

angular
    .module("eperusteApp")
    .controller(
        "PerusteprojektiMuodostumissaannotCtrl",
        (
            $scope,
            $stateParams,
            $timeout,
            PerusteenRakenne,
            Notifikaatiot,
            Editointikontrollit,
            PerusteProjektiService,
            Kommentit,
            KommentitBySuoritustapa,
            Lukitus,
            VersionHelper,
            Muodostumissaannot,
            virheService,
            PerusteProjektiSivunavi,
            perusteprojektiTiedot,
            $q,
            Varmistusdialogi,
            Algoritmit,
            PerusteenOsat
        ) => {
            $scope.editoi = false;
            $scope.suoritustapa = $stateParams.suoritustapa;
            $scope.rakenne = {
                $resolved: false,
                rakenne: { osat: [] },
                tutkinnonOsat: {}
            };
            $scope.versiot = {};
            $scope.isLocked = false;
            $scope.peruste = perusteprojektiTiedot.getPeruste();
            $scope.sisalto = perusteprojektiTiedot.getSisalto();
            let muodostumisOtsikko = null;

            function setOtsikko(value = muodostumisOtsikko) {
                $scope.rakenne.muodostumisOtsikko = value;
            }

            Kommentit.haeKommentit(KommentitBySuoritustapa, {
                id: $stateParams.perusteProjektiId,
                suoritustapa: $scope.suoritustapa
            });

            function lukitse(cb) {
                Lukitus.lukitseSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa, cb);
            }

            function errorCb() {
                virheService.virhe("virhe-perusteenosaa-ei-löytynyt");
            }

            function successCb(res) {
                res.$suoritustapa = $scope.suoritustapa;
                res.$resolved = true;
                $scope.rakenne = res;
                setOtsikko();
                Muodostumissaannot.laskeLaajuudet(
                    $scope.rakenne.rakenne,
                    $scope.rakenne.tutkinnonOsaViitteet,
                    $scope.rakenne.tutkinnonOsat
                );
                haeVersiot();
                Lukitus.tarkista($scope.rakenne.$peruste.id, $scope, $scope.suoritustapa);
            }

            function haeRakenne(versio = undefined) {
                Algoritmit.kaikilleLapsisolmuille($scope.sisalto, "lapset", lapsi => {
                    if (lapsi.perusteenOsa && lapsi.perusteenOsa.tunniste === "rakenne") {
                        muodostumisOtsikko = _.cloneDeep(lapsi.perusteenOsa);
                        setOtsikko();
                        return true;
                    }
                });

                return $q(function(resolve) {
                    haeVersiot(true, async function() {
                        const revNumber = VersionHelper.select($scope.versiot, versio);
                        if (versio && !$scope.versiot.latest) {
                            if (!revNumber) {
                                errorCb();
                            } else {
                                PerusteenRakenne.haeTutkinnonosatVersioByPeruste(
                                    $scope.peruste.id,
                                    $scope.suoritustapa,
                                    revNumber,
                                    function(tutkinnonOsat) {
                                        const vastaus = PerusteenRakenne.pilkoTutkinnonOsat(tutkinnonOsat, {});
                                        $scope.tutkinnonOsat = vastaus.tutkinnonOsat;
                                        VersionHelper.changeRakenne(
                                            $scope.versiot,
                                            {
                                                id: $scope.peruste.id,
                                                suoritustapa: $scope.suoritustapa
                                            },
                                            function(response) {
                                                $scope.rakenne.rakenne = response;
                                                $scope.rakenne.$resolved = true;
                                                $scope.rakenne.$suoritustapa = $scope.suoritustapa;
                                                $scope.rakenne.$peruste = $scope.peruste;
                                                $scope.rakenne.tutkinnonOsat = vastaus.tutkinnonOsat;
                                                $scope.rakenne.tutkinnonOsaViitteet = vastaus.tutkinnonOsaViitteet;
                                                resolve();
                                            }
                                        );
                                    }
                                );
                            }
                        } else {
                            const res = await PerusteenRakenne.haeByPerusteprojekti(
                                $stateParams.perusteProjektiId,
                                $scope.suoritustapa
                            );
                            successCb(res);
                            resolve();
                        }
                    });
                });
            }

            $scope.haeRakenne = haeRakenne;
            const versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;

            haeRakenne(versio).then(() => {
                Editointikontrollit.registerCallback({
                    edit: () => {
                        $scope.editoi = true;
                    },
                    asyncValidate: cb => {
                        lukitse(() => {
                            Muodostumissaannot.skratchpadNotEmpty()
                                .then(isEmpty => {
                                    return isEmpty ? leikelautaDialogi() : $q.when();
                                })
                                .then(() => {
                                    Editointikontrollit.notifySentenceCaseWarnings({
                                        obj: $scope.rakenne.muodostumisOtsikko,
                                        paths: [
                                            'nimi'
                                        ],
                                        after: cb
                                    });
                                });
                        });
                    },
                    asyncSave: (kommentti, cb) => {
                        $scope.rakenne.rakenne.metadata = { kommentti: kommentti };
                        cb();
                        tallennaRakenne($scope.rakenne, () => {
                            $scope.editoi = false;
                        });
                    },
                    cancel: () => {
                        return $q((resolve, reject) => {
                            Muodostumissaannot.skratchpadNotEmpty()
                                .then(isEmpty => {
                                    return isEmpty ? leikelautaDialogi() : $q.when();
                                })
                                .then(Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa))
                                .then(haeRakenne)
                                .then(() => {
                                    $scope.editoi = false;
                                })
                                .then(resolve);
                        });
                    }
                });
            });

            function tallennaRakenne(rakenne, cb) {
                cb = cb || _.noop;
                // TODO Jos tallennettaisiin otsikkotekstikappale viitteen läpi, ei tarvitsisi erillistä perusteenosalukkoa.
                // TODO optimointi: älä tallenna otsikkoa jos sitä ei muutettu
                const otsikkoId = $scope.rakenne.muodostumisOtsikko.id;
                Lukitus.lukitsePerusteenosa(otsikkoId, function() {
                    PerusteenOsat.saveTekstikappale(
                        { osanId: otsikkoId },
                        $scope.rakenne.muodostumisOtsikko,
                        function() {
                            PerusteProjektiSivunavi.refresh();
                            Lukitus.vapautaPerusteenosa(otsikkoId);
                        },
                        function(res) {
                            Lukitus.vapautaPerusteenosa(otsikkoId);
                            Notifikaatiot.serverCb(res);
                        }
                    );
                });

                PerusteenRakenne.tallennaRakenne(
                    rakenne,
                    rakenne.$peruste.id,
                    $scope.suoritustapa,
                    function() {
                        Notifikaatiot.onnistui("tallennus-onnistui");
                        haeVersiot(true, function() {
                            VersionHelper.setUrl($scope.versiot);
                        });
                        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
                        cb();
                    },
                    function() {
                        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
                        cb();
                    }
                );

                $scope.isLocked = false;
            }

            function haeVersiot(force = false, cb = _.noop) {
                VersionHelper.getRakenneVersions(
                    $scope.versiot,
                    {
                        id: $scope.peruste.id,
                        suoritustapa: $scope.suoritustapa
                    },
                    force,
                    cb
                );
            }

            $scope.vaihdaVersio = function(cb) {
                cb = cb || angular.noop;
                $scope.versiot.hasChanged = true;
                // Ideally we would reload the data and rewrite version to url without changing state
                VersionHelper.setUrl($scope.versiot);
                //      VersionHelper.changeRakenne($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, function(response) {
                //        console.log('cb kutsuttu', response);
                //        $scope.rakenne.rakenne = response;
                //        $scope.rakenne.$resolved = true;
                //        //VersionHelper.setUrl($scope.versiot, true);
                //        cb();
                //      });
            };

            $scope.revert = function() {
                haeRakenne().then(function() {
                    Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
                    haeVersiot(true);
                });
            };

            $scope.muokkaa = function() {
                lukitse(function() {
                    haeRakenne().then(function() {
                        $scope.rakenne.rakenne.$virheetMaara = Muodostumissaannot.validoiRyhma(
                            $scope.rakenne.rakenne,
                            $scope.rakenne.tutkinnonOsaViitteet,
                            $scope.tutkinnonOsat
                        );
                        Editointikontrollit.startEditing();
                        $scope.editoi = true;
                    });
                });
            };

            function leikelautaDialogi() {
                return $q((resolve, reject) => {
                    Varmistusdialogi.dialogi({
                        otsikko: "vahvista-liikkuminen",
                        teksti: "leikelauta-varoitus",
                        lisaTeksti: "haluatko-jatkaa",
                        successCb: resolve,
                        failureCb: reject,
                        primaryBtn: "poistu-sivulta"
                    })();
                });
            }

            $scope.$watch(
                "rakenne.rakenne",
                function(uusirakenne) {
                    if ($scope.editoi) {
                        Muodostumissaannot.laskeLaajuudet(uusirakenne, $scope.rakenne.tutkinnonOsaViitteet);
                        uusirakenne.$virheetMaara = Muodostumissaannot.validoiRyhma(
                            uusirakenne,
                            $scope.rakenne.tutkinnonOsaViitteet
                        );
                    }
                },
                true
            );

            $scope.$watch("editoi", function(editoi) {
                PerusteProjektiSivunavi.setVisible(!editoi);
            });
        }
    );

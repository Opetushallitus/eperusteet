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
    .service("TutkinnonOsanTuonti", function($uibModal) {
        function suoritustavoista(peruste, nykyinenTyyppi, successCb, failureCb) {
            failureCb = failureCb || function() {};
            return function() {
                $uibModal
                    .open({
                        template: require("views/modals/tuotutkinnonosasta.html"),
                        controller: "TuoTutkinnonOsaSuoritustavastaaCtrl",
                        resolve: {
                            peruste: function() {
                                return peruste;
                            },
                            suoritustapa: function() {
                                return nykyinenTyyppi;
                            }
                        }
                    })
                    .result.then(successCb, failureCb);
            };
        }

        function kaikista(tyyppi, successCb, failureCb) {
            failureCb = failureCb || function() {};
            return function() {
                $uibModal
                    .open({
                        template: require("views/modals/haetutkinnonosa.html"),
                        controller: "TuoTutkinnonOsaCtrl",
                        resolve: {
                            tyyppi: () => {
                                return tyyppi;
                            }
                        }
                    })
                    .result.then(successCb, failureCb);
            };
        }

        return {
            kaikista: kaikista,
            suoritustavoista: suoritustavoista
        };
    })
    .controller("TuoTutkinnonOsaSuoritustavastaaCtrl", function(
        PerusteenOsat,
        $scope,
        $uibModalInstance,
        peruste,
        PerusteTutkinnonosat,
        Notifikaatiot,
        suoritustapa,
        Algoritmit,
        Kaanna
    ) {
        $scope.tulokset = [];
        $scope.valitut = 0;
        $scope.peruste = peruste;
        $scope.kaikkiValittu = false;
        $scope.suoritustavat = _(peruste.suoritustavat)
            .map("suoritustapakoodi")
            .reject(function(st) {
                return st === suoritustapa;
            })
            .value();
        $scope.valittuSuoritustapa = $scope.suoritustavat[0];

        $scope.paginate = {
            perPage: 10,
            current: 1
        };
        $scope.search = {
            term: "",
            changed: function() {
                $scope.paginate.current = 1;
            },
            filterFn: function(item) {
                return Algoritmit.match($scope.search.term, item.nimi);
            }
        };

        $scope.orderFn = function(item) {
            return Kaanna.kaanna(item.nimi).toLowerCase();
        };

        $scope.updateTotal = function() {
            $scope.valitut = _.size(_.filter($scope.tulokset, "$$valitse"));
        };

        $scope.valitseKaikki = function(valinta) {
            _.each($scope.tulokset, function(tulos) {
                tulos.$$valitse = false;
                if ($scope.search.term) {
                    if ($scope.search.filterFn(tulos)) {
                        tulos.$$valitse = valinta;
                    }
                } else {
                    tulos.$$valitse = valinta;
                }
            });
            $scope.updateTotal();
        };

        $scope.vaihdaValinta = function(tulos) {
            tulos.$$valitse = !tulos.$$valitse;
            $scope.updateTotal();
        };

        $scope.paivitaTulokset = function(st) {
            if (typeof st === "undefined") {
                return;
            }
            PerusteTutkinnonosat.get(
                {
                    perusteId: peruste.id,
                    suoritustapa: st
                },
                function(res) {
                    $scope.tulokset = _(res)
                        .map(function(osa) {
                            return {
                                _tutkinnonOsa: osa._tutkinnonOsa,
                                nimi: osa.nimi
                            };
                        })
                        .value();
                },
                Notifikaatiot.serverCb
            );
        };
        $scope.paivitaTulokset($scope.valittuSuoritustapa);

        $scope.valittuSuoritustapa = $scope.suoritustavat[0];
        $scope.paivitaTulokset($scope.valittuSuoritustapa);

        $scope.ok = function() {
            $uibModalInstance.close(
                _.filter($scope.tulokset, function(tulos) {
                    return tulos.$$valitse;
                })
            );
        };
        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };
    })
    .controller("TuoTutkinnonOsaCtrl", function($scope, $uibModalInstance, PerusteetInternal, PerusteKaikki, Utils) {
        $scope.haku = true;
        $scope.perusteet = [];
        $scope.osat = [];
        $scope.luonnosPerusteet = [];
        $scope.valittu = {};
        $scope.sivukoko = 10;
        $scope.data = {
            nykyinensivu: 1,
            hakustr: "",
            tila: "valmis"
        };

        $scope.takaisin = () => ($scope.haku = true);

        $scope.valitse = () =>
            $uibModalInstance.close(
                _.filter($scope.osat, {
                    $$valitse: true
                })
            );

        $scope.paivitaHaku = sivu => {
            if (sivu) {
                $scope.data.nykyinensivu = sivu;
            }

            let pquery: any = {
                nimi: $scope.data.hakustr,
                sivukoko: $scope.sivukoko,
                sivu: $scope.data.nykyinensivu - 1
            };

            if (!$scope.luonnokset) {
                pquery.tila = "valmis";
            }

            PerusteetInternal.get(pquery, perusteet => ($scope.perusteet = perusteet));
        };

        $scope.hakuMuuttui = _.debounce(_.bind($scope.paivitaHaku, $scope), 300);

        $scope.jatka = parent => {
            $scope.haku = false;
            $scope.valittu = parent;

            PerusteKaikki.get(
                {
                    perusteId: parent.id
                },
                peruste => {
                    $scope.osat = [];
                    _.each(peruste.suoritustavat, suoritustapa => {
                        _.each(suoritustapa.tutkinnonOsaViitteet, tutkinnonOsaViite => {
                            const tutkinnonOsa = _.find(peruste.tutkinnonOsat, {
                                id: parseInt(tutkinnonOsaViite._tutkinnonOsa)
                            });

                            $scope.osat.push({
                                id: tutkinnonOsaViite.id,
                                laajuus: tutkinnonOsaViite.laajuus,
                                suoritustapakoodi: suoritustapa.suoritustapakoodi,
                                nimi: tutkinnonOsa.nimi,
                                tyyppi: tutkinnonOsa.tyyppi,
                                _tutkinnonOsa: tutkinnonOsaViite._tutkinnonOsa
                            });
                        });
                    });
                    _.sortBy($scope.osat, Utils.nameSort);
                }
            );
        };

        $scope.peruuta = () => $uibModalInstance.dismiss();

        $scope.paivitaHaku();
    });

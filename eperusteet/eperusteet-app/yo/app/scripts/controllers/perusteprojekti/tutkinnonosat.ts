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

angular
    .module("eperusteApp")
    .controller("PerusteprojektiTutkinnonOsatCtrl", function(
        $scope,
        $state,
        $stateParams,
        $rootScope,
        Api,
        perusteprojektiTiedot,
        PerusteProjektiService,
        PerusteenRakenne,
        Notifikaatiot,
        PerusteTutkinnonosat,
        PerusteTutkinnonosa,
        TutkinnonOsanTuonti,
        TutkinnonOsaEditMode
    ) {
        $scope.peruste = perusteprojektiTiedot.getPeruste();
        $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
        $scope.tutkinnonOsat = [];
        $scope.editoi = false;

        if (perusteprojektiTiedot.getPeruste().suoritustavat) {
            $scope.naytaToisestaSuoritustavastaTuonti = perusteprojektiTiedot.getPeruste().suoritustavat
                ? perusteprojektiTiedot.getPeruste().suoritustavat.length > 1
                : false;
        }

        $scope.yksikko = _.zipObject(
            _.map($scope.peruste.suoritustavat, "suoritustapakoodi"),
            _.map($scope.peruste.suoritustavat, "laajuusYksikko")
        );

        function haeTutkinnonosat() {
            PerusteTutkinnonosat.tilat({
                perusteId: $scope.peruste.id,
                suoritustapa: $scope.suoritustapa
            }).$promise.then(tilat => {
                const tilatMapped = _(tilat)
                    .indexBy("id")
                    .value();

                PerusteenRakenne.haeTutkinnonosat($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
                    $scope.tutkinnonOsat = _(res)
                        .map(tosa => {
                            tosa.$$tila = tilatMapped[tosa._tutkinnonOsa];
                            return tosa;
                        })
                        .value();
                });
            });
        }
        haeTutkinnonosat();

        $scope.lisaaKoodittomat = async () => {
            try {
                $scope.avDisabled = true;
                const koodit = _(await Api.all(`/perusteet/${$scope.peruste.id}/tutkinnonosat/ammattitaitovaatimuskoodisto`).customPOST())
                    .value();
                $scope.tarkistaAmmattitaitovaatimukset();
                Notifikaatiot.onnistui("koodien-lisays-onnistui");
            }
            catch (err) {
                console.error("Koodittomien lisäys epäonnistui", err);
            }
            finally {
                $scope.avDisabled = false;
            }
        };

        $scope.tarkistaAmmattitaitovaatimukset = async () => {
            try {
                $scope.avDisabled = true;
                const vaatimukset = _(await Api.all(`/perusteet/${$scope.peruste.id}/tutkinnonosat/ammattitaitovaatimukset`).getList())
                    .map(x => x.plain())
                    .value();
                $scope.koodilliset = _.filter(vaatimukset, "koodi");
                $scope.koodittomat = _.reject(vaatimukset, "koodi");
            }
            catch (err) {
                console.error("Lataus epäonnistui", err);
            }
            finally {
                $scope.avDisabled = false;
            }
        };

        $scope.tuoSuoritustavasta = TutkinnonOsanTuonti.suoritustavoista(
            perusteprojektiTiedot.getPeruste(),
            $scope.suoritustapa,
            function(osat) {
                _.forEach(osat, function(osa) {
                    $scope.lisaaTutkinnonOsaSuoraan(osa);
                });
            }
        );

        $scope.tuoTutkinnonosa = TutkinnonOsanTuonti.kaikista($scope.suoritustapa, function(osat) {
            _.forEach(osat, function(osa) {
                delete osa.id;
                $scope.lisaaTutkinnonOsaSuoraan(osa);
            });
        });

        $scope.lisaaTutkinnonOsaSuoraan = function(osa) {
            PerusteTutkinnonosa.save(
                {
                    perusteId: $scope.peruste.id,
                    suoritustapa: $stateParams.suoritustapa
                },
                osa,
                function(res) {
                    $scope.tutkinnonOsat.unshift(res);
                },
                Notifikaatiot.serverCb
            );
        };

        $scope.lisaaTutkinnonOsa = function(tyyppi) {
            var osa = tyyppi ? { tyyppi: tyyppi } : {};

            PerusteTutkinnonosa.save(
                {
                    perusteId: $scope.peruste.id,
                    suoritustapa: $stateParams.suoritustapa
                },
                osa,
                function(res) {
                    $scope.tutkinnonOsat.unshift(res);
                    TutkinnonOsaEditMode.setMode(true);

                    $state.go(
                        "root.perusteprojekti.suoritustapa." + ($scope.isVaTe ? "koulutuksenosa" : "tutkinnonosa"),
                        {
                            tutkinnonOsaViiteId: res.id,
                            versio: ""
                        }
                    );
                },
                function(err) {
                    Notifikaatiot.serverCb(err);
                }
            );
        };

        $scope.getHref = function(valittu) {
            return $state.href(
                "root.perusteprojekti.suoritustapa." + ($scope.isVaTe ? "koulutuksenosa" : "tutkinnonosa"),
                { tutkinnonOsaViiteId: valittu.id, versio: "" }
            );
        };
    });

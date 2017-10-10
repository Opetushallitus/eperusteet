/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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
    .controller("TpoSisaltoController", function(
        $scope,
        $state,
        $stateParams,
        Api,
        perusteprojektiTiedot,
        Algoritmit,
        SuoritustavanSisalto,
        TekstikappaleOperations,
        SuoritustapaSisalto,
        TutkinnonOsaEditMode,
        Notifikaatiot,
        Editointikontrollit,
        YleinenData
    ) {
        $scope.projekti = perusteprojektiTiedot.getProjekti();
        $scope.peruste = perusteprojektiTiedot.getPeruste();
        TekstikappaleOperations.setPeruste($scope.peruste);
        $scope.rajaus = "";
        $scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();
        $scope.$esitysurl = $state.href(
            "root.selaus." + (YleinenData.isEsiopetus($scope.peruste) ? "esiopetus" : "lisaopetus"),
            {
                perusteId: $scope.peruste.id,
                suoritustapa: $stateParams.suoritustapa
            }
        );

        $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();

        $scope.$watch(
            "peruste.sisalto",
            function() {
                Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                    const sisaltotyyppi = (lapsi && lapsi.perusteenOsa && lapsi.perusteenOsa.osanTyyppi) || "tekstikappale";
                    lapsi.$url = $state.href("root.perusteprojekti.suoritustapa." + sisaltotyyppi, {
                        suoritustapa: $stateParams.suoritustapa,
                        perusteenOsaViiteId: lapsi.id,
                        versio: ""
                    });
                });
            },
            true
        );

        $scope.rajaaSisaltoa = function(value) {
            if (_.isUndefined(value)) {
                return;
            }
            var sisaltoFilterer = function(osa, lapsellaOn) {
                osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, "perusteenOsa", "nimi");
                return osa.$filtered;
            };
            Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, sisaltoFilterer);
        };

        $scope.avaaSuljeKaikki = function(value) {
            var open = false;
            Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                open = open || lapsi.$opened;
            });
            Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                lapsi.$opened = _.isUndefined(value) ? !open : value;
            });
        };

        $scope.addSisalto = (tyyppi: "tekstikappale" | "taiteenala") => {
            SuoritustapaSisalto.save(
                {
                    perusteId: $scope.projekti._peruste,
                    suoritustapa: $stateParams.suoritustapa,
                },
                {
                    perusteenOsa: {
                        osanTyyppi: "taiteenala",
                    }
                },
                function(response) {
                    TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                    $state.go(
                        "root.perusteprojekti.suoritustapa." + tyyppi, {
                            perusteenOsaViiteId: response.id,
                            versio: "",
                        }, {
                            reload: true
                        });
                },
                Notifikaatiot.serverCb
            );
        };

        $scope.edit = function() {
            Editointikontrollit.startEditing();
        };

        Editointikontrollit.registerCallback({
            edit: function() {
                $scope.rajaus = "";
                $scope.avaaSuljeKaikki(true);
            },
            save: function() {
                TekstikappaleOperations.updateViitteet($scope.peruste.sisalto, function() {
                    Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
                });
            },
            cancel: function() {
                $state.go($state.current.name, $stateParams, {
                    reload: true
                });
            },
            validate: function() {
                return true;
            },
            notify: function(value) {
                $scope.editing = value;
            }
        });
    });

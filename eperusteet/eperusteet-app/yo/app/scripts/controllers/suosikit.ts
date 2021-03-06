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
    .controller("SuosikitCtrl", function($scope, Profiili, $uibModal) {
        $scope.suosikit = {};
        $scope.naytto = { limit: 5, shown: 5 };

        var paivitaSuosikit = function() {
            $scope.suosikit = Profiili.listaaSuosikit().reverse();
        };

        paivitaSuosikit();

        $scope.$on("kayttajaProfiiliPaivittyi", paivitaSuosikit);

        $scope.edit = function() {
            $uibModal.open({
                template: require("views/modals/suosikkienMuokkaus.html"),
                controller: "SuosikkienMuokkausController",
                size: "lg"
            });
        };
    })
    .controller("SuosikkienMuokkausController", function($scope, Profiili, Varmistusdialogi) {
        $scope.search = {
            term: "",
            update: function() {
                if (_.isEmpty($scope.search.term)) {
                    $scope.suosikit = $scope.originals;
                } else {
                    $scope.suosikit = _.filter($scope.originals, function(item: any) {
                        return item.nimi.toLowerCase().indexOf($scope.search.term.toLowerCase()) > -1;
                    });
                }
            }
        };

        function refresh() {
            $scope.suosikit = Profiili.listaaSuosikit().reverse();
            $scope.originals = angular.copy($scope.suosikit);
        }
        refresh();

        $scope.$on("kayttajaProfiiliPaivittyi", refresh);

        $scope.edit = function(suosikki) {
            $scope.editing = angular.copy(suosikki);
        };
        $scope.save = function(suosikki) {
            var found = _.findIndex($scope.suosikit, { id: suosikki.id });
            if (found > -1) {
                $scope.suosikit[found] = $scope.editing;
                Profiili.paivitaSuosikki($scope.editing);
            }
            $scope.editing = null;
        };
        $scope.cancel = function() {
            $scope.editing = null;
        };
        $scope.remove = function(suosikki) {
            Varmistusdialogi.dialogi({
                otsikko: "varmista-poisto",
                teksti: "varmista-poisto-suosikki-teksti",
                primaryBtn: "poista",
                successCb: function() {
                    Profiili.poistaSuosikki(suosikki);
                }
            })();
        };
    });

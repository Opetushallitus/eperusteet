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
    .service("Tyoryhmat", function($uibModal) {
        return {
            valitse: function(valittavat, valitut, successCb) {
                $uibModal
                    .open({
                        template: require("views/modals/tyoryhmavalitsin.html"),
                        controller: "valitseTyoryhmatModalCtrl",
                        resolve: {
                            valittavat: function() {
                                return valittavat;
                            },
                            valitut: function() {
                                return valitut;
                            }
                        }
                    })
                    .result.then(successCb);
            }
        };
    })
    .controller("valitseTyoryhmatModalCtrl", function($scope, $uibModalInstance, valittavat, valitut) {
        $scope.valitut = valitut;
        $scope.valittavat = _.difference(valittavat, valitut);
        $scope.uudet = {};

        $scope.valitse = function(tyoryhma) {
            $scope.uudet[tyoryhma] = !$scope.uudet[tyoryhma];
        };

        $scope.ok = function() {
            $uibModalInstance.close(
                _.filter(_.keys($scope.uudet), function(k) {
                    return $scope.uudet[k];
                })
            );
        };
        $scope.peruuta = $uibModalInstance.dismiss;
    });

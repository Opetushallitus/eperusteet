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
    .controller("RakenneosaModalCtrl", function($scope, $uibModalInstance, rakenneosa, Koodisto) {
        var setupRyhma = function(rakenneosa) {
            $scope.rakenneosa = _.cloneDeep(rakenneosa);
            if (!$scope.rakenneosa.kuvaus || !_.isObject($scope.rakenneosa.kuvaus)) {
                $scope.rakenneosa.kuvaus = {};
            }
        };
        setupRyhma(rakenneosa);

        $scope.vieraskoodiModaali = Koodisto.modaali(
            function(koodi) {
                $scope.rakenneosa.vieras = _.pick(koodi, "nimi", "koodiArvo", "koodiUri");
                $scope.rakenneosa.vieras = {
                    nimi: koodi.nimi,
                    uri: koodi.koodiUri,
                    arvo: koodi.koodiArvo
                };
            },
            {
                tyyppi: function() {
                    return "tutkinnonosat";
                },
                ylarelaatioTyyppi: function() {
                    return "";
                }
            },
            angular.noop,
            null
        );

        $scope.ok = function() {
            $uibModalInstance.close($scope.rakenneosa);
        };

        $scope.peruuta = $uibModalInstance.dismiss;
    });

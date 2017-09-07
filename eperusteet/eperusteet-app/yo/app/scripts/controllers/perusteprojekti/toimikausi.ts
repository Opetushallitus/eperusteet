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

angular.module("eperusteApp").controller("PerusteprojektiToimikausiCtrl", function($scope, YleinenData) {
    if (typeof $scope.projekti.toimikausiAlku === "number") {
        $scope.projekti.toimikausiAlku = new Date($scope.projekti.toimikausiAlku);
    }
    if (typeof $scope.projekti.toimikausiLoppu === "number") {
        $scope.projekti.toimikausiLoppu = new Date($scope.projekti.toimikausiLoppu);
    }

    $scope.kalenteriTilat = {
        toimikausiAlkuButton: false,
        toimikausiLoppuButton: false
    };

    $scope.open = function($event) {
        $event.preventDefault();
        $event.stopPropagation();

        for (var key in $scope.kalenteriTilat) {
            if ($scope.kalenteriTilat.hasOwnProperty(key) && key !== $event.target.id) {
                $scope.kalenteriTilat[key] = false;
            }
        }
        $scope.kalenteriTilat[$event.target.id] = !$scope.kalenteriTilat[$event.target.id];
    };

    $scope.dateOptions = YleinenData.dateOptions;
    $scope.format = YleinenData.dateFormatDatepicker;
});

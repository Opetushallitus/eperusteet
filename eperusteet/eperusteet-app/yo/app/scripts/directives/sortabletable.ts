/*
 * Copyright (c) 2017 The Finnish Board of Education - Opetushallitus
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

angular.module("eperusteApp")
.directive("sortableTable", () => {
    return {
        templateUrl: "scripts/directives/sortabletable.html",
        scope: {
            ngModel: "=",
            showIdx: "<",
            ngChange: "<", // Sallii muokkauksen jos määritelty
            isSorting: "=",
            nimeton: "<"
        },
        controller($scope, $state, Editointikontrollit, Notifikaatiot, Api) {
            $scope.ngModel = $scope.ngModel || [];
            $scope.showIdx = $scope.showIdx || true;
            $scope.isSorting = false;
            $scope.allowSorting = _.isFunction($scope.ngChange);
            $scope.hasMuokattu = !_.isEmpty($scope.ngModel) && !!_.first($scope.ngModel).muokattu;

            $scope.sortableOptions = {
                cursor: "move",
                cursorAt: { top : 2, left: 2 },
                handle: ".handle",
                delay: 100,
                tolerance: "pointer",
            };

            // let backup = Api.copy($scope.ngModel);

            $scope.sort = () => {
                if (!$scope.allowSorting) {
                    return;
                }

                Editointikontrollit.registerCallback({
                    async edit() {
                        // let backup = Api.copy($scope.ngModel);
                        $scope.isSorting = true;
                    },
                    async save() {
                        $scope.isSorting = false;
                        await $scope.ngChange($scope.ngModel);
                        Notifikaatiot.onnistui("tallennus-onnistui");
                    },
                    cancel() {
                        $scope.isSorting = false;
                        // $scope.ngModel = Api.copy(backup);
                    }
                });
                Editointikontrollit.startEditing();
            };

        }
    }
});

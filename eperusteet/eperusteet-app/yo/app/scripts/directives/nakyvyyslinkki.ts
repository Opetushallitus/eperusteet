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

export const nakyvyyslinkkiService = () => {
    let nakyvyys = false;

    return {
        setNakyvyys(value: boolean) {
            nakyvyys = value;
        },

        getNakyvyys() {
            return nakyvyys;
        }
    };
};

export const nakyvyyslinkki = ($window, $rootScope, nakyvyyslinkkiService) => {
    return {
        template:
        `<button
            ng-show="projektissa"
            style="margin: 0px"
            class="btn-link"
            ng-cloak
            ng-click="vaihdaTila(!$naytaKaannosvirheet)"
            icon-role="flag">
            <span ng-show="$naytaKaannosvirheet" kaanna="'piilota-kaannosvirheet'"></span>
            <span ng-hide="$naytaKaannosvirheet" kaanna="'nayta-kaannosvirheet'"></span>
        </button>`,
        restrict: "E",
        controller($scope, $state) {
            $rootScope.$naytaKaannosvirheet = false;
            $scope.projektissa = false;

            $scope.$on("$stateChangeSuccess", () => {
                $scope.projektissa = $state.includes("root.perusteprojekti");
            });

            $scope.vaihdaTila = (tila) => {
                $scope.$naytaKaannosvirheet = tila;
                nakyvyyslinkkiService.setNakyvyys(tila);
                $rootScope.$broadcast("naytaKaannosvirheet", tila);
            };
        }
    };
};

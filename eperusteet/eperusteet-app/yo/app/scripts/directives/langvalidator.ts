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

import * as _ from "lodash";
import * as angular from "angular";

export const LangValidator = (Kaanna, $compile, PerusteprojektiTiedotService, nakyvyyslinkkiService) => {
    return {
        scope: {
            kentta: "="
        },
        restrict: "E",
        template: `
        <span ng-if="nayta" class="puutteelliset-kielisisallot">
            <span ng-repeat="virhe in virheet">
                <span class="puutteellinen-kielisisalto" ng-bind="virhe"></span>
            </span>
        </span>
        `,
        controller($scope) {
            $scope.virheet = [];
            $scope.nayta = nakyvyyslinkkiService.getNakyvyys();

            async function updateKaannosvirheet() {
                if (!$scope.nayta) {
                    return;
                }

                const peruste = (await PerusteprojektiTiedotService).getPeruste();
                if (_.isObject($scope.kentta) && _.size($scope.kentta) > 0 && peruste && _.isArray(peruste.kielet)) {
                    $scope.virheet = _.filter(peruste.kielet, (kieli: string) => {
                        if (_.isArray($scope.kentta)) {
                            return _.some($scope.kentta, field => field && !field[kieli]);
                        }
                        else {
                            return !$scope.kentta[kieli];
                        }
                    });
                }
                else {
                    $scope.virheet = [];
                }
            }

            $scope.$watch("kentta", updateKaannosvirheet);
            $scope.$on("naytaKaannosvirheet", (ev, val) => {
                $scope.nayta = val;
                if (val) {
                    updateKaannosvirheet();
                }
            });
        }
    };
}

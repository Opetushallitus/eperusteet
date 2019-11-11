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

angular.module("eperusteApp").directive("mlInput", function($translate, YleinenData, $timeout) {
    return {
        restrict: "E",
        scope: {
            mlData: "=",
            mlAdditionalLanguages: "=",
            type: "@",
            required: "="
        },
        template: require("views/multiinput.html"),
        replace: true,
        transclude: true,
        controller: function($scope) {
            $scope.type = $scope.type || "text";
            $scope.langs = _(_.values(YleinenData.kielet))
                .union($scope.mlAdditionalLanguages || [])
                .sort()
                .value();
            if ($scope.mlData) {
                _.forEach($scope.langs, function(lang) {
                    $scope.mlData[lang] = $scope.mlData[lang] || "";
                });
            }

            $scope.activeLang = $translate.use() || $translate.preferredLanguage();
            $scope.kielivalintaAuki = false;

            $scope.vaihdaKieli = function(uusiKieli) {
                $scope.kielivalintaAuki = false;
                $scope.activeLang = uusiKieli;
            };
        },
        link: function(scope: any, element: any) {
            if (!scope.mlData) {
                console.log("You must set ml-data for ml-input.");
            } else if (!_.isObject(scope.mlData)) {
                console.log("ml-data must be an object");
            }
            if (scope.$parent.inputElId) {
                $timeout(function() {
                    element.find("input, textarea").attr("id", scope.$parent.inputElId);
                });
            }
        }
    };
});

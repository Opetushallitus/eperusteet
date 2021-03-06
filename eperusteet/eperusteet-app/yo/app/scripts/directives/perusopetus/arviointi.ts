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
    .directive("perusopetuksenArviointi", function() {
        return {
            template: require("views/directives/perusopetus/arviointi.html"),
            restrict: "A",
            scope: {
                model: "=perusopetuksenArviointi",
                editMode: "=",
                atavoite: "=atavoite"
            },
            controller: "PerusopetuksenArviointiController"
        };
    })
    .controller("PerusopetuksenArviointiController", function($scope, Kaanna, Kieli) {

        $scope.getArvosanat = () => {
            return Kieli.getUiKieli() === 'sv' ? [0,5,6,7,8,9,10] : [5,6,7,8,9,10];
        };

        $scope.arvosanat = $scope.getArvosanat().map(numero => {
            return {
                numero,
                teksti: Kaanna.kaanna("osaamisen-kuvaus-arvosanalle_"+numero)
            }
        });

        $scope.arvosanaNumerolla = (numero) => {
            return (<any[]>$scope.arvosanat).find(arvosana => arvosana.numero == numero);
        }

        $scope.addKohde = function() {
            $scope.model.push({});
        };
        $scope.removeKohde = function(kohde) {
            $scope.model.splice(kohde, 1);
        };
    });

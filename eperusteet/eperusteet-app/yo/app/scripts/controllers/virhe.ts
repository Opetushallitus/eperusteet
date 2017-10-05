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

angular
    .module("eperusteApp")
    .config(function($stateProvider) {
        $stateProvider.state("root.virhe", {
            url: "/virhe",
            template: require("views/virhe.html"),
            controller: "virheCtrl"
        });
    })
    .controller("virheCtrl", function($scope, virheService) {
        $scope.$watch(virheService.getData, function(value) {
            $scope.data = value;
        });
    })
    .service("virheService", function($state) {
        var data = {};

        return {
            setData(d) {
                data = d;
            },

            getData() {
                return data;
            },

            virhe(virhe) {
                if (_.isObject(virhe)) {
                    data = virhe;
                } else {
                    data = { muu: virhe };
                }
                $state.go("root.virhe");
            }
        };
    });

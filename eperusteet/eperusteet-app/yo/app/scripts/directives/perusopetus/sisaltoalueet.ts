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
    .directive("osanmuokkausSisaltoalueet", function() {
        return {
            template: require("views/directives/perusopetus/osanmuokkaussisaltoalueet.html"),
            restrict: "E",
            scope: {
                model: "=",
                config: "="
            },
            controller: function($scope, YleinenData, $rootScope, Utils, CloneHelper, OsanMuokkausHelper) {
                $scope.editables = $scope.model;
                $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
                $scope.isEditing = false;

                function getOppiaineenVuosiluokkakokonaisuus() {
                    var ovlk = OsanMuokkausHelper.getOppiaineenVuosiluokkakokonaisuus();
                    ovlk.sisaltoalueinfo = ovlk.sisaltoalueinfo || {
                        otsikko: {},
                        teksti: {}
                    };
                    $scope.ovlk = ovlk;
                }
                getOppiaineenVuosiluokkakokonaisuus();

                var cloner = CloneHelper.init(["nimi", "kuvaus"]);

                $scope.edit = function(alue) {
                    alue.$editing = true;
                    $scope.isEditing = true;
                    cloner.clone(alue);
                };
                $scope.remove = function(alue) {
                    var index = _.findIndex($scope.editables, function(item) {
                        return item === alue;
                    });
                    if (index > -1) {
                        $scope.editables.splice(index, 1);
                    }
                };
                $scope.cancel = function(alue) {
                    alue.$editing = false;
                    $scope.isEditing = false;
                    if (alue.$new) {
                        $scope.remove(alue);
                    } else {
                        cloner.restore(alue);
                    }
                };
                $scope.ok = function(alue) {
                    $rootScope.$broadcast("notifyCKEditor");
                    if (!$scope.hasTitle(alue)) {
                        return;
                    }
                    alue.$editing = false;
                    $scope.isEditing = false;
                };
                $scope.add = function() {
                    $scope.isEditing = true;
                    $scope.editables.push({
                        $editing: true,
                        $new: true,
                        nimi: {},
                        kuvaus: {}
                    });
                };
                $scope.hasTitle = function(alue) {
                    return Utils.hasLocalizedText(alue.nimi);
                };
            }
        };
    })
    .directive("kohdealueet", function() {
        return {
            template: require("views/directives/perusopetus/sisaltoalueet.html"),
            restrict: "A",
            scope: {
                model: "=kohdealueet"
            },
            controller: function() {}
        };
    })
    .directive("sisaltoalueet", function() {
        return {
            template: require("views/directives/perusopetus/sisaltoalueet.html"),
            restrict: "A",
            scope: {
                model: "=sisaltoalueet"
            },
            controller: "SisaltoalueetController"
        };
    })
    .controller("SisaltoalueetController", function($scope, YleinenData) {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    });

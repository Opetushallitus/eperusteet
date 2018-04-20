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
    .directive("eperusteNavi", function() {
        return {
            template: require("views/partials/navi.html"),
            restrict: "AE",
            controller: "NaviCtrl"
        };
    })
    .controller("NaviCtrl", function(
        $scope,
        Navigaatiopolku,
        Kaanna,
        $state,
        PerusteProjektiService,
        $location,
        $stateParams
    ) {
        var PREFIX = "root.perusteprojekti.";
        var SUBSTATES = {
            termisto: "perusteen-termisto",
            tiedot: "projektin-tiedot",
            peruste: "projektin-tiedot",
            projektiryhma: "projekti-projektiryhma"
        };
        $scope.navigaatiopolku = [];
        var inProject = null;

        function setNavi(state, params?) {
            $scope.navigaatiopolku = [];
            inProject = null;
            var index = state.name.indexOf(PREFIX);
            if (index === 0) {
                inProject = Navigaatiopolku.getProject();
                if (!inProject) {
                    return;
                }
                var url = PerusteProjektiService.getUrl(inProject, Navigaatiopolku.getPeruste());
                $scope.navigaatiopolku.push({
                    url: url.replace(/#/g, "") === $location.path() ? "" : url,
                    label: inProject.nimi
                });
                var substate = SUBSTATES[state.name.substring(PREFIX.length)];
                if (substate) {
                    $scope.navigaatiopolku.push({
                        label: substate
                    });
                }
            }
        }

        function updateTitle() {
            var isEtusivu = $state.is("root.aloitussivu");
            var prefix = Kaanna.kaanna("ePerusteet") + ": ";
            var title = "";
            if (inProject) {
                title = Kaanna.kaanna(inProject.nimi);
            } else if (isEtusivu) {
                title = Kaanna.kaanna("Etusivu");
            }
            if ($scope.navigaatiopolku.length > 1) {
                title += !title ? "" : ": " + Kaanna.kaanna((_.last($scope.navigaatiopolku) as any).label);
            }
            if (!title) {
                title = Kaanna.kaanna(_.last($state.current.name.split(".")));
            }
            angular.element("head > title").html(prefix + title);
        }

        function refresh(event, toState, toParams) {
            setNavi(toState || $state.current, toParams || $stateParams);
            updateTitle();
        }

        $scope.$on("$stateChangeSuccess", refresh);
        $scope.$on("update:navipolku", refresh);
    })
    .service("Navigaatiopolku", function($rootScope) {
        var projekti = null;
        var peruste = null;

        this.setProject = function(project, projectperuste) {
            projekti = project;
            peruste = projectperuste;
            $rootScope.$broadcast("update:navipolku");
        };
        this.getProject = function() {
            return projekti;
        };
        this.getPeruste = function() {
            return peruste;
        };
    });

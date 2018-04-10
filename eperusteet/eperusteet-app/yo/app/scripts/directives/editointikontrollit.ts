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

angular
    .module("eperusteApp")
    .directive("editointikontrollit", function($window) {
        return {
            template: require("views/partials/editointikontrollit.html"),
            restrict: "E",
            link: function(scope: any) {
                var window = angular.element($window),
                    container = angular.element(".edit-controls"),
                    wrapper = angular.element(".editointi-wrapper");

                /**
                 * Editointipalkki asettuu staattisesti footerin päälle kun skrollataan
                 * tarpeeksi alas. Ylempänä editointipalkki kelluu.
                 */
                scope.updatePosition = function() {
                    if (window.scrollTop() + window.innerHeight() < wrapper.offset().top + container.height()) {
                        container.addClass("floating");
                        container.removeClass("static");
                        container.css("width", wrapper.width());
                    } else {
                        container.removeClass("floating");
                        container.addClass("static");
                        container.css("width", "100%");
                    }
                };
                var updatepos = function() {
                    scope.updatePosition();
                };
                window.on("scroll resize", updatepos);
                scope.$on("$destroy", function() {
                    window.off("scroll resize", updatepos);
                });
                scope.updatePosition();

                scope.setMargins = function() {
                    if (scope.editStarted) {
                        wrapper.css("margin-bottom", "50px").css("margin-top", "20px");
                    } else {
                        wrapper.css("margin-bottom", 0).css("margin-top", 0);
                    }
                };
                scope.setMargins();
            }
        };
    })
    .controller("EditointiCtrl", function($scope, $rootScope, Editointikontrollit) {
        $scope.kommentti = "";
        $scope.hideControls = true;

        function setEditControls() {
            if (Editointikontrollit.editingEnabled()) {
                $scope.hideControls = false;
            } else {
                $scope.hideControls = true;
                $scope.editStarted = false;
            }
        }

        $scope.$on("$stateChangeStart", function() {
            Editointikontrollit.unregisterCallback();
            setEditControls();
        });

        Editointikontrollit.registerCallbackListener(setEditControls);

        $scope.$on("editointikontrollitRefresh", function() {
            $scope.updatePosition();
        });

        $scope.$on("enableEditing", () => {
            $scope.$evalAsync(() => {
                $scope.editStarted = true;
                $scope.setMargins();
                $scope.kommentti = "";
                $scope.updatePosition();
            });
        });

        $scope.$on("disableEditing", function() {
            $scope.editStarted = false;
            $scope.setMargins();
        });

        $scope.start = function() {
            Editointikontrollit.startEditing();
        };
        $scope.save = function() {
            Editointikontrollit.saveEditing($scope.kommentti);
        };
        $scope.cancel = function() {
            Editointikontrollit.cancelEditing();
        };
    });

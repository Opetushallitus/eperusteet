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
"use strict";
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .directive("pikamenu", function($document, $window) {
        return {
            restrict: "EA",
            transclude: true,
            templateUrl: "views/partials/perusteprojekti/pikamenu.html",
            link: function(scope, element) {
                // Pop the button next to the header after transclusion
                var header = element.find("#pikamenu-header");
                var button = element.find("#tutkinnonosat-pikamenu-button");
                button.detach().appendTo(header);

                // Clicking outside of menu closes it
                $document.on("click", function(event) {
                    if (element.find(event.target).length > 0) {
                        return;
                    }
                    scope.pikamenu.opened = false;
                    scope.$apply();
                });
                scope.$on("$destroy", function() {
                    $document.off("click");
                });

                function updatePosition() {
                    var header = element.find("#pikamenu-header");
                    var button = element.find("#tutkinnonosat-pikamenu-button");
                    var menu = element.find("#tutkinnonosat-pikamenu").filter(":visible");
                    if (menu.length === 0) {
                        return;
                    }
                    menu.css("top", 0);
                    var buttonOffset = button.offset();
                    var relButtonOffset = buttonOffset.left - header.offset().left;
                    var menuWidth = menu.width();
                    menu.css("left", relButtonOffset + button.width() - menuWidth + 1);
                    if (menu.offset().left < header.offset().left) {
                        menu.css("left", 0);
                    }
                    menu.css("top", buttonOffset.top - menu.offset().top + button.height() + 2);
                }

                // update position on 1. header text change 2. when opened 3. resize viewport
                scope.$watch(function() {
                    return element.find("#pikamenu-header").text();
                }, updatePosition);
                scope.$watch("pikamenu.opened", function(value) {
                    if (value) {
                        updatePosition();
                    }
                });
                angular.element($window).on("resize", updatePosition);
            },
            controller: "TutkinnonOsatPikamenu"
        };
    })
    .controller("TutkinnonOsatPikamenu", function($scope, Kaanna) {
        $scope.pikamenu = {
            opened: false,
            orderFn: function(key) {
                return Kaanna.kaanna($scope.rakenne.tutkinnonOsat[key].nimi).toLowerCase();
            }
        };
        $scope.keys = _.keys;
    });

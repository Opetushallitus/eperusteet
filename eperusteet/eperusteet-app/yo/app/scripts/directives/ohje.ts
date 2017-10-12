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

/**
 * Ohje
 * Ikoni tooltipille ja siihen liittyvä kelluva ohjeteksti.
 * Tukee sekä hiirtä (mouse enter) että touch/click-eventtejä.
 *
 * Kaksi eri toimintamoodia:
 * <ohje ...></ohje>
 *   luo kysymysmerkki-badgen, toimii mouseoverilla (ja klikillä)
 * <span ohje="false" ...><div>omaa sisältöä</div></span>
 *   kiinnittää popoverin olemassaolevaan sisältöön, avautuu klikillä
 *
 * suunta: left|right(default)|top|bottom
 * otsikko: optional
 */
angular.module("eperusteApp").directive("ohje", function($timeout, $compile, $document) {
    return {
        template: require("views/partials/ohje.html"),
        restrict: "EA",
        transclude: true,
        scope: {
            teksti: "@",
            otsikko: "@?",
            suunta: "@?",
            ohje: "@?",
            extra: "="
        },
        link: function(scope: any, element: any, attrs: any) {
            scope.showing = false;
            var DELAY = 500;
            var timer = null;
            var clickAnywhere = attrs.ohjeClickAnywhere !== "false";
            function appendExtraContent() {
                var content = $compile(scope.extra)(scope);
                element
                    .find(".popover-extra")
                    .empty()
                    .append(content);
            }

            var el = element.find(".popover-element");

            scope.mouseleave = function() {
                $timeout.cancel(timer);
            };

            scope.show = function(visible, mouseEnter) {
                var popupDelay = mouseEnter ? DELAY : 0;
                if (mouseEnter && scope.ohje === "false") {
                    return;
                }
                var opening = angular.isUndefined(visible) || visible;
                if (!scope.showing && !opening) {
                    return;
                }
                timer = $timeout(function() {
                    el.trigger(opening ? "show" : "hide");
                    scope.showing = opening;
                    if (opening) {
                        var title = element.find(".popover-title");
                        var closer = $compile(
                            angular.element('<span class="closer pull-right" ng-click="show(false)">&#x2715;</span>')
                        )(scope);
                        title.append(closer);
                        appendExtraContent();
                    }
                }, popupDelay);
            };

            var clickHandler = function(event) {
                if (element.find(event.target).length > 0) {
                    return;
                }
                scope.show(false);
                scope.$apply();
            };

            if (clickAnywhere) {
                // Click anywhere to close
                $document.on("click", clickHandler);
                scope.$on("$destroy", function() {
                    $document.off("click", clickHandler);
                });
            }

            scope.$on("ohje:closeAll", function() {
                scope.show(false);
            });

            scope.$watch("teksti", function() {
                scope.textObject = scope.$parent.$eval(scope.teksti) || scope.teksti;
            });
            scope.$watch("otsikko", function() {
                scope.title = scope.$parent.$eval(scope.otsikko) || scope.otsikko;
            });
        }
    };
});

// Modify popover template for binding unsafe html
angular.module("uib/template/popover/popover.html", []).run($templateCache => {
    $templateCache.put(
        "uib/template/popover/popover.html",
        '<div class="arrow"></div>\n' +
            '<div class="popover-inner">\n' +
            '  <h3 class="popover-title" ng-bind-html="uibTitle | unsafe" ng-if="uibTitle"></h3>\n' +
            '  <div class="popover-content" ng-bind-html="content | unsafe"></div>\n' +
            "</div>\n"
    );
});

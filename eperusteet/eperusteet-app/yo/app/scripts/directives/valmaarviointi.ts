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

angular
    .module("eperusteApp")
    .controller("valmaarviointiCtrl", function($scope, YleinenData, $timeout, Utils, Varmistusdialogi) {
        $scope.showNewKohdealueInput = false;
        $scope.isArviointi =
            $scope.type === "arviointi" || ($scope.$parent.field && $scope.$parent.field.valmatype === "arviointi");
        $scope.$$valmaOpen = true;

        $scope.valmaarviointi =
            !$scope.isArviointi && !_.isArray($scope.valmaarviointi) ? [$scope.valmaarviointi] : $scope.valmaarviointi;

        $scope.addNewGroup = function() {
            $scope.valmaarviointi.push({});
        };

        $scope.removeItem = function(valmaitem) {
            _.remove($scope.valmaarviointi, valmaitem);
        };

        $scope.rivi = {
            poista: function(list, index) {
                list.splice(index, 1);
            },
            uusi: function(kriteeri, event) {
                if (_.isEmpty(kriteeri.tavoitteet)) {
                    kriteeri.tavoitteet = [];
                }
                kriteeri.tavoitteet.push({ jarjestys: kriteeri.tavoitteet.length });

                // Set focus to newly added field
                var parent = angular.element(event.currentTarget).closest("table");
                $timeout(function() {
                    var found = parent.find(".form-control");
                    if (found.length > 0) {
                        found[found.length - 1].focus();
                    }
                }, 100);
            }
        };

        if ($scope.eiKohdealueita && (angular.isUndefined($scope.valmaarviointi) || $scope.valmaarviointi === null)) {
            $scope.uudenKohdealueenNimi = {
                fi: "Nimetön"
            };
            $scope.kohdealue.uusi();
        }
    })
    .directive("valmaarviointi", function(YleinenData, $timeout) {
        return {
            templateUrl: "views/partials/valmaarviointi.html",
            restrict: "E",
            scope: {
                valmaarviointi: "=",
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                eiKohdealueita: "@",
                isOsaamistavoite: "@",
                type: "@"
            },

            controller: "valmaarviointiCtrl",
            link: function(scope: any) {
                scope.eiKohdealueita = scope.eiKohdealueita === "true" || scope.eiKohdealueita === true;
                scope.editAllowed = scope.editAllowed === "true" || scope.editAllowed === true;

                scope.tavoitteet = [
                    {
                        otsikko: ""
                    }
                ];

                scope.$on("valmaarviointiasteikot", function() {
                    scope.valmaarviointiasteikot = YleinenData.valmaarviointiasteikot;
                });

                scope.elementDragged = false;

                scope.sortableOptions = {
                    axis: "y",
                    start: function() {
                        scope.elementDragged = true;
                    },
                    stop: function() {
                        // ei toimi
                    }
                };

                scope.kriteeriSortableOptions = {
                    axis: "y",
                    cancel: ".row-adder",
                    handle: ".drag-enable",
                    items: "tr:not(.row-adder)"
                };

                scope.$watch("editEnabled", function(value) {
                    scope.sortableOptions.disabled = !value;
                    scope.kriteeriSortableOptions.disabled = !value;
                    if (!value) {
                        scope.editableKohde = null;
                    }
                    $timeout(function() {
                        setAccordion(true);
                    });
                });

                scope.isElementDragged = function() {
                    if (scope.elementDragged) {
                        scope.elementDragged = false;
                        return true;
                    } else {
                        return false;
                    }
                };

                /**
         * is-open attribuutti on annettava modelina accordionille, jotta
         * ui-sortable voidaan disabloida lukutilassa.
         */
                function setAccordion(mode) {
                    var obj = scope.valmaarviointi;
                    //_.each(obj, function (kohdealue) {
                    //  kohdealue.$$accordionOpen = mode;
                    //  _.each(kohdealue.vaatimuksenKohteet, function (kohde) {
                    //    kohde.$$accordionOpen = mode;
                    //  });
                    //});
                }

                function accordionState() {
                    var obj = _.first(scope.valmaarviointi);
                    return obj && obj.$$accordionOpen;
                }

                scope.toggleAll = function() {
                    setAccordion(!accordionState());
                };

                setAccordion(true);
            }
        };
    })
    .directive("onEnter", function() {
        return function(scope, element, attrs) {
            element.bind("keydown keypress", function(event) {
                if (event.which === 13) {
                    scope.$apply(function() {
                        scope.$eval(attrs.onEnter);
                    });

                    event.preventDefault();
                }
            });
        };
    })
    .directive("onEsc", function() {
        return function(scope, element, attrs) {
            element.bind("keydown keypress", function(event) {
                if (event.which === 27) {
                    scope.$apply(function() {
                        scope.$eval(attrs.onEsc);
                    });

                    event.preventDefault();
                }
            });
        };
    })
    .directive("focusMe", function($timeout) {
        return function(scope, element, attrs) {
            scope.$watch(attrs.focusMe, function(value) {
                if (value === true) {
                    $timeout(function() {
                        element[0].focus();
                    }, 100);
                }
            });
        };
    });

// Kustomoitu accordion group, lisätty isElementDragged-tarkastelu
// jotta ui-sortable toimii accordionin kanssa.
angular.module("template/accordion/accordion-group.html", []).run([
    "$templateCache",
    function($templateCache) {
        $templateCache.put(
            "template/accordion/accordion-group.html",
            '<div class="panel panel-default">\n' +
                '  <div class="panel-heading">\n' +
                '    <h4 class="panel-title">\n' +
                '      <a class="accordion-toggle" ng-click="$parent.isElementDragged() || toggleOpen()" accordion-transclude="heading"><span ng-class="{\'text-muted\': isDisabled}">{{heading}}</span></a>\n' +
                "    </h4>\n" +
                "  </div>\n" +
                '  <div class="panel-collapse" uib-collapse="!isOpen">\n' +
                '	  <div class="panel-body" ng-transclude></div>\n' +
                "  </div>\n" +
                "</div>"
        );
    }
]);

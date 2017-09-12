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
    .controller("ammattitaitoCtrl", function($scope, YleinenData, $timeout, Utils, Varmistusdialogi) {
        $scope.showNewKohdealueInput = false;

        $scope.kohdealue = {
            uusi: function() {
                if (!$scope.uudenKohdealueenNimi) {
                    return;
                }

                if (angular.isUndefined($scope.ammattitaito) || $scope.ammattitaito === null) {
                    $scope.ammattitaito = [];
                }

                const kohdealue = {
                    otsikko: {},
                    $accordionOpen: true
                };
                kohdealue.otsikko = $scope.uudenKohdealueenNimi;

                $scope.ammattitaito.push(kohdealue);

                $scope.uudenKohdealueenNimi = null;
                $scope.showNewKohdealueInput = false;
            },
            cancel: function() {
                $scope.showNewKohdealueInput = false;
                $scope.uudenKohdealueenNimi = null;
            }
        };

        $scope.kohde = {
            muokkaa: function(kohde, event) {
                if (event) {
                    event.stopPropagation();
                }
                $scope.originalKohde = kohde;
                kohde.$editointi = true;
                $scope.editableKohde = angular.copy(kohde);

                if (kohde.vaatimukset === 0) {
                    $scope.editableKohde.vaatimukset = [{}];
                }
            },
            poista: function(list, item, event) {
                if (event) {
                    event.stopPropagation();
                }
                Varmistusdialogi.dialogi({
                    otsikko: "varmista-osion-poisto-otsikko",
                    teksti: "varmista-osion-poisto-teksti",
                    primaryBtn: "poista",
                    successCb: function() {
                        _.remove(list, item);
                    }
                })();
            },
            uusiWizard: function(kohdealue) {
                kohdealue.$newkohde = {
                    showInputArea: true
                };
            },
            isAdding: function(kohdealue) {
                return !_.isEmpty(kohdealue.$newkohde);
            },
            uusi: function(kohdealue) {
                if (angular.isUndefined(kohdealue.vaatimuksenKohteet) || kohdealue.vaatimuksenKohteet === null) {
                    kohdealue.vaatimuksenKohteet = [];
                }

                const kohde = {
                    otsikko: {},
                    vaatimukset: [{}],
                    $accordionOpen: true
                };

                kohde.otsikko[YleinenData.kieli] = kohdealue.$newkohde.nimi;
                kohdealue.vaatimuksenKohteet.push(kohde);
                kohdealue.$newkohde = {};
                $timeout(function() {
                    $scope.kohde.muokkaa(kohde);
                });
            },
            cancel: function(kohdealue) {
                kohdealue.$newkohde = null;
            },
            poistuMuokkauksesta: function(list, index) {
                $scope.editableKohde.$editointi = false;

                _.reduce(
                    $scope.editableKohde.vaatimukset,
                    (acc, vaatimus) => {
                        vaatimus.jarjestys = acc;
                        return acc + 1;
                    },
                    0
                );

                _.each($scope.editableKohde.vaatimusRyhmat, function(kriteeri) {
                    if (kriteeri.vaatimukset.length === 1 && !Utils.hasLocalizedText(kriteeri.vaatimukset[0])) {
                        kriteeri.vaatimukset = [];
                    }
                });

                list[index] = angular.copy($scope.editableKohde);
                $scope.kohde.peruMuokkaus();
            },
            peruMuokkaus: function() {
                $timeout(function() {
                    $scope.editableKohde = null;
                    delete $scope.originalKohde.$editointi;
                    $scope.originalKohde = null;
                });
            }
        };

        $scope.rivi = {
            poista: function(list, index) {
                list.splice(index, 1);
            },
            uusi: function(kriteeri, event) {
                if (_.isEmpty(kriteeri.vaatimukset)) {
                    kriteeri.vaatimukset = [];
                }
                kriteeri.vaatimukset.push({ jarjestys: kriteeri.vaatimukset.length });

                // Set focus to newly added field
                const parent = angular.element(event.currentTarget).closest("table");
                $timeout(function() {
                    const found = parent.find(".form-control");
                    if (found.length > 0) {
                        found[found.length - 1].focus();
                    }
                }, 100);
            }
        };

        if ($scope.eiKohdealueita && (angular.isUndefined($scope.ammattitaito) || $scope.ammattitaito === null)) {
            $scope.uudenKohdealueenNimi = {
                fi: "Nimetön"
            };
            $scope.kohdealue.uusi();
        }
    })
    .directive("ammattitaito", function(YleinenData, $timeout, TutkinnonOsaLeikelautaService) {
        return {
            templateUrl: "views/partials/ammattitaito.html",
            restrict: "E",
            scope: {
                ammattitaito: "=",
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                eiKohdealueita: "@"
            },
            controller: "ammattitaitoCtrl",
            link: function(scope: any) {
                scope.eiKohdealueita = scope.eiKohdealueita === "true" || scope.eiKohdealueita === true;
                scope.editAllowed = scope.editAllowed === "true" || scope.editAllowed === true;

                scope.vaatimukset = [
                    {
                        otsikko: ""
                    }
                ];

                scope.$on("ammattitaitoasteikot", function() {
                    scope.ammattitaitoasteikot = YleinenData.ammattitaitoasteikot;
                });

                scope.elementDragged = false;

                scope.sortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
                    connectWith: ".container-items-ammattitaito, .container-items-leikelauta",
                    start: function() {
                        scope.elementDragged = true;
                    }
                });

                scope.kohdeSortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
                    connectWith: ".container-items-vaatimuksenKohteet, .container-items-leikelauta",
                    start: function() {
                        scope.elementDragged = true;
                    }
                });

                scope.kriteeriSortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
                    axis: "y",
                    cancel: ".row-adder",
                    handle: ".drag-enable",
                    items: "tr:not(.row-adder)"
                });

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

                // is-open attribuutti on annettava modelina accordionille,
                // jotta ui-sortable voidaan disabloida lukutilassa.
                function setAccordion(mode) {
                    const obj = scope.ammattitaito;
                    _.each(obj, function(kohdealue) {
                        kohdealue.$accordionOpen = mode;
                        _.each(kohdealue.vaatimuksenKohteet, function(kohde) {
                            kohde.$accordionOpen = mode;
                        });
                    });
                }

                function accordionState() {
                    const obj = _.first(scope.ammattitaito);
                    return obj && obj.$accordionOpen;
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

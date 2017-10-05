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
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

angular
    .module("eperusteApp")
    .controller("arviointiCtrl", function(
        $scope,
        $translate,
        YleinenData,
        Varmistusdialogi,
        $timeout,
        Utils,
        ArviointiPreferences,
        Kaanna
    ) {
        $scope.showNewKohdealueInput = false;

        $scope.arviointiasteikkoChanged = function(kohdealue) {
            ArviointiPreferences.setting("asteikko", kohdealue.$newkohde.arviointiasteikko);
        };

        $scope.kohdealue = {
            uusi: function() {
                if (!$scope.uudenKohdealueenNimi) {
                    return;
                }

                if (angular.isUndefined($scope.arviointi) || $scope.arviointi === null) {
                    $scope.arviointi = [];
                }

                var kohdealue = {
                    otsikko: {},
                    $accordionOpen: true
                };
                kohdealue.otsikko[YleinenData.kieli] = $scope.uudenKohdealueenNimi;

                $scope.arviointi.push(kohdealue);

                $scope.uudenKohdealueenNimi = null;
                $scope.showNewKohdealueInput = false;
            },
            cancel: function() {
                $scope.showNewKohdealueInput = false;
                $scope.uudenKohdealueenNimi = null;
            }
        };

        const valmisteleKriteerit = function(arr, tasot) {
            _.each(tasot, function(taso) {
                arr.push({
                    _osaamistaso: taso.id,
                    kriteerit: [{}]
                });
            });
        };

        $scope.kohde = {
            muokkaa: function(kohde, event) {
                if (event) {
                    event.stopPropagation();
                }
                $scope.originalKohde = kohde;
                kohde.$editointi = true;
                $scope.editableKohde = angular.copy(kohde);
                var kriteerit = $scope.editableKohde.osaamistasonKriteerit;
                if (_.isArray(kriteerit) && _.isEmpty(kriteerit)) {
                    valmisteleKriteerit(kriteerit, $scope.arviointiasteikot[kohde._arviointiAsteikko].osaamistasot);
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
                    showInputArea: true,
                    arviointiasteikko: ArviointiPreferences.setting("asteikko")
                };
            },
            isAdding: function(kohdealue) {
                return !_.isEmpty(kohdealue.$newkohde);
            },
            editArviointiasteikko: arvioinninkohde => {
                $scope.editableKohde.$editArviointiasteikko = true;
                $scope.editableKohde.$editedArviointiasteikko = true;
            },
            cancelEditingArviointiasteikko: arvioinninkohde => {
                $scope.editableKohde.$editArviointiasteikko = false;
            },
            cahngeArviointiasteikko: arvioinninkohde => {
                arvioinninkohde.$vanhaArviointiAsteikko = arvioinninkohde._arviointiAsteikko;
                arvioinninkohde._arviointiAsteikko = $scope.editableKohde.$uusiArviointiAsteikko.id;
                $scope.editableKohde._arviointiAsteikko = $scope.editableKohde.$uusiArviointiAsteikko.id;
                $scope.editableKohde.$editArviointiasteikko = false;

                // Jos on sama arviointiasteikko, ei tehdä mitään
                if (
                    parseInt($scope.editableKohde._arviointiAsteikko) ===
                    parseInt(arvioinninkohde.$vanhaArviointiAsteikko)
                ) {
                    return;
                }

                $scope.editableKohde.osaamistasonKriteerit = [];
                valmisteleKriteerit(
                    $scope.editableKohde.osaamistasonKriteerit,
                    $scope.editableKohde.$uusiArviointiAsteikko.osaamistasot
                );

                // Osaamistasojen vastaavuudet kun käydään läpi alkuperäistä arvioinninkohdetta
                const osaamistasoVastaavuudet = {
                    1: 1,
                    2: 5,
                    3: 7,
                    4: 9,
                    5: 2,
                    6: 6,
                    7: 3,
                    8: 8,
                    9: 4
                };

                _.each($scope.originalKohde.osaamistasonKriteerit, osaamistasonKriteeri => {
                    const osaamistasoId = parseInt(osaamistasonKriteeri._osaamistaso);
                    const kohdeOsaamistasoId = osaamistasoVastaavuudet[osaamistasoId];

                    const kohdeOsaamistaso = _.find($scope.editableKohde.osaamistasonKriteerit, editableOaKriteeri => {
                        return parseInt(editableOaKriteeri._osaamistaso) === kohdeOsaamistasoId;
                    });

                    // Kopioidaan kriteerit jos osaamistaso löytyy kohteesta
                    if (kohdeOsaamistaso) {
                        kohdeOsaamistaso.kriteerit = osaamistasonKriteeri.kriteerit;
                    }
                });
            },
            uusi: function(kohdealue) {
                if (angular.isUndefined(kohdealue.arvioinninKohteet) || kohdealue.arvioinninKohteet === null) {
                    kohdealue.arvioinninKohteet = [];
                }

                var kohde = {
                    otsikko: {},
                    selite: {
                        fi: "Opiskelija",
                        sv: "Den studerande"
                    },
                    _arviointiAsteikko: kohdealue.$newkohde.arviointiasteikko.id,
                    osaamistasonKriteerit: [],
                    $accordionOpen: true
                };

                kohde.otsikko[YleinenData.kieli] = kohdealue.$newkohde.nimi;

                valmisteleKriteerit(kohde.osaamistasonKriteerit, kohdealue.$newkohde.arviointiasteikko.osaamistasot);

                kohdealue.arvioinninKohteet.push(kohde);
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
                _.each($scope.editableKohde.osaamistasonKriteerit, function(kriteeri) {
                    if (kriteeri.kriteerit.length === 1 && !Utils.hasLocalizedText(kriteeri.kriteerit[0])) {
                        kriteeri.kriteerit = [];
                    }
                });
                list[index] = angular.copy($scope.editableKohde);
                $scope.kohde.peruMuokkaus();
            },
            peruMuokkaus: function() {
                $timeout(function() {
                    $scope.editableKohde = null;
                    delete $scope.originalKohde.$editointi;
                    $scope.originalKohde._arviointiAsteikko = $scope.originalKohde.$vanhaArviointiAsteikko;
                    $scope.originalKohde = null;
                });
            }
        };

        $scope.rivi = {
            poista: function(list, index) {
                list.splice(index, 1);
            },
            uusi: function(kriteeri, event) {
                if (_.isEmpty(kriteeri.kriteerit)) {
                    kriteeri.kriteerit = [];
                }
                kriteeri.kriteerit.push({});
                // Set focus to newly added field
                let parent = angular.element(event.currentTarget).closest("table");
                $timeout(function() {
                    let found = parent.find(".form-control");
                    if (found.length > 0) {
                        found[found.length - 1].focus();
                    }
                }, 100);
            }
        };

        if ($scope.eiKohdealueita && (angular.isUndefined($scope.arviointi) || $scope.arviointi === null)) {
            $scope.uudenKohdealueenNimi = "automaattinen";
            $scope.kohdealue.uusi();
        }
    })
    .directive("arviointi", function(YleinenData, $timeout, TutkinnonOsaLeikelautaService) {
        return {
            template: require("views/partials/arviointi.html"),
            restrict: "E",
            scope: {
                arviointi: "=",
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                eiKohdealueita: "@",
                tyyppi: "@?"
            },
            controller: "arviointiCtrl",
            link: function(scope: any) {
                scope.eiKohdealueita = scope.eiKohdealueita === "true" || scope.eiKohdealueita === true;
                scope.editAllowed = scope.editAllowed === "true" || scope.editAllowed === true;

                scope.arviointiasteikot = YleinenData.arviointiasteikot || {};

                YleinenData.haeArviointiasteikot();

                scope.$on("arviointiasteikot", function() {
                    scope.arviointiasteikot = YleinenData.arviointiasteikot;
                });

                scope.elementDragged = false;

                scope.sortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
                    $$id: "arviointi sortable",
                    connectWith: ".container-items-kohteet, .container-items-leikelauta"
                });

                scope.kriteeriSortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
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

                /**
                 * is-open attribuutti on annettava modelina accordionille, jotta
                 * ui-sortable voidaan disabloida lukutilassa.
                 */
                function setAccordion(mode) {
                    let obj = scope.arviointi;
                    _.each(obj, function(kohdealue) {
                        kohdealue.$accordionOpen = mode;
                        _.each(kohdealue.arvioinninKohteet, function(kohde) {
                            kohde.$accordionOpen = mode;
                        });
                    });
                }

                function accordionState() {
                    let obj = _.first(scope.arviointi);
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
    })
    .controller("arvioinninTekstiKenttaCtrl", function($scope, Varmistusdialogi) {
        $scope.muokkaustila = false;
        $scope.editoitava = null;

        $scope.poistaAlkio = function(item, list, event) {
            $scope.estaEventti(event);
            if (_.isEmpty(item)) {
                _.remove(list, item);
                return;
            }
            Varmistusdialogi.dialogi({
                otsikko: "varmista-osion-poisto-otsikko",
                teksti: "varmista-osion-poisto-teksti",
                primaryBtn: "poista",
                successCb: function() {
                    _.remove(list, item);
                }
            })();
        };

        $scope.estaEventti = function($event) {
            if ($event) {
                $event.stopPropagation();
            }
        };

        $scope.asetaMuokkaustila = function(mode, $event) {
            $scope.muokkaustila = mode;
            $scope.editoitava = mode ? angular.copy($scope.sisaltoteksti) : null;
            $scope.estaEventti($event);
        };

        $scope.hyvaksyMuutos = function($event) {
            $scope.estaEventti($event);
            $scope.sisaltoteksti = angular.copy($scope.editoitava);
            $scope.asetaMuokkaustila(false);
        };

        $scope.peruMuutos = function($event) {
            $scope.estaEventti($event);
            $scope.asetaMuokkaustila(false);
        };
    })
    .directive("arvioinninTekstikentta", function() {
        return {
            template: require("views/partials/arvioinninTekstikentta.html"),
            restrict: "E",
            scope: {
                sisalto: "=",
                sisaltoalue: "=",
                editAllowed: "=",
                sisaltoteksti: "=",
                clickable: "@?",
                editmode: "="
            },
            controller: "arvioinninTekstiKenttaCtrl"
        };
    })
    .service("ArviointiPreferences", function() {
        this.data = {};
        this.setting = function(key, value) {
            if (arguments.length === 1) {
                return this.data[key];
            }
            this.data[key] = value;
        };
    });

// Kustomoitu accordion group, lisätty isElementDragged-tarkastelu
// jotta ui-sortable toimii accordionin kanssa.
angular.module("template/accordion/accordion-group.html", []).run([
    "$templateCache",
    function($templateCache) {
        $templateCache.put(
            "template/accordion/accordion-group.html",
            "<div class='panel panel-default'>\n" +
                "  <div class='panel-heading'>\n" +
                "    <h4 class='panel-title'>\n" +
                "      <a class='accordion-toggle' ng-click='$parent.isElementDragged() || toggleOpen()' accordion-transclude='heading'><span ng-class='{\"text-muted\": isDisabled}'>{{heading}}</span></a>\n" +
                "    </h4>\n" +
                "  </div>\n" +
                "  <div class='panel-collapse' uib-collapse='!isOpen'>\n" +
                "    <div class='panel-body' ng-transclude></div>\n" +
                "  </div>\n" +
                "</div>"
        );
    }
]);

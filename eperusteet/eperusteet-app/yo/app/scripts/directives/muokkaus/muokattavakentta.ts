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

import _ from "lodash";
import * as angular from "angular";

angular
    .module("eperusteApp")
    .directive("muokkauskenttaRaamit", Utils => {
        return {
            template: require("views/partials/muokkaus/muokattavaKentta.html"),
            restrict: "A",
            transclude: true,
            scope: {
                model: "=",
                piilotaOtsikko: "@?",
                field: "="
            },
            link: (scope, element, attrs) => {
                scope.otsikko = _.isString(scope.model) ? "muokkaus-" + scope.model + "-header" : scope.model;
                scope.hasModel = !_.isString(scope.model);
                scope.canCollapse = attrs.collapsible || false;
                scope.collapsed = false;
                scope.isEmpty = model => {
                    return !Utils.hasLocalizedText(model);
                };

                element.addClass("list-group-item ");
                element.attr("ng-class", "");

                scope.$watch("field.$editing", value => {
                    if (value) {
                        scope.collapsed = false;
                    }
                });
            }
        };
    })
    .directive("muokattavaKentta", ($compile, $rootScope, Editointikontrollit, $q, $timeout) => {
        return {
            restrict: "E",
            replace: true,
            scope: {
                field: "=fieldInfo",
                objectReady: "=objectPromise",
                removeField: "&?",
                editEnabled: "="
            },
            controller: ($scope, YleinenData, MuokkausUtils, Varmistusdialogi, Utils) => {
                $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

                if ($scope.objectReady) {
                    $scope.$watch("objectReady", newObjectReadyPromise => {
                        newObjectReadyPromise.then(newObject => {
                            $scope.object = newObject;
                        });
                    });
                }

                function poistaOsio(value) {
                    if (angular.isString(value)) {
                        MuokkausUtils.nestedSet($scope.object, $scope.field.path, ".", "");
                    } else {
                        MuokkausUtils.nestedSet($scope.object, $scope.field.path, ".", undefined);
                    }
                    if (!$scope.mandatory) {
                        $scope.removeField({ fieldToRemove: $scope.field });
                    }
                }

                $scope.suljeOsio = () => {
                    // Jos kentässä on dataa, kysytään varmistus.
                    const getValue = MuokkausUtils.nestedGet($scope.object, $scope.field.path, ".");
                    if (!_.isEmpty(getValue)) {
                        Varmistusdialogi.dialogi({
                            otsikko: "varmista-osion-poisto-otsikko",
                            teksti: "varmista-osion-poisto-teksti",
                            primaryBtn: "poista",
                            successCb: () => {
                                poistaOsio(getValue);
                            }
                        })();
                    } else {
                        poistaOsio(getValue);
                    }
                };

                function getTitlePath() {
                    // FIXME: ?
                    return (
                        (_ as any).initial($scope.field.path.split("."), 1).join(".") +
                        "." +
                        $scope.field.originalLocaleKey
                    );
                }

                $scope.editOsio = () => {
                    // Assumed that field has a title at upper level in hierarchy
                    $scope.titlePath = getTitlePath();
                    $scope.originalContent = angular.copy(
                        MuokkausUtils.nestedGet($scope.object, $scope.field.path, ".")
                    );
                    $scope.originalTitle = angular.copy(MuokkausUtils.nestedGet($scope.object, $scope.titlePath, "."));
                    $scope.field.$editing = true;
                };

                $scope.okEdit = () => {
                    $scope.titlePath = $scope.titlePath || getTitlePath();
                    const title = MuokkausUtils.nestedGet($scope.object, $scope.titlePath, ".");
                    if (Utils.hasLocalizedText(title)) {
                        // Force model update
                        $rootScope.$broadcast("notifyCKEditor");

                        $scope.originalContent = null;
                        $scope.originalTitle = null;
                        $scope.field.$editing = false;
                    }
                };

                $scope.cancelEdit = () => {
                    if (!$scope.originalContent) {
                        // New, can delete
                        poistaOsio(MuokkausUtils.nestedGet($scope.object, $scope.field.path, "."));
                    } else {
                        MuokkausUtils.nestedSet($scope.object, $scope.field.path, ".", $scope.originalContent, true);
                        MuokkausUtils.nestedSet($scope.object, $scope.titlePath, ".", $scope.originalTitle, true);
                        $scope.field.$editing = false;
                    }
                };
            },
            link: (scope, element) => {
                const typeParams = scope.field.type.split(".");

                $q
                    .all({
                        object: scope.objectReady,
                        editMode: Editointikontrollit.getEditModePromise()
                    })
                    .then(values => {
                        scope.object = values.object;
                        // TODO fix bug, vuosiluokkakokonaisuus: editMode is false on first edit
                        scope.editMode = values.editMode;

                        if (!scope.field.mandatory) {
                            const contentFrame = angular
                                .element("<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>")
                                .append(getElementContent(typeParams[0]));

                            populateElementContent(contentFrame);
                        } else {
                            populateElementContent(getElementContent(typeParams[0]));
                        }
                    });

                const ELEMENT_MAP = {
                    "editor-header": ["addEditorAttributesFor", "<h3>"],
                    "text-input": ["addInputAttributesFor", "<input>", { "editointi-kontrolli": "" }],
                    "input-area": ["addInputAttributesFor", "<textarea>", { "editointi-kontrolli": "" }],
                    "editor-text": ["addEditorAttributesFor", "<p>"],
                    "editor-area": ["addEditorAttributesFor", "<div>"],
                    arviointi: [
                        "",
                        "<arviointi>",
                        {
                            "editointi-sallittu": "true",
                            arviointi: "object." + scope.field.path,
                            "edit-enabled": "editEnabled"
                        }
                    ],
                    ammattitaito: [
                        "",
                        "<ammattitaito>",
                        {
                            "editointi-sallittu": "true",
                            ammattitaito: "object." + scope.field.path,
                            "edit-enabled": "editEnabled"
                        }
                    ],
                    ammattitaitovaatimukset2019: [
                        "",
                        "<tutkinnonosan-ammattitaitovaatimukset2019>",
                        {
                            "editointi-sallittu": "true",
                            "edit-enabled": "editEnabled",
                            ammattitaitovaatimukset: "object." + scope.field.path,
                        }
                    ],
                    geneerinenarviointi: [
                        "",
                        "<tutkinnonosangeneerisetammattitaitovaatimukset>",
                        {
                            "editointi-sallittu": "true",
                            "edit-enabled": "editEnabled",
                            geneerinen: "object." + scope.field.path,
                        }
                    ],
                    valmaarviointi: [
                        "",
                        "<valmaarviointi>",
                        {
                            "editointi-sallittu": "true",
                            valmaarviointi: "object." + scope.field.path,
                            "edit-enabled": "editEnabled"
                        }
                    ],
                    "vuosiluokkakokonaisuuden-osaaminen": [
                        "",
                        "<div>",
                        {
                            "editointi-sallittu": "true",
                            "vuosiluokkakokonaisuuden-osaaminen": "object." + scope.field.path,
                            "edit-enabled": "editEnabled"
                        }
                    ],
                    osaamistavoitteet: []
                };
                ELEMENT_MAP.osaamistavoitteet = _.merge(_.clone(ELEMENT_MAP.arviointi), {
                    tyyppi: "osaamistavoitteet"
                });
                ELEMENT_MAP.osaamistavoitteet = _.merge(_.clone(ELEMENT_MAP.arviointi), {
                    tyyppi: "osaamisen-arviointi"
                });

                const mapperFns = {
                    addEditorAttributesFor: element => {
                        element
                            .addClass("list-group-item-text")
                            .attr("ng-model", "object." + scope.field.path)
                            .attr("ckeditor", "")
                            .attr("editing-enabled", "{{editMode}}");
                        const placeholder = scope.field.placeholder
                            ? scope.field.placeholder
                            : "muokkaus-" + scope.field.localeKey + "-placeholder";
                        element.attr("editor-placeholder", placeholder);
                        return element;
                    },
                    addInputAttributesFor: element => {
                        return element
                            .addClass("form-control")
                            .attr("ng-model", "object." + scope.field.path)
                            .attr("placeholder", "{{ 'muokkaus-" + scope.field.localeKey + "-placeholder' | kaanna }}");
                    }
                };

                function wrapEditor(element) {
                    const editWrapper = angular.element('<div ng-if="field.$editing"></div>');
                    editWrapper.append(element);
                    const viewWrapper = angular.element(
                        '<div ng-if="!field.$editing" ng-bind-html="valitseKieli(object.' +
                            scope.field.path +
                            ') | unsafe"></div>'
                    );
                    const wrapper = angular.element("<div>");
                    wrapper.append(editWrapper, viewWrapper);
                    return wrapper;
                }

                function getElementContent(elementType) {
                    const mapped = ELEMENT_MAP[elementType];
                    if (!mapped) {
                        return null;
                    }

                    let element = (mapped[0] ? mapperFns[mapped[0]] : _.identity)(angular.element(mapped[1]));
                    _.each(mapped[2], (value, key) => {
                        element.attr(key, value);
                    });
                    if (element !== null && scope.field.localized) {
                        element.attr("slocalized", "");
                    }

                    if (scope.field.isolateEdit) {
                        element = wrapEditor(element);
                    }

                    return element;
                }

                function populateElementContent(content) {
                    element.append(content);
                    $timeout(() => {
                        $compile(element.contents())(scope);
                    });
                }
            }
        };
    })
    .directive("vaihtoehtoisenKentanRaami", () => {
        return {
            template: require("views/directives/vaihtoehtoisenkentanraami.html"),
            restrict: "E",
            transclude: true,
            link: (scope, element) => {
                scope.$watch("editEnabled", () => {
                    const buttons = element.find(".field-buttons");
                    const header = element.closest("li.kentta").find(".osio-otsikko");
                    buttons.detach().appendTo(header);
                });
            },
            controller: $scope => {
                $scope.callFn = ($event, fn) => {
                    if ($event) {
                        $event.preventDefault();
                        $event.stopPropagation();
                    }
                    $scope[fn]();
                };
            }
        };
    })
    .directive("localized", ($timeout, Kieli) => {
        return {
            priority: 5,
            restrict: "A",
            require: "ngModel",
            link: (scope, element, attrs, ngModelCtrl: any) => {
                ngModelCtrl.$formatters.push(modelValue => {
                    if (angular.isUndefined(modelValue) || modelValue === null) {
                        return;
                    }
                    return modelValue[Kieli.getSisaltokieli()];
                });

                ngModelCtrl.$parsers.push(function(viewValue) {
                    let localizedModelValue = ngModelCtrl.$modelValue;

                    if (localizedModelValue === null || _.isUndefined(localizedModelValue)) {
                        localizedModelValue = {};
                    }

                    localizedModelValue[Kieli.getSisaltokieli()] = viewValue;

                    return localizedModelValue;
                });

                scope.$on("changed:sisaltokieli", function(event, sisaltokieli) {
                    $timeout(() => {
                        if (
                            ngModelCtrl.$modelValue !== null &&
                            !angular.isUndefined(ngModelCtrl.$modelValue) &&
                            !_.isEmpty(ngModelCtrl.$modelValue[sisaltokieli])
                        ) {
                            ngModelCtrl.$setViewValue(ngModelCtrl.$modelValue[sisaltokieli]);
                        } else {
                            ngModelCtrl.$setViewValue("");
                        }
                        ngModelCtrl.$render();
                    });
                });
            }
        };
    })
    .directive("slocalized", function($timeout, Kieli) {
        return {
            priority: 5,
            restrict: "A",
            require: "ngModel",
            link: function(scope, element, attrs, ngModelCtrl: any) {
                ngModelCtrl.$formatters.push(function(modelValue) {
                    if (angular.isUndefined(modelValue) || modelValue === null) {
                        return "";
                    }
                    return modelValue[Kieli.getSisaltokieli()];
                });

                ngModelCtrl.$parsers.push(function(viewValue) {
                    let localizedModelValue = ngModelCtrl.$modelValue;

                    if (localizedModelValue === null || _.isUndefined(localizedModelValue)) {
                        localizedModelValue = {};
                    }

                    if (_.isString(viewValue)) {
                        localizedModelValue[Kieli.getSisaltokieli()] = viewValue;
                    }

                    return localizedModelValue;
                });

                scope.$on("changed:sisaltokieli", function(event, sisaltokieli) {
                    $timeout(() => {
                        if (
                            ngModelCtrl.$modelValue !== null &&
                            !angular.isUndefined(ngModelCtrl.$modelValue) &&
                            !_.isEmpty(ngModelCtrl.$modelValue[sisaltokieli])
                        ) {
                            ngModelCtrl.$setViewValue(ngModelCtrl.$modelValue[sisaltokieli]);
                        } else {
                            ngModelCtrl.$setViewValue("");
                        }
                        ngModelCtrl.$render();
                    });
                });
            }
        };
    });

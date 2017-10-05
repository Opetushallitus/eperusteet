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
    .directive("osallinenOsa", $compile => {
        return {
            template: require("views/directives/osallinenosa.html"),
            restrict: "AE",
            transclude: true,
            scope: {
                model: "=",
                config: "=",
                versiot: "=",
                overwrittenVaihdaVersio: "=",
                overwrittenRevertCb: "=",
                overwrittenDeleteRedirectCb: "=",
                editableModel: "="
            },
            controller: "OsallinenOsaController",
            link: function(scope: any, element: any) {
                if (_.isObject(scope.config) && scope.config.fieldRenderer) {
                    element
                        .find(".tutkinnonosa-sisalto")
                        .empty()
                        .append($compile(angular.element(scope.config.fieldRenderer))(scope));
                }
            }
        };
    })
    .controller(
        "OsallinenOsaController",
        (
            $scope,
            $state,
            VersionHelper,
            $q,
            Lukitus,
            Editointikontrollit,
            FieldSplitter,
            Varmistusdialogi,
            $rootScope,
            Utils,
            $timeout,
            $stateParams
        ) => {
            $scope.isLocked = false;
            $scope.isNew = $stateParams.osanId === "uusi";
            $scope.editEnabled = false;
            $scope.muokkaa = () => Lukitus.lukitse(Editointikontrollit.startEditing);

            if ($scope.config.editingCallbacks) {
                Editointikontrollit.registerCallback($scope.config.editingCallbacks);
                Editointikontrollit.registerEditModeListener(mode => {
                    $scope.editEnabled = mode;
                });
            }

            if ($scope.isNew) {
                $scope.muokkaa();
            } else {
                Lukitus.genericTarkista(
                    () => {
                        $scope.isLocked = false;
                    },
                    lukonOmistaja => {
                        $scope.isLocked = true;
                        $scope.lockNotification = lukonOmistaja;
                    }
                );
            }

            $scope.isPublished = () => {
                return $scope.model.tila === "julkaistu";
            };

            $scope.generateBackHref = () => {
                return $state.href($scope.config.backState);
            };

            $scope.vaihdaVersio = () => {
                if ($scope.overwrittenVaihdaVersio) {
                    $scope.overwrittenVaihdaVersio();
                } else {
                    $scope.versiot.hasChanged = true;
                    VersionHelper.setUrl($scope.versiot);
                    //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tutkinnonOsa.id}, responseFn);
                }
            };

            $scope.revertCb = response => {
                if ($scope.overwrittenRevertCb) {
                    $scope.overwrittenRevertCb(response);
                } else {
                    // TODO
                    //responseFn(response);
                    //saveCb(response);
                }
            };

            $scope.addField = function(field) {
                let splitfield = FieldSplitter.process(field);
                let cssClass;
                if (splitfield.isMulti()) {
                    const index = splitfield.addArrayItem($scope.editableModel);
                    //$rootScope.$broadcast('osafield:update');
                    cssClass = splitfield.getClass(index);
                    field.$setEditable = index;
                } else {
                    field.visible = true;
                    field.$added = true;
                    cssClass = FieldSplitter.getClass(field);
                }
                ($scope.config.addFieldCb || angular.noop)(field);
                $rootScope.$broadcast("osafield:update");
                $timeout(() => {
                    Utils.scrollTo("li." + cssClass);
                }, 200);
            };

            $scope.removeWhole = function() {
                Varmistusdialogi.dialogi({
                    otsikko: "varmista-poisto",
                    teksti: $scope.config.removeWholeConfirmationText || "",
                    primaryBtn: "poista",
                    successCb: function() {
                        $scope.config.removeWholeFn(function() {
                            Editointikontrollit.unregisterCallback();
                            if ($scope.overwrittenDeleteRedirectCb) {
                                $scope.overwrittenDeleteRedirectCb();
                            } else {
                                $state.go($scope.config.backState[0], $scope.config.backState[1], {
                                    reload: true
                                });
                            }
                        });
                    }
                })();
            };

            $scope.actionButtonFn = function(button) {
                (button.callback || angular.noop)();
            };

            $scope.shouldHide = function(button) {
                const custom = button.hide ? button.hide : "";
                const defaults = $scope.editEnabled || !$scope.versiot.latest;
                if (!custom) {
                    return defaults;
                }
                const parsed = $scope.$eval(custom);
                return defaults || parsed;
            };
        }
    );

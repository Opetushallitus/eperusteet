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

angular.module("eperusteApp").directive("muokkausOsaaminen", () => {
    return {
        templateUrl: "views/directives/osaaminen.html",
        restrict: "E",
        scope: {
            model: "=",
            versiot: "=",
            service: "="
        },
        controller: ($scope, Notifikaatiot, PerusteProjektiSivunavi, YleinenData, $stateParams, $state, Api) => {
            $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
            $scope.isNew = $stateParams.osanId === "uusi";
            $scope.editableModel = Api.copy($scope.model);
            $scope.editEnabled = false;
            $scope.data = {
                options: {
                    title: () => {
                        if (!$scope.editableModel.nimi) {
                            return "uusi-tutkinnonosa";
                        }
                        return $scope.editableModel.nimi;
                    },
                    editTitle: "muokkaa-osaaminen",
                    newTitle: "uusi-osaaminen",
                    backLabel: "laaja-alainen-osaaminen",
                    backState: [
                        "root.perusteprojekti.suoritustapa." + $stateParams.suoritustapa + "osalistaus",
                        {
                            suoritustapa: $stateParams.suoritustapa,
                            osanTyyppi: "osaaminen"
                        }
                    ],
                    removeWholeLabel: "poista-osaamiskokonaisuus",
                    removeWholeConfirmationText: "poistetaanko-osaamiskokonaisuus",
                    removeWholeFn: async cb => {
                        await $scope.editableModel.remove();
                        $scope.service.clearCache();
                        cb();
                    },
                    fields: [],
                    editingCallbacks: {
                        edit: async () => {
                            if (!$scope.isNew) {
                                $scope.model = await $scope.model.get();
                                $scope.editableModel = Api.copy($scope.model);
                            }
                        },
                        save: async () => {
                            if ($scope.isNew) {
                                $scope.editableModel = await $scope.editableModel.post();
                                Notifikaatiot.onnistui("tallennus-onnistui");
                                $scope.service.clearCache();
                                $state.go(
                                    $state.current,
                                    {
                                        suoritustapa: $stateParams.suoritustapa,
                                        osanTyyppi: $stateParams.osanTyyppi,
                                        osanId: $scope.editableModel.id
                                    },
                                    {
                                        reload: true
                                    }
                                );
                            } else {
                                $scope.editableModel = await $scope.editableModel.save();
                                Notifikaatiot.onnistui("tallennus-onnistui");
                                $scope.model = Api.copy($scope.editableModel);
                            }
                        },
                        cancel: () => {
                            if ($scope.isNew) {
                                $state.go($scope.data.options.backState[0], $scope.data.options.backState[1], {
                                    reload: true
                                });
                            } else {
                                $scope.editableModel = Api.copy($scope.model);
                            }
                        },
                        notify: value => {
                            $scope.editEnabled = value;
                            PerusteProjektiSivunavi.setVisible(!value);
                        },
                        validate: () => {
                            return true;
                        }
                    }
                }
            };
        }
    };
});

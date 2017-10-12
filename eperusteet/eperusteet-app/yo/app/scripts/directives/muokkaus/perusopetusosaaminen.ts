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
    .directive("perusopetusMuokkausOsaaminen", function() {
        return {
            template: require("views/directives/perusopetus/osaaminen.html"),
            restrict: "E",
            scope: {
                model: "=",
                versiot: "="
            },
            controller: "perusopetusMuokkausOsaaminenController"
        };
    })
    .controller("perusopetusMuokkausOsaaminenController", function(
        $scope,
        PerusopetusService,
        Notifikaatiot,
        PerusteProjektiSivunavi,
        YleinenData,
        $stateParams,
        CloneHelper,
        $timeout,
        $state,
        Lukitus
    ) {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.editableModel = {};
        $scope.editEnabled = false;

        let cloner = CloneHelper.init(["nimi", "kuvaus"]);

        let callbacks = {
            edit: function() {
                cloner.clone($scope.editableModel);
            },
            save: function() {
                const isNew = !$scope.editableModel.id;
                PerusopetusService.saveOsa($scope.editableModel, $stateParams, function(tallennettu) {
                    $scope.editableModel = tallennettu;
                    PerusopetusService.clearCache();
                    if (isNew) {
                        $state.go($state.current, _.extend(_.clone($stateParams), { osanId: tallennettu.id }), {
                            reload: true
                        });
                    } else {
                        Lukitus.vapauta();
                    }
                    Notifikaatiot.onnistui("tallennus-onnistui");
                });
            },
            cancel: function() {
                cloner.restore($scope.editableModel);
                if ($scope.editableModel.$isNew) {
                    $timeout(function() {
                        $state.go.apply($state, $scope.data.options.backState);
                    });
                } else {
                    Lukitus.vapauta();
                }
            },
            notify: function(value) {
                $scope.editEnabled = value;
                PerusteProjektiSivunavi.setVisible(!value);
            },
            validate: function() {
                return true;
            }
        };

        $scope.data = {
            options: {
                title: function() {
                    return $scope.editableModel.nimi;
                },
                editTitle: "muokkaa-osaaminen",
                newTitle: "uusi-osaaminen",
                backLabel: "laaja-alainen-osaaminen",
                backState: [
                    "root.perusteprojekti.suoritustapa.osalistaus",
                    { suoritustapa: $stateParams.suoritustapa, osanTyyppi: PerusopetusService.OSAAMINEN }
                ],
                removeWholeLabel: "poista-osaamiskokonaisuus",
                removeWholeConfirmationText: "poistetaanko-osaamiskokonaisuus",
                removeWholeFn: function(then) {
                    PerusopetusService.deleteOsa($scope.editableModel, function() {
                        PerusopetusService.clearCache();
                        then();
                    });
                },
                fields: [],
                editingCallbacks: callbacks
            }
        };

        $scope.model.then(function(data) {
            $scope.editableModel = angular.copy(data);
        });
    });

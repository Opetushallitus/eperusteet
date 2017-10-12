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
    .value("UiKieli", { kielikoodi: "fi" })
    .service("KielipreferenssiUpdater", function($rootScope, Profiili) {
        $rootScope.$on("changed:sisaltokieli", function(event, value) {
            Profiili.setPreferenssi("sisaltokieli", value);
        });
        this.noop = angular.noop;
    })
    .service("Kieli", function($rootScope, $state, $stateParams, UiKieli) {
        var sisaltokieli = "fi";

        var SISALTOKIELET = ["fi", "sv", "se", "ru", "en"];

        this.kieliOrder = function(kielikoodi) {
            return _.indexOf(SISALTOKIELET, kielikoodi);
        };

        this.availableSisaltokielet = _.clone(SISALTOKIELET);

        var isValidKielikoodi = function(kielikoodi) {
            return _.indexOf(SISALTOKIELET, kielikoodi) > -1;
        };

        this.setAvailableSisaltokielet = function(kielet) {
            if (_.isArray(kielet) && !_.isEmpty(kielet)) {
                var isValid = _.all(_.map(kielet, isValidKielikoodi));
                if (isValid) {
                    this.availableSisaltokielet = kielet;
                    $rootScope.$broadcast("update:sisaltokielet", kielet);
                }
            }
        };

        this.resetSisaltokielet = function() {
            this.availableSisaltokielet = SISALTOKIELET;
            $rootScope.$broadcast("update:sisaltokielet", SISALTOKIELET);
        };

        this.setSisaltokieli = function(kielikoodi) {
            if (_.indexOf(this.SISALTOKIELET, kielikoodi) > -1) {
                var old = sisaltokieli;
                sisaltokieli = kielikoodi;
                if (old !== kielikoodi) {
                    $rootScope.$broadcast("changed:sisaltokieli", kielikoodi);
                }
            }
        };

        this.getSisaltokieli = function() {
            return sisaltokieli;
        };

        this.setUiKieli = function(kielikoodi) {
            if (isValidKielikoodi(kielikoodi)) {
                UiKieli.kielikoodi = kielikoodi;
                $state.go($state.current.name, _.merge($stateParams, { lang: kielikoodi }), { reload: true });
            }
        };

        this.getUiKieli = function() {
            return UiKieli.kielikoodi;
        };

        this.isValidKielikoodi = isValidKielikoodi;
        this.SISALTOKIELET = SISALTOKIELET;
    })
    .directive("kielenvaihto", function() {
        return {
            restrict: "AE",
            scope: {
                modal: "@modal"
            },
            controller: "KieliCtrl",
            template: require("views/directives/kielenvaihto.pug")
        };
    })
    .controller("KieliCtrl", function(
        $scope,
        $stateParams,
        YleinenData,
        $state,
        Kieli,
        Profiili,
        $q,
        KielipreferenssiUpdater
    ) {
        KielipreferenssiUpdater.noop();
        $scope.isModal = $scope.modal === "true";
        $scope.sisaltokielet = [];
        $scope.sisaltokieli = Kieli.getSisaltokieli();
        $scope.kieliOrder = Kieli.kieliOrder;
        $scope.uiLangChangeAllowed = true;
        var stateInit = $q.defer();
        var casFetched = $q.defer();

        var info = Profiili.profiili();
        if (info.$casFetched) {
            casFetched.resolve();
        }

        $scope.$on("$stateChangeSuccess", function() {
            stateInit.resolve();
        });

        $scope.$on("fetched:casTiedot", function() {
            casFetched.resolve();
        });

        $q.all([stateInit.promise, casFetched.promise]).then(function() {
            var lang = Profiili.lang();
            // Disable ui language change if language preference found in CAS
            if (Kieli.isValidKielikoodi(lang)) {
                $scope.uiLangChangeAllowed = false;
                Kieli.setUiKieli(lang);
            }
            var profiili = Profiili.profiili();
            if (profiili.preferenssit.sisaltokieli) {
                Kieli.setSisaltokieli(profiili.preferenssit.sisaltokieli);
            }
        });

        const updateSisaltokielet = value => {
            $scope.sisaltokielet = _.map(Kieli.SISALTOKIELET, kieli => {
                return {
                    kieli,
                    inUse: _.includes(value, kieli)
                };
            });
        };

        $scope.$on("update:sisaltokielet", function(event, value) {
            updateSisaltokielet(value);
        });

        $scope.$on("changed:sisaltokieli", function(event, value) {
            $scope.sisaltokieli = value;
        });

        $scope.setSisaltokieli = function(kieli) {
            Kieli.setSisaltokieli(kieli.kieli);
        };

        $scope.koodit = _.map(_.pairs(YleinenData.kielet), function(item) {
            return { koodi: item[1], nimi: item[0] };
        });
        $scope.kieli = YleinenData.kieli;

        $scope.$on("notifyCKEditor", function() {
            $scope.kieli = YleinenData.kieli;
        });

        $scope.vaihdaKieli = function(kielikoodi) {
            Kieli.setUiKieli(kielikoodi);
        };

        updateSisaltokielet(Kieli.SISALTOKIELET);
    });

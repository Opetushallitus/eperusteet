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
    .service("ProjektinMurupolkuService", function($rootScope, $state) {
        var PREFIX = "root.perusteprojekti.";
        var namecache = {};
        var URLS = {
            tutkinnonosat: ["suoritustapa.tutkinnonosat"],
            osalistaus: ["suoritustapa.osalistaus"],
            aipeosalistaus: ["suoritustapa.aipeosalistaus"],
            lukioosat: ["suoritustapa.lukioosat"],
            koulutuksenosa: ["suoritustapa.koulutuksenosat"]
        };
        var custom = [];

        this.getName = function(idKey, stateParams) {
            return namecache[idKey] ? namecache[idKey][stateParams[idKey]] : null;
        };

        this.getUrl = function(key, stateParams) {
            return $state.href(PREFIX + URLS[key], stateParams);
        };

        var STATES = {
            "suoritustapa.koulutuksenosa": {
                items: [{ url: "koulutuksenosa" }, { getName: "tutkinnonOsaViiteId" }]
            },
            "suoritustapa.tutkinnonosa": {
                items: [{ url: "tutkinnonosat" }, { getName: "tutkinnonOsaViiteId" }]
            },
            "suoritustapa.tekstikappale": {
                items: ["custom"]
            },
            "suoritustapa.osaalue": {
                items: [{ url: "osalistaus", label: { getName: "osanTyyppi" } }, "custom", { getName: "osanId" }]
            },
            "suoritustapa.aipeosaalue": {
                items: [{ url: "aipeosalistaus", label: { getName: "osanTyyppi" } }, "custom", { getName: "osanId" }]
            },
            "suoritustapa.aipeosaalue.oppiaine": {
                items: [{ url: "aipeosalistaus", label: { getName: "osanTyyppi" } }, "custom", { getName: "osanId" }]
            },
            "suoritustapa.lukioosaalue": {
                items: [{ url: "lukioosat", label: { getName: "osanTyyppi" } }, "custom", { getName: "osanId" }]
            }
        };

        this.get = function(stateName) {
            if (stateName.indexOf(PREFIX) === 0) {
                stateName = stateName.substring(PREFIX.length);
            }
            return STATES[stateName];
        };

        this.set = function(idKey, id, value) {
            if (!namecache[idKey]) {
                namecache[idKey] = {};
            }
            namecache[idKey][id] = value;
            $rootScope.$broadcast("update:projektinMurupolku");
        };

        this.setCustom = function(arr, broadcast) {
            custom = _.cloneDeep(arr);
            if (_.isUndefined(broadcast) || broadcast) {
                $rootScope.$broadcast("update:projektinMurupolku");
            }
        };

        this.getCustom = function() {
            return custom;
        };
    })
    .directive("projektinMurupolku", function() {
        return {
            template: require("views/directives/perusteprojekti/murupolku.html"),
            restrict: "AE",
            scope: {},
            controller: "ProjektinMurupolkuController"
        };
    })
    .controller("ProjektinMurupolkuController", function($scope, $state, ProjektinMurupolkuService, $stateParams) {
        $scope.isActive = true;
        $scope.isTekstikappale = false;

        function resolveLabel(item) {
            var fn = _.first(_.keys(item));
            var value = item[fn];
            return ProjektinMurupolkuService[fn](value, $stateParams);
        }

        function resolveItem(item, items) {
            var ret = item;
            if (_.isObject(item)) {
                if (_.has(item, "url")) {
                    var url = ProjektinMurupolkuService.getUrl(item.url, $stateParams);
                    ret = { url: url, label: item.label ? resolveLabel(item.label) : item.url };
                } else {
                    var label = resolveLabel(item);
                    ret = { label: label };
                }
                items.push(ret);
            } else if (item === "custom") {
                var customArr = ProjektinMurupolkuService.getCustom();
                if (!_.isEmpty(customArr) && _.isArray(customArr)) {
                    Array.prototype.push.apply(items, customArr);
                }
            }
        }

        function setCrumb() {
            var crumbConfig = ProjektinMurupolkuService.get($state.current.name);
            var items = crumbConfig ? _.cloneDeep(crumbConfig.items) : [];
            var resolvedItems = [];
            _.each(items, function(item) {
                resolveItem(item, resolvedItems);
            });
            $scope.items = resolvedItems;
        }

        $scope.$on("$stateChangeSuccess", setCrumb);
        $scope.$on("$stateChangeStart", function() {
            $scope.isTekstikappale = false;
            ProjektinMurupolkuService.setCustom([], false);
        });
        $scope.$on("update:projektinMurupolku", setCrumb);
        $scope.$on("disableEditing", function() {
            $scope.isActive = true;
            $scope.isTekstikappale = false;
        });
        $scope.$on("enableEditing", function() {
            $scope.isActive = false;
            if ($state.current.name === "root.perusteprojekti.suoritustapa.tekstikappale") {
                $scope.isTekstikappale = true;
            }
        });

        setCrumb();
    });

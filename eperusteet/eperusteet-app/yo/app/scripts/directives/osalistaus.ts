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
    .service("OrderHelper", function() {
        this.ORDER_OPTIONS = [
            { value: "oletus", label: "oletus" },
            { value: "nimi", label: "nimi" },
            { value: "muokattu", label: "muokattu-viimeksi" }
        ];
        this.ORDER_LAAJUUS = [{ value: "laajuus", label: "laajuus" }];
        this.ORDER_JARJESTYS = [{ value: "jarjestys", label: "tutkinnonosa-jarjestysnumero" }];
        this.ORDER_JNRO = [{ value: "jnro", label: "jnro" }];
        this.get = function(withLaajuus, koulutustyyppi) {
            var ret = withLaajuus ? this.ORDER_OPTIONS.concat(this.ORDER_LAAJUUS) : this.ORDER_OPTIONS;
            if (koulutustyyppi) {
                ret = ret.concat(this.ORDER_JARJESTYS);
            }
            return ret;
        };
        this.getWithJnro = function() {
            var ret = this.ORDER_OPTIONS.concat(this.ORDER_JNRO);
            return ret;
        };
    })
    .directive("osalistaus", function(OrderHelper, $compile, $stateParams) {
        return {
            templateUrl: "views/directives/osalistaus.html",
            restrict: "A",
            scope: {
                model: "=osalistaus",
                searchPlaceholder: "@?",
                emptyPlaceholder: "@?",
                showLaajuus: "@?",
                urlGenerator: "&",
                options: "=",
                koulutustyyppi: "="
            },
            controller: "OsalistausDirectiveController",
            link: function(scope: any, element: any, attrs) {
                if (scope.options && scope.options.extrafilter) {
                    var el = $compile(scope.options.extrafilter.template)(scope);
                    element
                        .find("#osalistausextrafilter")
                        .empty()
                        .append(el);
                }
                attrs.$observe("showLaajuus", function(value) {
                    scope.hasLaajuus = value === "true";
                    if ($stateParams.osanTyyppi !== "aihekokonaisuudet") {
                        scope.jarjestysOptions = OrderHelper.get(scope.hasLaajuus, scope.koulutustyyppi);
                    }
                });
                attrs.$observe("yksikko", function(value) {
                    scope.unit = value;
                });
            }
        };
    })
    .controller("OsalistausDirectiveController", function(
        $scope,
        $stateParams,
        Kaanna,
        Algoritmit,
        OrderHelper,
        Profiili,
        YleinenData,
        Kielimapper
    ) {
        var defaultPreferences = {
            nakymatyyli: "palikka"
        };

        $scope.isNumber = _.isNumber;
        $scope.jarjestysTapa = _.first(OrderHelper.get(null, $scope.koulutustyyppi)).value;
        $scope.jarjestysOrder = false;
        $scope.preferenssit = Profiili.profiili().resolved
            ? _.merge(defaultPreferences, Profiili.profiili().preferenssit)
            : defaultPreferences;
        $scope.kaytaJarjestysnumeroa = $scope.koulutustyyppi ? true : false;
        $scope.isVaTe = YleinenData.isValmaTelma($scope.koulutustyyppi);
        $scope.vateConverter = Kielimapper.mapTutkinnonosatKoulutuksenosat($scope.isVaTe);

        $scope.$on("kayttajaProfiiliPaivittyi", function() {
            $scope.preferenssit = _.merge($scope.preferenssit, Profiili.profiili().preferenssit);
        });

        $scope.$watch(
            "preferenssit.nakymatyyli",
            function(uusi) {
                if (Profiili.isResolved()) {
                    Profiili.setPreferenssi("nakymatyyli", uusi);
                }
            },
            true
        );

        $scope.search = {
            term: "",
            placeholder: $scope.searchPlaceholder || ""
        };

        if ($stateParams.osanTyyppi === "aihekokonaisuudet") {
            $scope.jarjestysOptions = OrderHelper.getWithJnro();
        } else {
            $scope.jarjestysOptions = OrderHelper.get(null, $scope.koulutustyyppi);
        }

        $scope.searchChanged = function(term) {
            $scope.search.term = term;
        };

        $scope.asetaJarjestys = function(tyyppi, suunta) {
            if ($scope.jarjestysTapa === tyyppi) {
                $scope.jarjestysOrder = !$scope.jarjestysOrder;
                suunta = $scope.jarjestysOrder;
            } else {
                $scope.jarjestysOrder = false;
                $scope.jarjestysTapa = tyyppi;
            }
        };

        $scope.jarjestys = function(data) {
            switch ($scope.jarjestysTapa) {
                case "nimi":
                    return Kaanna.kaanna(data.nimi);
                case "laajuus":
                    return data.laajuus;
                case "oletus":
                    if ($stateParams.osanTyyppi === "aihekokonaisuudet") {
                        return data.jnro;
                    }
                    break;
                case "muokattu":
                    return data.muokattu;
                case "jarjestys":
                    return data.jarjestys;
                case "jnro":
                    return data.jnro;
                default:
                    break;
            }
        };

        $scope.comparisonFn = function(value) {
            var rajaus = Algoritmit.rajausVertailu($scope.search.term, value, "nimi");
            var extrarajaus = true;
            if ($scope.options && $scope.options.extrafilter && $scope.options.extrafilter.model) {
                extrarajaus = $scope.options.extrafilter.fn($scope.options.extrafilter.model, value);
            }
            return extrarajaus && rajaus;
        };
    });

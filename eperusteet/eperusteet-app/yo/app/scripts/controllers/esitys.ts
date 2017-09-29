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

"use strict";
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .config(function($stateProvider) {
        $stateProvider
            .state("root.esitys", {
                url: "/esitys",
                template: "<div ui-view></div>"
            })
            .state("root.esitys.peruste", {
                url: "/:perusteId/:suoritustapa?prestate&projekti",
                templateUrl: "views/esitys.html",
                controller: "EsitysCtrl",
                resolve: {
                    peruste: function($stateParams, Perusteet) {
                        return Perusteet.get({ perusteId: $stateParams.perusteId }).$promise;
                    },
                    sisalto: function($stateParams, SuoritustapaSisalto) {
                        return SuoritustapaSisalto.get({
                            perusteId: $stateParams.perusteId,
                            suoritustapa: $stateParams.suoritustapa
                        }).$promise;
                    },
                    arviointiasteikot: function($stateParams, Arviointiasteikot) {
                        return Arviointiasteikot.list({}).$promise;
                    },
                    tutkinnonOsat: function($stateParams, PerusteTutkinnonosat) {
                        return PerusteTutkinnonosat.query({
                            perusteId: $stateParams.perusteId,
                            suoritustapa: $stateParams.suoritustapa
                        }).$promise;
                    },
                    koulutusalaService: "Koulutusalat",
                    opintoalaService: "Opintoalat"
                }
            })
            .state("root.esitys.peruste.rakenne", {
                url: "/rakenne",
                templateUrl: "eperusteet-esitys/views/rakenne.html",
                controller: "epEsitysRakenneController",
                resolve: {
                    // FIXME: ui-router bug or some '$on'-callback manipulating $stateParams?
                    // $stateParams changes between config and controller
                    //
                    // Got to live third-party libs
                    realParams: function($stateParams) {
                        return _.clone($stateParams);
                    }
                }
            })
            .state("root.esitys.peruste.tutkinnonosat", {
                url: "/tutkinnonosat",
                templateUrl: "eperusteet-esitys/views/tutkinnonosat.html",
                controller: "epEsitysTutkinnonOsatController"
            })
            .state("root.esitys.peruste.tutkinnonosa", {
                url: "/tutkinnonosat/:id",
                templateUrl: "eperusteet-esitys/views/tutkinnonosa.html",
                controller: "epEsitysTutkinnonOsaController"
            })
            .state("root.esitys.peruste.tekstikappale", {
                url: "/sisalto/:osanId",
                templateUrl: "eperusteet-esitys/views/tekstikappale.html",
                controller: "epEsitysSisaltoController",
                resolve: {
                    tekstikappaleId: function($stateParams) {
                        return $stateParams.osanId;
                    },
                    tekstikappale: function(tekstikappaleId, PerusteenOsat) {
                        return PerusteenOsat.getByViite({ viiteId: tekstikappaleId }).$promise;
                    },
                    lapset: function(sisalto, tekstikappaleId, epTekstikappaleChildResolver) {
                        return epTekstikappaleChildResolver.get(sisalto, tekstikappaleId);
                    }
                }
            })
            .state("root.esitys.peruste.tiedot", {
                url: "/tiedot",
                templateUrl: "eperusteet-esitys/views/tiedot.html",
                controller: "epEsitysTiedotController"
            });
    })
    .controller("EsitysCtrl", function(
        $scope,
        $stateParams,
        sisalto,
        peruste,
        YleinenData,
        $state,
        Algoritmit,
        tutkinnonOsat,
        Kaanna,
        arviointiasteikot,
        Profiili,
        PdfCreation,
        koulutusalaService,
        opintoalaService,
        Kieli,
        TermistoService
    ) {
        $scope.$prestate = $stateParams.prestate;

        const AmmatillisetKoulutustyypit = ["koulutustyyppi_1", "koulutustyyppi_11", "koulutustyyppi_12"];
        $scope.isAmmatillinen = _.includes(AmmatillisetKoulutustyypit, peruste.koulutustyyppi);

        TermistoService.setPeruste(peruste);
        $scope.isOpas = peruste.tyyppi === "opas";
        $scope.Koulutusalat = koulutusalaService;
        $scope.Opintoalat = opintoalaService;
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        var isTutkinnonosatActive = function() {
            return $state.is("root.esitys.peruste.tutkinnonosat") || $state.is("root.esitys.peruste.tutkinnonosa");
        };
        $scope.navi = {
            items: [
                { label: "perusteen-tiedot", link: ["root.esitys.peruste.tiedot"], $glyph: "list-alt" },
                { label: "tutkinnonosat", link: ["root.esitys.peruste.tutkinnonosat"], isActive: isTutkinnonosatActive }
            ],
            header: "perusteen-sisalto"
        };

        function mapSisalto(sisalto) {
            sisalto = _.clone(sisalto);
            var flattened = {};
            Algoritmit.kaikilleLapsisolmuille(sisalto, "lapset", function(lapsi, depth) {
                flattened[lapsi.id] = _.clone(lapsi.perusteenOsa);
                $scope.navi.items.push({
                    label: lapsi.perusteenOsa.nimi,
                    link:
                        lapsi.perusteenOsa.tunniste === "rakenne"
                            ? ["root.esitys.peruste.rakenne", { suoritustapa: $stateParams.suoritustapa }]
                            : ["root.esitys.peruste.tekstikappale", { osanId: "" + lapsi.id }],
                    depth: depth
                });
            });
            return flattened;
        }
        $scope.kaanna = function(val) {
            return Kaanna.kaanna(val);
        };

        $scope.peruste = peruste;
        Kieli.setAvailableSisaltokielet($scope.peruste.kielet);
        $scope.$on("$destroy", function() {
            Kieli.resetSisaltokielet();
        });
        $scope.backLink = $state.href(YleinenData.koulutustyyppiInfo[$scope.peruste.koulutustyyppi].hakuState);
        $scope.sisalto = mapSisalto(sisalto);

        $scope.arviointiasteikot = _.zipObject(
            _.map(arviointiasteikot, "id"),
            _.map(arviointiasteikot, function(asteikko) {
                return _.zipObject(_.map(asteikko.osaamistasot, "id"), asteikko.osaamistasot);
            })
        );
        $scope.tutkinnonOsat = _(tutkinnonOsat)
            .sortBy(function(r) {
                return Kaanna.kaanna(r.nimi);
            })
            .value();

        $scope.valittu = {};
        $scope.suoritustavat = _.map(peruste.suoritustavat, "suoritustapakoodi");
        $scope.suoritustapa = $stateParams.suoritustapa;

        $scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko(peruste, $scope.suoritustapa);

        $scope.vaihdaSuoritustapa = function(suoritustapa) {
            $state.go("root.esitys.peruste", _.merge(_.clone($stateParams), { suoritustapa: suoritustapa }), {
                reload: true
            });
        };

        if ($state.current.name === "root.esitys.peruste") {
            var params = _.extend(_.clone($stateParams), {
                // TODO siirry käyttämään YleinenData.koulutustyyppiInfo:a
                suoritustapa: YleinenData.validSuoritustapa($scope.peruste, $stateParams.suoritustapa)
            });
            $state.go("root.esitys.peruste.tiedot", params, { location: "replace" });
        }

        $scope.rajaaSisaltoa = function() {
            _.forEach($scope.sisaltoRakenne, function(r) {
                r.$rejected = _.isEmpty($scope.rajaus)
                    ? false
                    : !Algoritmit.match($scope.rajaus, $scope.sisalto[r.id].nimi);
                if (!r.$rejected) {
                    var parent = $scope.sisaltoRakenneMap[r.parent];
                    while (parent) {
                        parent.$rejected = false;
                        parent = $scope.sisaltoRakenneMap[parent.parent];
                    }
                }
            });
            $scope.extra.tutkinnonOsat = !Algoritmit.match($scope.rajaus, Kaanna.kaanna("tutkinnonosat"));
            $scope.extra.tutkinnonRakenne = !Algoritmit.match($scope.rajaus, Kaanna.kaanna("tutkinnon-rakenne"));
        };

        $scope.luoPdf = function() {
            PdfCreation.setPerusteId($scope.peruste.id);
            PdfCreation.openModal($scope.isOpas, $scope.isAmmatillinen);
        };
    })
    .directive("esitysSivuOtsikko", function($compile) {
        var TEMPLATE =
            '<div class="painikkeet pull-right">' +
            '<a class="action-link" ng-click="asetaSuosikki()">' +
            "<span class=\"glyphicon\" ng-class=\"{'glyphicon-star': onSuosikki, 'glyphicon-star-empty': !onSuosikki}\"></span>" +
            "{{ onSuosikki ? 'poista-suosikeista' : 'merkitse-suosikiksi' | kaanna }}" +
            "</a>" +
            '<a class="action-link left-space" ng-click="printSisalto()" icon-role="print" kaanna="\'tulosta-sivu\'"></a>' +
            "</div>";
        return {
            restrict: "A",
            link: function(scope, element: any) {
                var compiled = $compile(TEMPLATE)(scope);
                element.append(compiled);
            },
            scope: {
                nimi: "=esitysSivuOtsikko"
            },
            controller: function($scope, $state, Profiili, Kaanna, $stateParams) {
                $scope.onSuosikki = Profiili.haeSuosikki($state);
                $scope.asetaSuosikki = function() {
                    var suosikkiOtsikko =
                        Kaanna.kaanna($scope.$parent.peruste.nimi) + ": " + (Kaanna.kaanna($scope.nimi) || "");
                    if ($scope.$parent.suoritustapa) {
                        suosikkiOtsikko += " (" + Kaanna.kaanna($scope.$parent.suoritustapa) + ")";
                    }
                    if (_.has($stateParams, "sisalto") && _.has($stateParams, "vlk")) {
                        suosikkiOtsikko += " (" + Kaanna.kaanna("opetuksen-sisallot") + ")";
                    }
                    Profiili.asetaSuosikki($state, suosikkiOtsikko, function() {
                        $scope.onSuosikki = Profiili.haeSuosikki($state);
                    });
                };

                $scope.printSisalto = function() {
                    var print = window.open("", "esitysPrintSisalto", "height=640,width=640");
                    print.document.write(
                        '<html><head><link rel="stylesheet" href="styles/eperusteet.css"></head><body class="esitys-print-view">' +
                            $("#esitysPrintSisalto").html() +
                            "</body></html>"
                    );
                    print.print();
                    print.close();
                };
            }
        };
    })
    .service("MurupolkuData", function() {
        // dummy service for eperusteet-esitys
        this.get = angular.noop;
        this.set = angular.noop;
        this.setTitle = angular.noop;
    });

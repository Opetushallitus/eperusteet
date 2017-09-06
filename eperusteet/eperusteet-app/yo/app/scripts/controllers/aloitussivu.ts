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
/* global _ */

angular
    .module("eperusteApp")
    .config(function($stateProvider) {
        $stateProvider
            .state("root", {
                url: "/:lang",
                template: "<div ui-view></div>",
                abstract: true,
                resolve: {
                    resolved: function(Profiili) {
                        return Profiili.resolvedPromise();
                    },
                    suosikit: function(Profiili) {
                        return Profiili;
                    },
                    casTiedot: function(Profiili) {
                        return Profiili.casTiedot();
                    }
                },
                onEnter: [
                    "YleinenData",
                    "$stateParams",
                    function(YleinenData, $stateParams) {
                        YleinenData.vaihdaKieli($stateParams.lang);
                    }
                ]
            })
            .state("root.aloitussivu", {
                url: "",
                templateUrl: "views/aloitussivu.html",
                controller: "AloitusSivuController"
            });
    })
    .controller("AloitusSivuController", function($scope, $state, YleinenData) {
        $scope.valinnat = [
            {
                koodi: "koulutustyyppi_15",
                helper: "selaa-perustetta"
            },
            {
                koodi: "koulutustyyppi_16",
                helper: "selaa-perusteita"
            },
            {
                koodi: "koulutustyyppi_17",
                helper: "selaa-perustetta"
            },
            {
                koodi: "koulutustyyppi_2",
                helper: "selaa-perusteita"
            },
            {
                label: "ammatillinen-peruskoulutus",
                koodi: "koulutustyyppi_1",
                helper: "hae-perusteita"
            },
            {
                label: "ammatillinen-aikuiskoulutus",
                koodi: "koulutustyyppi_11",
                helper: "hae-perusteita"
            }
        ];
        _.each($scope.valinnat, item => {
            const info = YleinenData.koulutustyyppiInfo[item.koodi] || {};
            item.label = item.label || info.nimi;
            item.url = $state.href(info.hakuState);
        });
    });

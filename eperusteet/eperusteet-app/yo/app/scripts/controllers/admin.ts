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

angular.module('eperusteApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('root.admin', {
                url: '/admin',
                templateUrl: 'views/admin/base.html',
                controller: 'AdminBaseController',
            })
            .state('root.admin.perusteprojektit', {
                url: '/perusteprojektit',
                templateUrl: 'views/admin/perusteprojektit.html',
                controller: 'AdminPerusteprojektitController'
            })
            .state('root.admin.tiedotteet', {
                url: '/tiedotteet',
                templateUrl: 'views/admin/tiedotteet.html',
                controller: 'TiedotteidenHallintaController'
            });
    })

    .controller('AdminBaseController', function ($scope, $state) {
        $scope.tabs = [
            {label: 'perusteprojektit', state: 'root.admin.perusteprojektit'},
            {label: 'tiedotteet', state: 'root.admin.tiedotteet'}
        ];

        $scope.chooseTab = function ($index) {
            _.each($scope.tabs, function (item, index) {
                item.$tabActive = index === $index;
            });
            var state = $scope.tabs[$index];
            if (state) {
                $state.go(state.state);
            }
        };

        if ($state.current.name === 'root.admin') {
            $scope.chooseTab(0);
        } else {
            _.each($scope.tabs, function (item) {
                item.$tabActive = item.state === $state.current.name;
            });
        }
    })

    .controller('AdminPerusteprojektitController', function ($rootScope, $scope, Api,
        Algoritmit, PerusteprojektiTila, Notifikaatiot, Kaanna, YleinenData, Varmistusdialogi,
        PerusteProjektiService, Utils) {
        $scope.jarjestysTapa = 'nimi';
        $scope.jarjestysOrder = false;
        $scope.tilaRajain = null;
        $scope.filteredPp = [];
        $scope.itemsPerPage = 10;
        $scope.nykyinen = 1;
        $scope.alaraja = 0;
        $scope.ylaraja = $scope.alaraja + $scope.itemsPerPage;
        $scope.rajaus = "";
        $scope.tilat = [
            "poistettu",
            "laadinta",
            "kommentointi",
            "viimeistely",
            "valmis",
            "julkaistu"
        ];

        async function updateSearch() {
            const perusteprojektit = await Api.one("perusteprojektit/perusteHaku").get({
                nimi: $scope.rajaus,
                tila: $scope.tilaRajain && $scope.tilaRajain.toUpperCase(),
                sivu: $scope.nykyinen - 1,
                sivukoko: $scope.itemsPerPage,
            });
            $scope.perusteprojektit = _.map(perusteprojektit.data, (pp) => {
                return {
                    ...pp,
                    suoritustapa: YleinenData.valitseSuoritustapaKoulutustyypille(pp.koulutustyyppi),
                    $$url: PerusteProjektiService.getUrl(pp),
                };
            });
            $scope.nykyinen = perusteprojektit.sivu + 1;
            $scope.kokonaismaara = perusteprojektit.kokonaismäärä;
        }
        updateSearch();

        const dUpdateSearch = _.debounce(updateSearch, 300);

        $scope.$watch("rajaus", updateSearch);
        $scope.$watch("tilaRajain", updateSearch);

        $scope.valitseSivu = (sivu) => {
            $scope.nykyinen = sivu;
            updateSearch();
        };

        $scope.palauta = (pp) => {
            const uusiTila = 'laadinta';
            Varmistusdialogi.dialogi({
                otsikko: Kaanna.kaanna('vahvista-palautus'),
                teksti: Kaanna.kaanna('vahvista-palautus-sisältö', {
                    nimi: pp.nimi,
                    tila: Kaanna.kaanna('tila-' + uusiTila)
                })
            })(() => {
                PerusteprojektiTila.save({id: pp.id, tila: uusiTila}, {}, (vastaus) => {
                    if (vastaus.vaihtoOk) {
                        pp.tila = uusiTila;
                    }
                    else {
                        Notifikaatiot.varoitus('tilan-vaihto-epaonnistui');
                    }
                }, Notifikaatiot.serverCb);
            });
        };
    });

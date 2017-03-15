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

angular.module("eperusteApp")
.config($stateProvider => $stateProvider
.state("root.aipeperusteprojekti", {
    abstract: true,
    url: "/aipeperusteprojekti/:perusteProjektiId",
    resolve: {
        perusteprojektiOikeudet: PerusteprojektiOikeudetService => PerusteprojektiOikeudetService,
        perusteprojektiOikeudetNouto: (perusteprojektiOikeudet, $stateParams) => perusteprojektiOikeudet.noudaOikeudet($stateParams),
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (Api, $stateParams) => Api.one("perusteprojektit", $stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste) => peruste.one("aipeopetus")
    },
    templateUrl: 'states/aipeperusteprojekti/view.html',
    controller: ($scope, $state, $stateParams, TiedoteService, PdfCreation, Kieli, perusteprojekti, peruste,
                 PerusteprojektiOikeudetService) => {
        $scope.projekti = perusteprojekti;
        $scope.peruste = peruste;
        $scope.luoPdf = () => {
            PdfCreation.setPerusteId($scope.projekti._peruste);
            PdfCreation.openModal();
        };
        $scope.showBackLink = () => !$state.is('root.aipeperusteprojekti.suoritustapa.sisalto');
        $scope.lisaaTiedote = () => {
            TiedoteService.lisaaTiedote(null, $stateParams.perusteProjektiId);
        };
        $scope.canChangePerusteprojektiStatus = () => {
            return PerusteprojektiOikeudetService.onkoOikeudet('perusteprojekti', 'tilanvaihto');
        };
        $scope.$on('enableEditing', () => {
            $scope.muokkausEnabled = true;
        });
        $scope.$on('disableEditing', () => {
            $scope.muokkausEnabled = false;
        });
    }
}));

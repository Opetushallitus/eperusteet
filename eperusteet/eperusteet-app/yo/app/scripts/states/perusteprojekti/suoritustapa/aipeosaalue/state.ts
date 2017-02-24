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

'use strict';

angular.module('eperusteApp')
.config($stateProvider => {
$stateProvider
.state('root.perusteprojekti.suoritustapa.aipeosaalue', {
    url: '/aipeosat/:osanTyyppi/:osanId',
    templateUrl: 'scripts/states/perusteprojekti/suoritustapa/aipeosaalue/view.html',
    resolve: {
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (perusteprojektit, $stateParams) => perusteprojektit.one($stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
        vaiheet: (aipeopetus) => aipeopetus.all("vaiheet").getList(),
        laajaalaiset: (aipeopetus) => aipeopetus.all("laajaalaiset").getList(),
        laajaalainen: (laajaalaiset, $stateParams, Api) => $stateParams.osanId === 'uusi'
            ? Api.restangularizeElement(laajaalaiset, {}, '')
            : laajaalaiset.get($stateParams.osanId)
    },
    controller: ($scope, $q, $stateParams, laajaalaiset, laajaalainen, Editointikontrollit, Notifikaatiot,
                 YleinenData, ProjektinMurupolkuService) => {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.isNew = $stateParams.osanId === 'uusi';
        $scope.isOsaaminen = true;
        $scope.versiot = {
            latest: true
        };
        $scope.laajaalainen = laajaalainen;
        $scope.dataObject = laajaalainen;
        $scope.edit = () => {
            Editointikontrollit.startEditing();
        };

        ProjektinMurupolkuService.set('osanTyyppi', $stateParams.osanTyyppi, 'osaaminen');
        ProjektinMurupolkuService.set('osanId', $stateParams.osanId, $scope.laajaalainen.nimi);
    },
    onEnter: PerusteProjektiSivunavi => {
        PerusteProjektiSivunavi.setVisible();
    }
})});

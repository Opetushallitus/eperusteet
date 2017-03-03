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
.config($stateProvider => $stateProvider
.state('root.aipeperusteprojekti.suoritustapa.osaalue', {
    url: '/osalistaus/:osanTyyppi/osaalue/:osanId',
    templateUrl: 'states/aipeperusteprojekti/suoritustapa/osaalue/view.html',
    resolve: {
        laajaalainen: (laajaalaiset, $stateParams, Api) => $stateParams.osanId === 'uusi'
            ? Api.restangularizeElement(laajaalaiset, {}, '')
            : laajaalaiset.get($stateParams.osanId)
    },
    controller: ($scope, $q, $stateParams, laajaalaiset, laajaalainen, vaiheet, Editointikontrollit, Notifikaatiot,
                 YleinenData, ProjektinMurupolkuService) => {
        $scope.isNew = $stateParams.osanId === 'uusi';
        $scope.isVaihe = $stateParams.osanTyyppi === AIPEService.VAIHEET;
        $scope.isOppiaine = $stateParams.osanTyyppi === AIPEService.OPPIAINEET;
        $scope.isOsaaminen = $stateParams.osanTyyppi === AIPEService.OSAAMINEN;
        $scope.versiot = {
            latest: true
        };
        const getOsa = () => {
            switch ($stateParams.osanTyyppi) {
                case AIPEService.VAIHEET:
                    return vaiheet;
                case AIPEService.OSAAMINEN:
                    return laajaalainen;
                default:
                    throw "osan tyyppi viallinen";
            }
        };
        $scope.dataObject = getOsa();

        $scope.edit = () => {
            Editointikontrollit.startEditing();
        };

        const labels = _.invert(AIPEService.LABELS);
        ProjektinMurupolkuService.set('osanTyyppi', $stateParams.osanTyyppi, labels[$stateParams.osanTyyppi]);
        ProjektinMurupolkuService.set('osanId', $stateParams.osanId, $scope.dataObject.nimi);
    }
}));

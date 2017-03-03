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
.state('root.aipeperusteprojekti.suoritustapa.osalistaus', {
    url: '/osalistaus/:osanTyyppi',
    templateUrl: 'states/aipeperusteprojekti/suoritustapa/osalistaus/view.html',
    controller: ($scope, $state, $stateParams, virheService, laajaalaiset) => {
        $scope.osaaminenSisalto = _.find(AIPEService.SISALLOT, {
            tyyppi: 'osaaminen'
        });

        $scope.laajaalaiset = laajaalaiset;

        $scope.options = {
            extrafilter: null
        };

        $scope.createUrl = value => $state.href('root.aipeperusteprojekti.suoritustapa.osaalue', {
            suoritustapa: $stateParams.suoritustapa,
            osanTyyppi: $stateParams.osanTyyppi,
            osanId: value.id,
            tabId: 0
        });

        $scope.add = () => {
            $state.go('root.aipeperusteprojekti.suoritustapa.laajaalainen', {
                osanId: 'uusi'
            });
        };
    }
}));

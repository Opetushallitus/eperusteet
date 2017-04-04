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
.controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService, YleinenData) {
    PerusteProjektiService.watcher($scope, 'projekti');

    if (typeof $scope.projekti.paatosPvm === 'number') {
        $scope.projekti.paatosPvm = new Date($scope.projekti.paatosPvm);
    }

    $scope.yksikot = YleinenData.yksikot;

    $scope.tehtavaluokat = [
        'Tehtäväluokka-1',
        'Tehtäväluokka-2',
        'Tehtäväluokka-3',
        'Tehtäväluokka-4'
    ];

    $scope.koulutustyypit = YleinenData.koulutustyypit;

    $scope.reforminMukainen = YleinenData.isReformoitava;

    $scope.tarvitseeLaajuuden = (koulutustyyppi) => koulutustyyppi
        && _.indexOf(YleinenData.laajuudellisetKoulutustyypit, koulutustyyppi) !== -1;
});


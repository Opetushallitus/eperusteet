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
  .controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService, YleinenData) {
  PerusteProjektiService.watcher($scope, 'projekti');

  if (typeof $scope.projekti.paatosPvm === 'number') {
     $scope.projekti.paatosPvm = new Date($scope.projekti.paatosPvm);
  }
  
  $scope.tehtavaluokat = [
    'Tehtäväluokka-1',
    'Tehtäväluokka-2',
    'Tehtäväluokka-3',
    'Tehtäväluokka-4'
  ];

  $scope.koulutustyypit = YleinenData.koulutustyypit;

  $scope.paatosPvmOpen = false;

  $scope.showWeeks = true;

  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    $scope.paatosPvmOpen = !$scope.paatosPvmOpen;
  };

  $scope.dateOptions = {
    'year-format': 'yy',
    //'month-format': 'M',
    //'day-format': 'd',
    'starting-day': 1
  };

  $scope.format = YleinenData.dateFormatDatepicker;
});


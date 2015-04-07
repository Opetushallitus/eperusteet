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

 angular.module('eperusteet.esitys')
.controller('epEsitysSisaltoController', function($scope, $state, $stateParams, PerusteenOsat, YleinenData,
  /*MurupolkuData,*/ epParentFinder, epTekstikappaleChildResolver) {
  $scope.linkVar = $stateParams.osanId ? 'osanId' : 'tekstikappaleId';
  //$scope.$parent.valittu.sisalto = $stateParams[$scope.linkVar];
  $scope.valittuSisalto = $scope.$parent.sisalto[$stateParams[$scope.linkVar]];
  $scope.tekstikappale = $scope.valittuSisalto;
  $scope.lapset = epTekstikappaleChildResolver.getSisalto();
  /*MurupolkuData.set({
    osanId: $scope.valittuSisalto.id,
    tekstikappaleNimi: $scope.valittuSisalto.nimi,
    parents: ParentFinder.find($scope.$parent.originalSisalto.lapset, parseInt($stateParams.osanId, 10))
  });*/
  if (!$scope.valittuSisalto) {
    var params = _.extend(_.clone($stateParams), {
      suoritustapa: YleinenData.validSuoritustapa($scope.peruste, $stateParams.suoritustapa)
    });
    $state.go('root.esitys.peruste.tiedot', params);
  } else {
    PerusteenOsat.get({ osanId: $scope.valittuSisalto.id }, function (res) {
      $scope.valittuSisalto = res;
      $scope.tekstikappale = res;
    });
  }
});

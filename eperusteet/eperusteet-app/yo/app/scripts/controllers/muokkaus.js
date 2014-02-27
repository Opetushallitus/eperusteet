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
  .config(function($routeProvider) {
    $routeProvider
      .when('/muokkaus/:perusteenOsanTyyppi/:id?', {
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        navigaationimi: 'PerusteenOsanMuokkaus'
      });
  })
  .controller('MuokkausCtrl', function($scope, $routeParams, PerusteenOsat, $location, $compile) {
    console.log('setup muokkaus');
    $scope.tyyppi = $routeParams.perusteenOsanTyyppi;
    
    if($routeParams.id) {
      $scope.objekti = PerusteenOsat.get({osanId: $routeParams.id}, function(){}, function() {
        console.log('unable to find perusteen osa #' + $routeParams.id);
        $location.path('/selaus/ammatillinenperuskoulutus');
      });
    }
    
    var muokkausDirective = null;
    if($routeParams.perusteenOsanTyyppi === 'tekstikappale') {
      muokkausDirective = angular.element('<muokkaus-tekstikappale tekstikappale="objekti"></muokkaus-tekstikappale>');
    } else if($routeParams.perusteenOsanTyyppi === 'tutkinnonosa') {
      muokkausDirective = angular.element('<muokkaus-tutkinnonosa tutkinnon-osa="objekti"></muokkaus-tutkinnonosa>');
    }
    var el = $compile(muokkausDirective)($scope);
    
    angular.element('.muokkaus-elementti-placeholder').replaceWith(el);
  });

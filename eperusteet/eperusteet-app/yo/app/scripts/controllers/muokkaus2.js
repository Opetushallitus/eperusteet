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
      .when('/muokkaus2/:perusteenOsanTyyppi/:id?', {
        templateUrl: 'views/muokkaus2.html',
        controller: 'Muokkaus2Ctrl',
        navigaationimi: 'PerusteenOsanMuokkaus'
      });
  })
  .controller('Muokkaus2Ctrl', function($scope, $routeParams, PerusteenOsat, $location) {
    console.log('setup muokkaus2');
    $scope.tyyppi = $routeParams.perusteenOsanTyyppi;
    
    if($routeParams.id) {
      $scope.objekti = PerusteenOsat.get({osanId: $routeParams.id}, function(response){}, function(error) {
        console.log('unable to find perusteen ossa #' + $routeParams.id);
        $location.path('/selaus/ammatillinenperuskoulutus');
      });
    }
//    else {
//      // TODO: palvelulta pyydetään haluttu template
//      if($scope.tyyppi === 'tekstikappale') {
//        $scope.objekti = {
//          nimi: {
//            fi: '',
//            sv: ''
//          },
//          teksti: {
//            fi: '',
//            sv: ''
//          }
//        };
//      } else {

//      }
//    }
//    this.tallennaPerusteenosa = function(perusteenOsa) {
//      console.log('Tallennetaan');
//      console.log(perusteenOsa);
//      if(!perusteenOsa.id) {
//        PerusteenOsat.saveTutkinnonOsa(perusteenOsa);
//        
//        $location.path('/muokkaus/' + $scope.tyyppi + '/' + perusteenOsa.id);
//      } else {
//        if($scope.tyyppi === 'tekstikappale') {
//          
//        } else {
//          perusteenOsa.saveTutkinnonOsa();
//        }
//      }
//    };
  });

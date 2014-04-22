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
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojektiSisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        naviRest: ['sisältö']
      });
  })
  .controller('PerusteprojektisisaltoCtrl', function($scope, $stateParams, PerusteprojektiResource,
    Suoritustapa, SuoritustapaSisalto) {
     $scope.projekti = {};

    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({id: $stateParams.perusteProjektiId}, function(vastaus) {
        $scope.projekti = vastaus;
        if ($scope.projekti.peruste.id) {
          haeSisalto('ops');
        }
      }, function(virhe) {
        console.log('virhe', virhe);
      });
      
    } else {
      $scope.projekti.id = 'uusi';
      //Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }
    
    var haeSisalto = function(suoritustapa) {
      Suoritustapa.get({perusteenId: $scope.projekti.peruste.id, suoritustapa: suoritustapa}, function(vastaus) {
        $scope.projekti.peruste.sisalto = vastaus;
      }, function(virhe) {
        console.log('suoritustapasisältöä ei löytynyt', virhe);
      });
    };

    $scope.createSisalto = function () {
      SuoritustapaSisalto.save({perusteId: $scope.projekti.peruste.id, suoritustapa: 'ops'}, function(vastaus) {
        haeSisalto('ops');
        console.log('uusi suoritustapa sisältö', vastaus);
      });
    };
  });

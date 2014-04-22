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
.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/perusteprojekti', '/perusteprojekti/uusi/sisalto');
    $urlRouterProvider.when('/perusteprojekti/', '/perusteprojekti/uusi/sisalto');
    $stateProvider
      .state('perusteprojekti', {
        url: '/perusteprojekti/:perusteProjektiId',
        navigaationimi: 'navi-perusteprojekti',
        template: '<div ui-view></div>',
        resolve: {'koulutusalaService': 'Koulutusalat'},
        abstract: true
      })
      .state('perusteprojekti.editoi', {
        url: '/muokkaa',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        abstract: true
      });
    })
  .controller('PerusteprojektiCtrl', function ($scope, $stateParams, Navigaatiopolku,
    PerusteprojektiResource, koulutusalaService) {
    $scope.projekti = {};
    
    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        PerusteProjektiService.save(vastaus);
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });
      }, function(virhe) {
        console.log('virhe', virhe);
      });
    } else {
      $scope.projekti.id = 'uusi';
      Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

  });

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
        url: '/perusteprojekti/:perusteProjektiId/:suoritustapa',
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
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'},
        abstract: true
      });
    })
  .controller('PerusteprojektiCtrl', function ($scope, $rootScope, $stateParams, Navigaatiopolku,
    PerusteprojektiResource, koulutusalaService, opintoalaService, Perusteet, SivunavigaatioService,
    PerusteProjektiService, Kaanna) {
    PerusteProjektiService.cleanSuoritustapa();
    $scope.projekti = {};
    $scope.peruste = {};

    $scope.testi = '';

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;

    PerusteProjektiService.setSuoritustapa($stateParams.suoritustapa);

    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        // TODO: väliaikaisesti hardkoodattu tila
        $scope.projekti.tila = 'luonnos';
        SivunavigaatioService.asetaProjekti($scope.projekti);
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });

        Perusteet.get({perusteenId: vastaus._peruste}, function(vastaus) {
          $scope.peruste = vastaus;
        }, function(virhe) {
          console.log('perusteen haku virhe', virhe);
        });

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

    $scope.canChangePerusteprojektiStatus = function () {
      // TODO vain omistaja voi vaihtaa tilaa
      return true;
    };

    $scope.perusteenNimi = function() {
      if (Kaanna.kaanna($scope.peruste.nimi) === '') {
        return null;
      } else {
        return $scope.peruste.nimi;
      }
    };
  });

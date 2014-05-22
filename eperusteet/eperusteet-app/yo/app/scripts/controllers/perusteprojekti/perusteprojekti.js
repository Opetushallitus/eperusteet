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
    $stateProvider
      .state('perusteprojekti', {
        url: '/perusteprojekti/:perusteProjektiId',
        navigaationimi: 'navi-perusteprojekti',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'},
        abstract: true
      })
      .state('perusteprojekti.suoritustapa', {
        url: '/:suoritustapa',
        template: '<div ui-view></div>',
        navigaationimi: 'navi-perusteprojekti',
        abstract: true
      })
      .state('perusteprojekti.suoritustapa.muodostumissaannot', {
        url: '/tutkinnonrakenne',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiMuodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        naviRest: ['muodostumissaannot'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: true});
          }]
      })
      .state('perusteprojekti.suoritustapa.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        naviRest: ['tutkinnonosat'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: true});
          }]
      })
      .state('perusteprojekti.suoritustapa.perusteenosa', {
        url: '/perusteenosa/:perusteenOsanTyyppi/:perusteenOsaId',
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        naviRest: [':perusteenOsanTyyppi'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: true});
          }]
      })
      .state('perusteprojekti.suoritustapa.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiSisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({piilota: true});
          }]
      })
      .state('perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedot.html',
        controller: 'ProjektinTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojekti.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiPeruste.html',
        controller: 'PerusteenTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiProjektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojektiwizard', {
        url: '/perusteprojekti',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedotUusi.html',
        controller: 'ProjektinTiedotCtrl',
        abstract: true
      })
      .state('perusteprojektiwizard.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedot.html',
        controller: 'ProjektinTiedotCtrl',
        naviBase: ['uusi-perusteprojekti'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      });
  })
  .controller('PerusteprojektiCtrl', function ($scope, $stateParams, Navigaatiopolku,
    PerusteprojektiResource, koulutusalaService, opintoalaService, Perusteet, SivunavigaatioService,
    PerusteProjektiService, Kaanna) {
      
    PerusteProjektiService.cleanSuoritustapa();
    $scope.projekti = {};
    $scope.peruste = {};

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;

    PerusteProjektiService.setSuoritustapa($stateParams.suoritustapa);

    $scope.projekti.id = $stateParams.perusteProjektiId;
    PerusteprojektiResource.get({id: $stateParams.perusteProjektiId}, function(vastaus) {
      $scope.projekti = vastaus;
      // TODO: väliaikaisesti hardkoodattu tila
      $scope.projekti.tila = 'luonnos';
      SivunavigaatioService.asetaProjekti($scope.projekti);
      Navigaatiopolku.asetaElementit({perusteProjektiId: vastaus.nimi});

      Perusteet.get({perusteenId: vastaus._peruste}, function(vastaus) {
        $scope.peruste = vastaus;
      }, function(virhe) {
        console.log('perusteen haku virhe', virhe);
      });

    }, function(virhe) {
      console.log('virhe', virhe);
    });

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

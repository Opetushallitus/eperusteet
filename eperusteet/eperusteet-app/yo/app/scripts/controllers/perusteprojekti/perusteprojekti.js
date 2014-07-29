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
// /* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('root.perusteprojekti', {
        url: '/perusteprojekti/:perusteProjektiId',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        resolve: {
          'koulutusalaService': 'Koulutusalat',
          'opintoalaService': 'Opintoalat',
          'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'perusteprojektiAlustus': ['perusteprojektiTiedot', '$stateParams', function(perusteprojektiTiedot, $stateParams) {
              return perusteprojektiTiedot.alustaProjektinTiedot($stateParams);
            }]
        },
        abstract: true
      })
      .state('root.perusteprojekti.suoritustapa', {
        url: '/:suoritustapa',
        template: '<div ui-view></div>',
        navigaationimi: 'navi-perusteprojekti',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
              return perusteprojektiTiedot.projektinTiedotAlustettu();
            }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
            }]
        },
        abstract: true
      })
      .state('root.perusteprojekti.suoritustapa.muodostumissaannot', {
        url: '/rakenne{versio:(?:/[^/]+)?}',
        templateUrl: 'views/partials/perusteprojekti/muodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        onEnter: ['SivunavigaatioService', 'perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({osiot: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('root.perusteprojekti.suoritustapa.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/perusteprojekti/tutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        onEnter: ['SivunavigaatioService', 'perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({osiot: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('root.perusteprojekti.suoritustapa.perusteenosa', {
        url: '/perusteenosa/{perusteenOsanTyyppi}/{perusteenOsaId}{versio:(?:/[^/]+)?}',
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: true});
          }]
      })
      .state('root.perusteprojekti.suoritustapa.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojekti/sisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        onEnter: ['SivunavigaatioService', 'perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({piilota: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('root.perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('root.perusteprojekti.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojekti/peruste.html',
        controller: 'PerusteenTiedotCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('root.perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/projektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('root.perusteprojektiwizard', {
        url: '/perusteprojekti',
        templateUrl: 'views/partials/perusteprojekti/tiedotUusi.html',
        abstract: true
      })
      .state('root.perusteprojektiwizard.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      });
  })
  .controller('PerusteprojektiCtrl', function($scope, $state, $stateParams,
    Navigaatiopolku, koulutusalaService, opintoalaService, SivunavigaatioService,
    PerusteProjektiService, Kaanna, perusteprojektiTiedot) {

    function init() {
      $scope.projekti = perusteprojektiTiedot.getProjekti();
      $scope.peruste = perusteprojektiTiedot.getPeruste();
      // TODO poista kun tilasiirtymät tuettuina
      //$scope.projekti.tila = 'luonnos';
    }
    init();
    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;

    Navigaatiopolku.asetaElementit({
      perusteprojekti: {
        nimi: $scope.projekti.nimi,
        url: 'root.perusteprojekti.suoritustapa.sisalto'
      }
    });

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

    $scope.canChangePerusteprojektiStatus = function() {
      // TODO vain omistaja voi vaihtaa tilaa
      return true;
    };

    $scope.$on('update:perusteprojekti', function () {
      perusteprojektiTiedot.alustaProjektinTiedot($stateParams).then(function () {
        init();
      });
    });
  });

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
      .state('perusteprojekti', {
        url: '/perusteprojekti/:perusteProjektiId',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat',
                  'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
                  'perusteprojektiAlustus': function(perusteprojektiTiedot, $stateParams) {
                    return perusteprojektiTiedot.alustaProjektinTiedot($stateParams);
                  }
                },
        abstract: true
      })
      .state('perusteprojekti.suoritustapa', {
        url: '/:suoritustapa',
        template: '<div ui-view></div>',
        navigaationimi: 'navi-perusteprojekti',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
                  'projektinTiedotAlustettu': function(perusteprojektiTiedot) {
                    return perusteprojektiTiedot.projektinTiedotAlustettu();
                  },
                  'perusteenSisaltoAlustus': function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
                    return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
                  }
        },
        abstract: true
      })
      .state('perusteprojekti.suoritustapa.muodostumissaannot', {
        url: '/tutkinnonrakenne',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiMuodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        onEnter: ['SivunavigaatioService', 'perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({osiot: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('perusteprojekti.suoritustapa.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        onEnter: ['SivunavigaatioService', 'perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({osiot: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('perusteprojekti.suoritustapa.perusteenosa', {
        url: '/perusteenosa/:perusteenOsanTyyppi/:perusteenOsaId',
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        onEnter: ['SivunavigaatioService', 'Kommentit', 'KommentitByPerusteenOsa', '$stateParams', function(SivunavigaatioService, Kommentit, KommentitByPerusteenOsa, $stateParams) {
            Kommentit.haeKommentit(KommentitByPerusteenOsa, { id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.perusteenOsaId });
            SivunavigaatioService.aseta({osiot: true});
          }]
      })
      .state('perusteprojekti.suoritustapa.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiSisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        onEnter: ['SivunavigaatioService','perusteprojektiTiedot', function(SivunavigaatioService, perusteprojektiTiedot) {
            SivunavigaatioService.aseta({piilota: true, perusteprojektiTiedot: perusteprojektiTiedot});
          }]
      })
      .state('perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedot.html',
        controller: 'ProjektinTiedotCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojekti.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiPeruste.html',
        controller: 'PerusteenTiedotCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiProjektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      })
      .state('perusteprojektiwizard', {
        url: '/perusteprojekti',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedotUusi.html',
        abstract: true
      })
      .state('perusteprojektiwizard.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/perusteprojektiTiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      });
  })
  .controller('PerusteprojektiCtrl', function ($scope, $state, $stateParams,
    Navigaatiopolku, koulutusalaService, opintoalaService, SivunavigaatioService,
    PerusteProjektiService, Kaanna, perusteprojektiTiedot) {

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;

    $scope.projekti.tila = 'luonnos';
    Navigaatiopolku.asetaElementit({
        perusteprojekti: {
          nimi: $scope.projekti.nimi,
          url: 'perusteprojekti.suoritustapa.sisalto'
        }
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

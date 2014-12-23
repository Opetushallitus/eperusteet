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
            }],
          'perusteprojektiOikeudet': 'PerusteprojektiOikeudetService',
          'perusteprojektiOikeudetNouto': ['perusteprojektiOikeudet', '$stateParams', function(perusteprojektiOikeudet, $stateParams) {
              perusteprojektiOikeudet.noudaOikeudet($stateParams);
          }]
        },
        abstract: true
      })
      .state('root.perusteprojekti.suoritustapa.osalistaus', {
        url: '/osat/:osanTyyppi',
        templateUrl: 'views/partials/perusteprojekti/osalistaus.html',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
            return perusteprojektiTiedot.projektinTiedotAlustettu();
          }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
            }]
        },
        controller: 'OsalistausController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.osaalue', {
        url: '/osat/:osanTyyppi/:osanId/:tabId',
        templateUrl: 'views/partials/perusteprojekti/osaalue.html',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService',
          'projektinTiedotAlustettu': ['perusteprojektiTiedot', function(perusteprojektiTiedot) {
            return perusteprojektiTiedot.projektinTiedotAlustettu();
          }],
          'perusteenSisaltoAlustus': ['perusteprojektiTiedot', 'projektinTiedotAlustettu', '$stateParams',
            function(perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) {
              return perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
            }]
        },
        controller: 'OsaAlueController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.muokkaus', {
        url: '/muokkaus/:osanTyyppi/:osanId',
        templateUrl: 'views/muokkaus.html',
        controller: 'OsanMuokkausController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
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
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/perusteprojekti/tutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.tutkinnonosa', {
        url: '/tutkinnonosa/{tutkinnonOsaViiteId}{versio:(?:/[^/]+)?}',
        templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
        controller: 'muokkausTutkinnonosaCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.tekstikappale', {
        url: '/tekstikappale/{perusteenOsaViiteId}{versio:(?:/[^/]+)?}',
        templateUrl: 'views/partials/muokkaus/tekstikappale.html',
        controller: 'muokkausTekstikappaleCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
        }]
      })
      .state('root.perusteprojekti.suoritustapa.tutkinnonosa.osaalue', {
        url: '/osaalue/{osaAlueId}',
        templateUrl: 'views/partials/muokkaus/tutkinnonOsaOsaAlue.html',
        controller: 'TutkinnonOsaOsaAlueCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.suoritustapa.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/partials/perusteprojekti/sisalto.html',
        controller: 'PerusteprojektisisaltoCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.suoritustapa.posisalto', {
        url: '/posisalto',
        templateUrl: 'views/partials/perusteprojekti/perusopetus.html',
        controller: 'PerusopetusSisaltoController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.suoritustapa.eosisalto', {
        url: '/eosisalto',
        templateUrl: 'views/partials/perusteprojekti/esiopetus.html',
        controller: 'EsiopetusSisaltoController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojekti/peruste.html',
        controller: 'PerusteenTiedotCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }],
        resolve: {
          valittavatKielet: function(Perusteet) {
            return Perusteet.valittavatKielet().$promise;
          }
        }
      })
      .state('root.perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/projektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojekti.termisto', {
        url: '/termisto',
        templateUrl: 'views/partials/perusteprojekti/termisto.html',
        controller: 'TermistoController',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible(false);
        }]
      })
      .state('root.perusteprojektiwizard', {
        url: '/perusteprojekti',
        template: '<div ui-view></div>',
        abstract: true
      })
      .state('root.perusteprojektiwizard.pohja', {
        url: '/perustepohja',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      })
      .state('root.perusteprojektiwizard.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojekti/tiedot.html',
        controller: 'ProjektinTiedotCtrl',
        resolve: {'perusteprojektiTiedot': 'PerusteprojektiTiedotService'}
      });
  })
  .controller('PerusteprojektiCtrl', function($scope, $state, $stateParams,
    koulutusalaService, opintoalaService, Navigaatiopolku,
    PerusteProjektiService, perusteprojektiTiedot, PerusteProjektiSivunavi, PdfCreation,
    SuoritustapaSisalto, Notifikaatiot, TutkinnonOsaEditMode, perusteprojektiOikeudet, TermistoService) {

    $scope.muokkausEnabled = false;

    $scope.luoPdf = function () {
      PdfCreation.setPerusteId($scope.projekti._peruste);
      PdfCreation.openModal();
    };

    function init() {
      $scope.projekti = perusteprojektiTiedot.getProjekti();
      $scope.peruste = perusteprojektiTiedot.getPeruste();
      $scope.backLink = PerusteProjektiService.getUrl($scope.projekti, $scope.peruste);
      TermistoService.setPeruste($scope.peruste);
    }
    init();

    // Generoi uudestaan "Projektin päänäkymä"-linkki kun suoritustapa vaihtuu
    $scope.$watch(function () {
      return PerusteProjektiService.getSuoritustapa();
    }, function () {
      $scope.backLink = PerusteProjektiService.getUrl($scope.projekti, $scope.peruste);
    });

    var amFooter = '<button class="btn btn-default" kaanna="lisaa-tutkintokohtainen-osa" icon-role="ep-text-add" ng-click="$parent.lisaaTekstikappale()" oikeustarkastelu="{ target: \'peruste\', permission: \'muokkaus\' }"></button>';
    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.sivunavi = {
      suoritustapa: PerusteProjektiService.getSuoritustapa(),
      items: [],
      footer: amFooter,
      type: 'AM'
    };
    var sivunaviItemsChanged = function (items) {
      $scope.sivunavi.items = items;
    };
    var sivunaviTypeChanged = function (type) {
      $scope.sivunavi.type = type;
      switch (type) {
        case 'YL':
          $scope.sivunavi.suoritustapa = '';
          $scope.sivunavi.footer = '';
          break;
        default:
          $scope.sivunavi.footer = amFooter;
          $scope.sivunavi.suoritustapa = PerusteProjektiService.getSuoritustapa();
          break;
      }
    };
    PerusteProjektiSivunavi.register('itemsChanged', sivunaviItemsChanged);
    PerusteProjektiSivunavi.register('typeChanged', sivunaviTypeChanged);
    PerusteProjektiSivunavi.refresh(true);

    $scope.$on('$stateChangeSuccess', function() {
      var newSuoritustapa = PerusteProjektiService.getSuoritustapa();
      if (newSuoritustapa !== $scope.sivunavi.suoritustapa) {
        PerusteProjektiSivunavi.refresh(true);
      }
      $scope.sivunavi.suoritustapa = $scope.sivunavi.type === 'AM' ? newSuoritustapa : '';
    });

    Navigaatiopolku.setProject($scope.projekti, $scope.peruste);

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

    $scope.canChangePerusteprojektiStatus = function() {
      return perusteprojektiOikeudet.onkoOikeudet('perusteprojekti', 'tilanvaihto');
    };

    $scope.showBackLink = function () {
      return !($state.is('root.perusteprojekti.suoritustapa.sisalto') ||
               $state.is('root.perusteprojekti.suoritustapa.posisalto') ||
               $state.is('root.perusteprojekti.suoritustapa.eosisalto'));
    };

    $scope.isNaviVisible = function () {
      return PerusteProjektiSivunavi.isVisible();
    };

    $scope.$on('update:perusteprojekti', function () {
      perusteprojektiTiedot.alustaProjektinTiedot($stateParams).then(function () {
        init();
        PerusteProjektiSivunavi.refresh(true);
      });
    });

    $scope.lisaaTekstikappale = function () {
      function lisaaSisalto(method, sisalto, cb) {
        cb = cb || angular.noop;
        SuoritustapaSisalto[method]({
          perusteId: $scope.projekti._peruste,
          suoritustapa: PerusteProjektiService.getSuoritustapa()
        }, sisalto, cb, Notifikaatiot.serverCb);
      }
      lisaaSisalto('save', {}, function(response) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        $state.go('root.perusteprojekti.suoritustapa.tekstikappale', {
          perusteenOsaViiteId: response.id,
          versio: ''
        });
      });
    };

    $scope.$on('enableEditing', function() {
      $scope.muokkausEnabled = true;
    });
    $scope.$on('disableEditing', function() {
      $scope.muokkausEnabled = false;
    });
  });

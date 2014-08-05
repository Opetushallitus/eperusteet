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
/* global _ */

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
      .state('root.perusteprojekti.suoritustapa.perusteenosa', {
        url: '/perusteenosa/{perusteenOsanTyyppi}/{perusteenOsaId}{versio:(?:/[^/]+)?}',
        templateUrl: 'views/muokkaus.html',
        controller: 'MuokkausCtrl',
        onEnter: ['PerusteProjektiSivunavi', function(PerusteProjektiSivunavi) {
          PerusteProjektiSivunavi.setVisible();
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
        }]
      })
      .state('root.perusteprojekti.projektiryhma', {
        url: '/projektiryhma',
        templateUrl: 'views/partials/perusteprojekti/projektiryhma.html',
        controller: 'ProjektiryhmaCtrl',
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
    Navigaatiopolku, koulutusalaService, opintoalaService, SivunavigaatioService,
    PerusteProjektiService, Kaanna, perusteprojektiTiedot, PerusteProjektiSivunavi) {

    function init() {
      $scope.projekti = perusteprojektiTiedot.getProjekti();
      $scope.peruste = perusteprojektiTiedot.getPeruste();
      // TODO poista kun tilasiirtymät tuettuina
      //$scope.projekti.tila = 'luonnos';
    }
    init();
    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.sivunavi = {
      suoritustapa: PerusteProjektiService.getSuoritustapa(),
      items: []
    };
    var sivunaviItemsChanged = function (items) {
      $scope.sivunavi.items = items;
    };
    PerusteProjektiSivunavi.register(sivunaviItemsChanged);
    PerusteProjektiSivunavi.refresh();

    $scope.$on('$stateChangeSuccess', function() {
      var newSuoritustapa = PerusteProjektiService.getSuoritustapa();
      if (newSuoritustapa !== $scope.sivunavi.suoritustapa) {
        PerusteProjektiSivunavi.refresh();
      }
      $scope.sivunavi.suoritustapa = newSuoritustapa;
    });

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

    $scope.showBackLink = function () {
      return !$state.is('root.perusteprojekti.suoritustapa.sisalto');
    };

    $scope.getBackLink = function () {
      return $state.href('root.perusteprojekti.suoritustapa.sisalto', {
        suoritustapa: PerusteProjektiService.getSuoritustapa() || 'naytto'
      });
    };

    $scope.isNaviVisible = function () {
      return PerusteProjektiSivunavi.isVisible();
    };

    $scope.$on('update:perusteprojekti', function () {
      perusteprojektiTiedot.alustaProjektinTiedot($stateParams).then(function () {
        init();
        PerusteProjektiSivunavi.refresh();
      });
    });
  })
  .service('PerusteProjektiSivunavi', function (PerusteprojektiTiedotService, $stateParams) {
    var service = null;
    var _isVisible = false;
    var items = [];
    var data = {
      projekti: {
        peruste: {
          sisalto: {
          }
        }
      }
    };
    var callbacks = {
      changed: angular.noop
    };

    var processNode = function (node, level) {
      level = level || 0;
      _.each(node.lapset, function (lapsi) {
        items.push({
          label: lapsi.perusteenOsa.nimi,
          id: lapsi.id,
          depth: level,
          link: [
            'root.perusteprojekti.suoritustapa.perusteenosa',
            {
              perusteenOsanTyyppi: 'tekstikappale',
              perusteenOsaId: lapsi.perusteenOsa.id,
              versio: null
            }
          ]
        });
        processNode(lapsi, level + 1);
      });
    };

    var buildTree = function () {
      items = [
        {label: 'tutkinnonosat', link: ['root.perusteprojekti.suoritustapa.tutkinnonosat', {}]},
        {label: 'tutkinnon-rakenne', link: ['root.perusteprojekti.suoritustapa.muodostumissaannot', {}]},
      ];
      processNode(data.projekti.peruste.sisalto);
      callbacks.changed(items);
    };

    var load = function () {
      data.projekti = service.getProjekti();
      data.projekti.peruste = service.getPeruste();
      data.projekti.peruste.sisalto = service.getSisalto();
      buildTree();
    };

    this.register = function (cb) {
      callbacks.changed = cb;
    };

    this.refresh = function () {
      if (!service) {
        PerusteprojektiTiedotService.then(function(res) {
          service = res;
          load();
        });
      } else {
        service.alustaPerusteenSisalto($stateParams, true).then(function () {
          load();
        });
      }
    };

    this.setVisible = function (visible) {
      _isVisible = _.isUndefined(visible) ? true : visible;
    };

    this.isVisible = function () {
      return _isVisible;
    };
  });

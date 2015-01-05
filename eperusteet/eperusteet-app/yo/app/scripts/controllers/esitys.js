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
/* global _,$ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('root.esitys', {
        url: '/esitys',
        template: '<div ui-view></div>'
      })
      .state('root.esitys.peruste', {
        url: '/:perusteId/:suoritustapa',
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        resolve: {
          peruste: function($stateParams, Perusteet) {
            return Perusteet.get({ perusteId: $stateParams.perusteId }).$promise;
          },
          sisalto: function($stateParams, SuoritustapaSisalto) {
            return SuoritustapaSisalto.get({ perusteId: $stateParams.perusteId, suoritustapa: $stateParams.suoritustapa }).$promise;
          },
          arviointiasteikot: function($stateParams, Arviointiasteikot) {
            return Arviointiasteikot.list({}).$promise;
          },
          tutkinnonOsat: function($stateParams, PerusteTutkinnonosat) {
            return PerusteTutkinnonosat.query({ perusteId: $stateParams.perusteId, suoritustapa: $stateParams.suoritustapa }).$promise;
          },
          koulutusalaService: 'Koulutusalat',
          opintoalaService: 'Opintoalat'
        }
      })
      .state('root.esitys.peruste.rakenne', {
        url: '/rakenne',
        templateUrl: 'views/partials/esitys/rakenne.html',
        controller: 'EsitysRakenneCtrl',
        resolve: {
          // FIXME: ui-router bug or some '$on'-callback manipulating $stateParams?
          // $stateParams changes between config and controller
          //
          // Got to live third-party libs
          realParams: function($stateParams) {
            return _.clone($stateParams);
          },
        }
      })
      .state('root.esitys.peruste.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/partials/esitys/tutkinnonOsat.html',
        controller: 'EsitysTutkinnonOsatCtrl'
      })
      .state('root.esitys.peruste.tutkinnonosa', {
        url: '/tutkinnonosat/:id',
        templateUrl: 'views/partials/esitys/tutkinnonOsa.html',
        controller: 'EsitysTutkinnonOsaCtrl'
      })
      .state('root.esitys.peruste.tekstikappale', {
        url: '/sisalto/:osanId',
        templateUrl: 'views/partials/esitys/sisalto.html',
        controller: 'EsitysSisaltoCtrl'
      })
      .state('root.esitys.peruste.tiedot', {
        url: '/tiedot',
        templateUrl: 'views/partials/esitys/tiedot.html',
        controller: 'EsitysTiedotCtrl'
      });
  })

  .controller('EsitysRakenneCtrl', function($scope, $state, $stateParams, PerusteenRakenne, realParams) {
    $scope.$parent.valittu.sisalto = 'rakenne';
    $scope.muodostumisOtsikko = _.find($scope.$parent.sisalto, function (item) {
      return item.tunniste === 'rakenne';
    });
    PerusteenRakenne.hae(realParams.perusteId, realParams.suoritustapa, function(rakenne) {
      $scope.rakenne = rakenne;
      $scope.rakenne.$suoritustapa = realParams.suoritustapa;
      $scope.rakenne.$resolved = true;
    });

    $scope.suosikkiHelper($state, 'tutkinnon-rakenne');
  })

  .controller('EsitysTutkinnonOsaCtrl', function($scope, $state, $stateParams, PerusteenOsat, TutkinnonosanTiedotService,
      Tutke2Osa) {
    $scope.tutkinnonOsaViite = _.find($scope.$parent.tutkinnonOsat, function(tosa) {
      return tosa.id === parseInt($stateParams.id, 10);
    });
    $scope.osaAlueet = {};
    TutkinnonosanTiedotService.noudaTutkinnonOsa({perusteenOsaId: $scope.tutkinnonOsaViite._tutkinnonOsa}).then(function () {
      $scope.tutkinnonOsa = TutkinnonosanTiedotService.getTutkinnonOsa();
      $scope.fieldKeys = _.intersection(_.keys($scope.tutkinnonOsa), TutkinnonosanTiedotService.keys());
      if ($scope.tutkinnonOsa.tyyppi === 'tutke2') {
        Tutke2Osa.kasitteleOsaAlueet($scope.tutkinnonOsa);
      }
    });
    $scope.suosikkiHelper($state, $scope.tutkinnonOsaViite.nimi);
    $scope.fieldOrder = function (item) {
      return TutkinnonosanTiedotService.order(item);
    };
    $scope.hasArviointi = function (osaamistavoite) {
      return osaamistavoite.arviointi &&
        osaamistavoite.arviointi.arvioinninKohdealueet &&
        osaamistavoite.arviointi.arvioinninKohdealueet.length > 0 &&
        osaamistavoite.arviointi.arvioinninKohdealueet[0].arvioinninKohteet &&
        osaamistavoite.arviointi.arvioinninKohdealueet[0].arvioinninKohteet.length > 0;
    };
  })

  .controller('EsitysTutkinnonOsatCtrl', function($scope, $state, $stateParams, PerusteenRakenne, Algoritmit) {
    $scope.$parent.valittu.sisalto = 'tutkinnonosat';
    $scope.tosarajaus = '';
    $scope.rajaaTutkinnonOsia = function(haku) { return Algoritmit.rajausVertailu($scope.tosarajaus, haku, 'nimi'); };
    $scope.suosikkiHelper($state, 'tutkinnonosat');
  })

  .controller('EsitysTiedotCtrl', function($scope, YleinenData, PerusteenTutkintonimikkeet, $state) {
    $scope.showKoulutukset = function () {
      return YleinenData.showKoulutukset($scope.peruste);
    };

    $scope.koulutusalaNimi = function(koodi) {
      return $scope.Koulutusalat.haeKoulutusalaNimi(koodi);
    };

    $scope.opintoalaNimi = function(koodi) {
      return $scope.Opintoalat.haeOpintoalaNimi(koodi);
    };

    PerusteenTutkintonimikkeet.get($scope.peruste.id, $scope);
    $scope.suosikkiHelper($state, 'perusteen-tiedot');
  })

  .controller('EsitysSisaltoCtrl', function($scope, $state, $stateParams, PerusteenOsat, YleinenData) {
    $scope.$parent.valittu.sisalto = $stateParams.osanId;
    $scope.valittuSisalto = $scope.$parent.sisalto[$stateParams.osanId];
    if (!$scope.valittuSisalto) {
      var params = _.extend(_.clone($stateParams), {
        suoritustapa: YleinenData.validSuoritustapa($scope.peruste, $stateParams.suoritustapa)
      });
      $state.go('root.esitys.peruste.rakenne', params);
    } else {
      $scope.suosikkiHelper($state, $scope.valittuSisalto.nimi);
      PerusteenOsat.get({ osanId: $scope.valittuSisalto.id }, _.setWithCallback($scope, 'valittuSisalto'));
    }
  })

  .controller('EsitysCtrl', function($scope, $stateParams, sisalto, peruste,
      YleinenData, $state, Algoritmit, tutkinnonOsat, Kaanna, arviointiasteikot,
      Profiili, PdfCreation, koulutusalaService, opintoalaService) {

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    var isTutkinnonosatActive = function () {
      return $state.is('root.esitys.peruste.tutkinnonosat') || $state.is('root.esitys.peruste.tutkinnonosa');
    };
    $scope.navi = {
      items: [
        {label: 'perusteen-tiedot', link: ['root.esitys.peruste.tiedot'], $glyph: 'list-alt'},
        {label: 'tutkinnonosat', link: ['root.esitys.peruste.tutkinnonosat'], isActive: isTutkinnonosatActive}
      ],
      header: 'perusteen-sisalto'
    };

    function mapSisalto(sisalto) {
      sisalto = _.clone(sisalto);
      var flattened = {};
      Algoritmit.kaikilleLapsisolmuille(sisalto, 'lapset', function(lapsi, depth) {
        flattened[lapsi.id] = _.clone(lapsi.perusteenOsa);
        $scope.navi.items.push({
          label: lapsi.perusteenOsa.nimi,
          link: lapsi.perusteenOsa.tunniste === 'rakenne' ? ['root.esitys.peruste.rakenne', { suoritustapa: $stateParams.suoritustapa }] : ['root.esitys.peruste.tekstikappale', { osanId: ''+lapsi.id }],
          depth: depth
        });
      });
      return flattened;
    }
    $scope.kaanna = function (val) {
      return Kaanna.kaanna(val);
    };

    $scope.peruste = peruste;
    $scope.backLink = $state.href(YleinenData.koulutustyyppiInfo[$scope.peruste.koulutustyyppi].hakuState);
    $scope.sisalto = mapSisalto(sisalto);

    $scope.arviointiasteikot = _.zipObject(_.map(arviointiasteikot, 'id'), _.map(arviointiasteikot, function(asteikko) {
      return _.zipObject(_.map(asteikko.osaamistasot, 'id'), asteikko.osaamistasot);
    }));
    $scope.tutkinnonOsat = _(tutkinnonOsat).reject(function(r) { return r.poistettu; })
                                           .sortBy(function(r) { return Kaanna.kaanna(r.nimi); })
                                           .value();

    $scope.valittu = {};
    $scope.suoritustavat = _.map(peruste.suoritustavat, 'suoritustapakoodi');
    $scope.suoritustapa = $stateParams.suoritustapa;

    $scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko(peruste, $scope.suoritustapa);

    $scope.vaihdaSuoritustapa = function(suoritustapa) {
      $state.go('root.esitys.peruste', _.merge(_.clone($stateParams), { suoritustapa: suoritustapa }), { reload: true });
    };

    if ($state.current.name === 'root.esitys.peruste') {
      var params = _.extend(_.clone($stateParams), {
        suoritustapa: YleinenData.validSuoritustapa($scope.peruste, $stateParams.suoritustapa)
      });
      $state.go('root.esitys.peruste.rakenne', params);
    }

    $scope.rajaaSisaltoa = function() {
      _.forEach($scope.sisaltoRakenne, function(r) {
        r.$rejected = _.isEmpty($scope.rajaus) ? false : !Algoritmit.match($scope.rajaus, $scope.sisalto[r.id].nimi);
        if (!r.$rejected) {
          var parent = $scope.sisaltoRakenneMap[r.parent];
          while (parent) {
            parent.$rejected = false;
            parent = $scope.sisaltoRakenneMap[parent.parent];
          }
        }
      });
      $scope.extra.tutkinnonOsat = !Algoritmit.match($scope.rajaus, Kaanna.kaanna('tutkinnonosat'));
      $scope.extra.tutkinnonRakenne = !Algoritmit.match($scope.rajaus, Kaanna.kaanna('tutkinnon-rakenne'));
    };

    $scope.suosikkiHelper = function(state, nimi) {
      $scope.onSuosikki = Profiili.haeSuosikki(state);
      $scope.asetaSuosikki = function() {
        Profiili.asetaSuosikki(state, Kaanna.kaanna($scope.peruste.nimi) + ': ' + (Kaanna.kaanna(nimi) || '') + ' (' + Kaanna.kaanna($scope.suoritustapa) + ')', function() {
          $scope.onSuosikki = Profiili.haeSuosikki(state);
        });
      };
    };

    $scope.printSisalto = function() {
      var print = window.open('', 'esitysPrintSisalto', 'height=640,width=640');
      print.document.write('<html><head><link rel="stylesheet" href="styles/eperusteet.css"></head><body class="esitys-print-view">' +
                           $('#esitysPrintSisalto').html() +
                           '</body></html>');
      print.print();
      print.close();
    };

    $scope.luoPdf = function () {
      PdfCreation.setPerusteId($scope.peruste.id);
      PdfCreation.openModal();
    };
  })

  .directive('esitysSivuOtsikko', function ($compile) {
    var TEMPLATE = '<div class="painikkeet pull-right">' +
      '<a class="action-link" ng-click="asetaSuosikki()">' +
      '<span class="glyphicon" ng-class="{\'glyphicon-star\': onSuosikki, \'glyphicon-star-empty\': !onSuosikki}"></span>' +
      '{{ onSuosikki ? \'poista-suosikeista\' : \'merkitse-suosikiksi\' | kaanna }}' +
      '</a>' +
      '<a class="action-link left-space" ng-click="printSisalto()" icon-role="print" kaanna="\'tulosta-sivu\'"></a>' +
      '</div>';
    return {
      restrict: 'AE',
      link: function (scope, element) {
        var compiled = $compile(TEMPLATE)(scope);
        element.append(compiled);
      }
    };
  });

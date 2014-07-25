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
            return Arviointiasteikot.query({}).$promise;
          },
          tutkinnonOsat: function($stateParams, PerusteTutkinnonosat) {
            return PerusteTutkinnonosat.query({ perusteId: $stateParams.perusteId, suoritustapa: $stateParams.suoritustapa }).$promise;
          }
        }
      })
      .state('root.esitys.peruste.rakenne', {
        url: '/rakenne',
        templateUrl: 'views/partials/esitys/rakenne.html',
        controller: 'EsitysRakenneCtrl'
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
      });
  })
  .controller('EsitysRakenneCtrl', function($scope, $stateParams, PerusteenRakenne) {
    $scope.$parent.valittu.sisalto = 'rakenne';
    PerusteenRakenne.hae($stateParams.perusteId, $stateParams.suoritustapa, function(rakenne) {
      $scope.rakenne = rakenne;
      $scope.rakenne.$suoritustapa = $stateParams.suoritustapa;
      $scope.rakenne.$resolved = true;
    });
  })
  .controller('EsitysTutkinnonOsaCtrl', function($scope, $stateParams, PerusteenOsat) {
    $scope.tutkinnonOsaViite = _.find($scope.$parent.tutkinnonOsat, function(tosa) {
      return tosa.id === parseInt($stateParams.id, 10);
    });
    PerusteenOsat.get({ osanId: $scope.tutkinnonOsaViite._tutkinnonOsa }, function(res) { $scope.tutkinnonOsa = res; });
  })
  .controller('EsitysTutkinnonOsatCtrl', function($scope, $stateParams, PerusteenRakenne, Algoritmit) {
    $scope.$parent.valittu.sisalto = 'tutkinnonosat';
    $scope.tosarajaus = '';
    $scope.rajaaTutkinnonOsia = function(haku) { return Algoritmit.rajausVertailu($scope.tosarajaus, haku, 'nimi'); };
  })
  .controller('EsitysSisaltoCtrl', function($scope, $stateParams, Lokalisointi) {
    $scope.$parent.valittu.sisalto = $stateParams.osanId;
    $scope.valittuSisalto = $scope.$parent.sisalto[$stateParams.osanId];
    Lokalisointi.valitseKieli($stateParams.lang);
  })
  .controller('EsitysCtrl', function($q, $scope, $stateParams, sisalto, peruste, Kayttajaprofiilit, Suosikit, Suosikitbroadcast, YleinenData,
                                     Navigaatiopolku, $state, virheService, Algoritmit, PerusteenRakenne, tutkinnonOsat, Kaanna, arviointiasteikot) {
    $scope.navi = {
      items: [
        {label: 'tutkinnonosat', link: ['root.esitys.peruste.tutkinnonosat', {}]},
        {label: 'tutkinnon-rakenne', link: ['root.esitys.peruste.rakenne', {}]},
      ]
    };

    function mapSisalto(sisalto) {
      sisalto = _.clone(sisalto);
      var flattened = {};
      Algoritmit.kaikilleLapsisolmuille(sisalto, 'lapset', function(lapsi) {
        flattened[lapsi.id] = _.clone(lapsi.perusteenOsa);
        $scope.navi.items.push({
          label: lapsi.perusteenOsa.nimi,
          link: ['root.esitys.peruste.tekstikappale', { osanId: ''+lapsi.id }]
        });
      });
      return flattened;
    }

    $scope.peruste = peruste;
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
      $state.go('root.esitys.peruste', {suoritustapa: suoritustapa});
    };

    if ($state.current.name === 'root.esitys.peruste') {
      var links = _.filter($scope.navi.items, function (item) {
        return item.label !== 'tutkinnonosat' && item.label !== 'tutkinnon-rakenne' &&
          _.isArray(item.link) && item.link.length > 1;
      });
      var params = _.isEmpty(links) ? {} : { osanId: _.first(links).link[1].osanId };
      $state.go('root.esitys.peruste.tekstikappale', params);
    }
  });

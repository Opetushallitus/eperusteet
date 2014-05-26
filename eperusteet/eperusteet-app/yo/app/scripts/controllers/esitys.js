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
      .state('esitys', {
        url: '/esitys',
        template: '<div ui-view></div>'
      })
      .state('esitys.peruste', {
        url: '/:perusteenId/:suoritustapa',
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        naviRest: [':perusteenId']
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $stateParams, Kayttajaprofiilit, Suosikit,
      Perusteet, Suosikitbroadcast, Suoritustapa, YleinenData, Navigaatiopolku,
      palvelinhaunIlmoitusKanava, PerusteRakenteet, TreeCache, $state) {

    $scope.konteksti = $stateParams.konteksti;
    $scope.peruste = {};
    $scope.syvyys = 2;
    $scope.suosikkiLista = {};
    $scope.rakenne = {};
    var eiSuosikkiTyyli = 'glyphicon glyphicon-star-empty pointer';
    var suosikkiTyyli = 'glyphicon glyphicon-star pointer';
    $scope.suosikkiTyyli = eiSuosikkiTyyli;
    $scope.suoritustapa = $stateParams.suoritustapa;
    var suosikkiId = null;
    $scope.suodatin = {};

    // function haeRakenne(suoritustapa) {
    //   PerusteRakenteet.get({
    //       perusteenId: $stateParams.perusteenId,
    //       suoritustapa: suoritustapa
    //   }, function(re) {
    //     $scope.rakenne.rakenne = re;
    //     $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
    //   });
    // }

    // $scope.peruMuutokset = haeRakenne;

    // if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    // else { TreeCache.hae(); }
    //
    $scope.tallennaRakenne = function(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      rakenne.tutkinnonOsat = _.values(rakenne.tutkinnonOsat);
      PerusteRakenteet.save({ perusteenId: $stateParams.perusteenId }, rakenne, function(re) {
        console.log(re);
      });
    };

    var perusteHakuPromise = (function() {
      if ($stateParams.perusteenId) {
        return Perusteet.get({perusteenId: $stateParams.perusteenId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    var kayttajaProfiiliPromise = Kayttajaprofiilit.get({}).$promise;

    perusteHakuPromise.then(function(peruste) {
      if (peruste.id) {
        Navigaatiopolku.asetaElementit({
          peruste: { nimi: peruste.nimi }
        });
        // haeRakenne(peruste.suoritustavat[0].suoritustapakoodi);
        $scope.peruste = peruste;
        haeSuoritustapaSisalto(peruste.id);
      } else {
        // TODO perustetta ei löytynyt, virhesivu.
      }
    }, function(error) {
      console.log(error);
      // TODO
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      // $location.path('/selaus/' + $scope.konteksti);
    });

    kayttajaProfiiliPromise.then(function(profiili) {
      $scope.suosikkiLista = profiili.suosikit;
      $scope.suosikkiTyyli = $scope.onSuosikki();
    }, function() {
      console.log('profiilia ei löytynyt');
      $scope.suosikkiLista = [];
      $scope.suosikkiTyyli = $scope.onSuosikki();
    });

    var haeSuoritustapaSisalto = function (id) {
      Suoritustapa.get({perusteenId: id, suoritustapa: $scope.suoritustapa}, function(vastaus) {
        $scope.peruste.rakenne = vastaus;
        $scope.suodatin.otsikot = _.compact(_.pluck(_.pluck(vastaus.lapset, 'perusteenOsa'), 'nimi'));
      }, function (virhe) {
          console.log('suoritustapasisältöä ei löytynyt', virhe);
      });
    };

    $scope.onSuosikki = function() {
      for (var i = 0; i < _.size($scope.suosikkiLista); i++) {
        if ($scope.suosikkiLista[i].perusteId === $scope.peruste.id && $scope.suosikkiLista[i].suoritustapakoodi === $scope.suoritustapa) {
          suosikkiId = $scope.suosikkiLista[i].id;
          return suosikkiTyyli;
        }
      }
      suosikkiId = null;
      return eiSuosikkiTyyli;
    };

    $scope.asetaSuosikiksi = function() {
      if ($scope.suosikkiTyyli === eiSuosikkiTyyli) {

        Suosikit.save({}, {perusteId: $scope.peruste.id, suoritustapakoodi: $scope.suoritustapa}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });

      } else {
        Suosikit.delete({suosikkiId: suosikkiId}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });
      }
    };

    $scope.suodatinValittu = function(suodatinId) {
      var suodatinTmp = _.find($scope.suodatin.otsikot, function(suodatin) {
        return suodatin._id === suodatinId;
      });
      suodatinTmp.valittu = true;

      $scope.suodatinId = '';
    };

    $scope.poistaSuodatin = function (suodatin) {
      suodatin.valittu = false;
    };

    $scope.onkoSuodatettu = function (id) {
      var valitutSuodattimet = _.filter($scope.suodatin.otsikot, 'valittu');
      return valitutSuodattimet.length === 0 || _.isObject(_.find(valitutSuodattimet, function(suodatin) {return suodatin._id === id;}));
    };

    $scope.valitseKieli = function(teksti) {
      return YleinenData.valitseKieli(teksti);
    };

    $scope.$on('optioPoistettu', function() {
      $scope.$broadcast('optiotMuuttuneet');
    });

    $scope.vaihdaSuoritustapa = function(suoritustapa) {
      $state.go('esitys.peruste', {perusteenId: $stateParams.perusteenId, suoritustapa: suoritustapa});
    };


    var hakuAloitettuKäsittelijä = function() {
      $scope.hakuMenossa = true;
    };

    var hakuLopetettuKäsittelijä = function() {
      $scope.hakuMenossa = false;
    };
    palvelinhaunIlmoitusKanava.kunHakuAloitettu($scope, hakuAloitettuKäsittelijä);
    palvelinhaunIlmoitusKanava.kunHakuLopetettu($scope, hakuLopetettuKäsittelijä);

    $scope.terveydentilaOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Terveydentila optio 1', valittu: false},
      {teksti: 'Terveydentila optio 2', valittu: false},
      {teksti: 'Terveydentila optio 3', valittu: false},
      {teksti: 'Terveydentila optio 4', valittu: false}
    ];

    $scope.todistuksetOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Todistukset optio 1', valittu: false},
      {teksti: 'Todistukset optio 2', valittu: false},
      {teksti: 'Todistukset optio 3', valittu: false}
    ];

    $scope.arviointiOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Oppilaan arviointi oppiaineessa', valittu: false},
      {teksti: 'Oppiaineen hyvän edistymisen kuvaus', valittu: false},
      {teksti: 'Oppiaineen hyvän osaamisen kuvaus', valittu: false},
      {teksti: 'Oppiaineen päätösarvioinnin kriteerit arvosanalle 8', valittu: false},
      {teksti: 'Todistukset', valittu: false},
      {teksti: 'Erityisen tutkinnon suoritusten arviointi ja muutokset', valittu: false}
    ];

    $scope.maarayksetOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Määräykset optio 1', valittu: false},
      {teksti: 'Määräykset optio 2', valittu: false},
      {teksti: 'Määräykset optio 3', valittu: false},
      {teksti: 'Määräykset optio 4', valittu: false},
      {teksti: 'Määräykset optio 5', valittu: false}
    ];
  });

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
        url: '/:perusteId/:suoritustapa',
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        naviRest: [':perusteId']
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $stateParams, Kayttajaprofiilit, Suosikit,
      Perusteet, Suosikitbroadcast, SuoritustapaSisalto, YleinenData, Navigaatiopolku, PerusteRakenteet, $state, virheService) {

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
    //       perusteId: $stateParams.perusteId,
    //       suoritustapa: suoritustapa
    //   }, function(re) {
    //     $scope.rakenne.rakenne = re;
    //     $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
    //   });
    // }

    // $scope.peruMuutokset = haeRakenne;

    var perusteHakuPromise = (function() {
      if ($stateParams.perusteId) {
        return Perusteet.get({perusteId: $stateParams.perusteId}).$promise;
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
        virheService.virhe('virhe-perustetta-ei-löytynyt');
      }
    }, function() {
      virheService.virhe('virhe-perustetta-ei-löytynyt');
    });

    kayttajaProfiiliPromise.then(function(profiili) {
      $scope.suosikkiLista = profiili.suosikit;
      $scope.suosikkiTyyli = $scope.onSuosikki();
    }, function() {
      virheService.virhe('virhe-profiilia-ei-löytynyt');
      $scope.suosikkiLista = [];
      $scope.suosikkiTyyli = $scope.onSuosikki();
    });

    var haeSuoritustapaSisalto = function (id) {
      SuoritustapaSisalto.get({perusteId: id, suoritustapa: $scope.suoritustapa}, function(vastaus) {
        $scope.peruste.rakenne = vastaus;
        $scope.suodatin.otsikot = _.compact(_.pluck(_.pluck(vastaus.lapset, 'perusteenOsa'), 'nimi'));
      }, function () {
        virheService.virhe('virhe-suoritustapasisältöä-ei-löytynyt');
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

    $scope.$on('optioPoistettu', function() {
      $scope.$broadcast('optiotMuuttuneet');
    });

    $scope.vaihdaSuoritustapa = function(suoritustapa) {
      $state.go('esitys.peruste', {perusteId: $stateParams.perusteId, suoritustapa: suoritustapa});
    };


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

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
  .service('TutkinnonOsanTuonti', function($modal) {
    function suoritustavoista(peruste, nykyinenTyyppi, successCb, failureCb) {
      failureCb = failureCb || function() {};
      return function() {
        $modal.open({
          templateUrl: 'views/modals/tuotutkinnonosasta.html',
          controller: 'TuoTutkinnonOsaSuoritustavastaaCtrl',
          resolve: {
            peruste: function() { return peruste; },
            suoritustapa: function() { return nykyinenTyyppi; },
          }
        })
        .result.then(successCb, failureCb);
      };
    }

    function kaikista(tyyppi, successCb, failureCb) {
      failureCb = failureCb || function() {};
      return function() {
        $modal.open({
          templateUrl: 'views/modals/haetutkinnonosa.html',
          controller: 'TuoTutkinnonOsaCtrl',
          resolve: {
            tyyppi: function() { return tyyppi; }
          }
        })
        .result.then(successCb, failureCb);
      };
    }

    return {
      kaikista: kaikista,
      suoritustavoista: suoritustavoista
    };
  })
  .controller('TuoTutkinnonOsaSuoritustavastaaCtrl', function(PerusteenOsat, $scope, $modalInstance, peruste, PerusteTutkinnonosat, Notifikaatiot, suoritustapa) {
    $scope.tulokset = [];
    $scope.valitut = 0;
    $scope.peruste = peruste;
    $scope.suoritustavat = _(peruste.suoritustavat).map('suoritustapakoodi')
                                                   .reject(function(st) { return st === suoritustapa; })
                                                   .value();

    $scope.valinta = function(tulos) {
      $scope.valitut += tulos.$valitse ? -1 : 1;
    };

    $scope.vaihdaValinta = function(tulos) {
        tulos.$valitse = !tulos.$valitse;
        $scope.valinta(tulos);
    };

    $scope.paivitaTulokset = function(st) {
      PerusteTutkinnonosat.get({
        perusteId: peruste.id,
        suoritustapa: st
      },
      function(res) {
        $scope.tulokset = _(res).map(function(osa) {
                                  return {
                                    _tutkinnonOsa: osa._tutkinnonOsa,
                                    nimi: osa.nimi
                                  };
                                })
                                .value();
      },
      Notifikaatiot.serverCb);
    };

    $scope.ok = function() {
      $modalInstance.close(_.filter($scope.tulokset, function(tulos) {
        return tulos.$valitse;
      }));
    };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  })
  .controller('TuoTutkinnonOsaCtrl', function(PerusteenOsat, $scope, $modalInstance,
    Perusteet, PerusteRakenteet, PerusteTutkinnonosat, tyyppi) {

    $scope.haku = true;
    $scope.perusteet = [];
    $scope.perusteenosat = [];
    $scope.valittu = {};
    $scope.sivukoko = 10;
    $scope.data = {
      nykyinensivu: 1,
      hakustr: ''
    };

    $scope.takaisin = function() {
      $scope.haku = true;
    };
    $scope.valitse = function() {
      $modalInstance.close(_.filter($scope.perusteenosat, function(osa) { return osa.$valitse; }));
    };

    $scope.paivitaHaku = function() {
      Perusteet.get({
          nimi: $scope.data.hakustr,
          sivukoko: $scope.sivukoko,
          sivu: $scope.data.nykyinensivu - 1
      }, function(perusteet) {
        $scope.perusteet = perusteet;
      });
    };

    $scope.hakuMuuttui = _.debounce(_.bind($scope.paivitaHaku, $scope), 300);

    $scope.jatka = function(par) {
      $scope.haku = false;
      $scope.valittu = par;
      PerusteTutkinnonosat.query({
        perusteId: par.id,
        suoritustapa: tyyppi
      }, function(re) {
        $scope.perusteenosat = re;
      });
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };

    $scope.paivitaHaku();
  });

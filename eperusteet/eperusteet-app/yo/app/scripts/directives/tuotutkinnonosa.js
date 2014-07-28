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
    function modaali(tyyppi, successCb, failureCb) {
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
      modaali: modaali
    };
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
        perusteenId: par.id,
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

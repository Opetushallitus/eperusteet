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
  .controller('SuosikitCtrl', function($scope, Kayttajaprofiilit, YleinenData, $rootScope, $state, Navigaatiopolku) {

    $scope.suosikit = {};
    $scope.suppeaMaara = 5;
    $scope.suosikkiRaja = $scope.suppeaMaara;
    $scope.projektitRaja = $scope.suppeaMaara;
    $scope.lisaaSuosikkeja = false;
    $scope.naytetaanKaikkiSuosikit = false;
    $scope.naytetaanKaikkiProjektit = false;
    $scope.suosikkiNapinTeksti = '';

    var naytaKaikkiTeksti = 'sivupalkki-näytä-kaikki';
    var piilotaTeksti = 'sivupalkki-piilota';

    $scope.resetNavi = Navigaatiopolku.clear;

    var paivitaSuosikit = function() {
      Kayttajaprofiilit.get({}, function(vastaus) {

        YleinenData.lisääKontekstitPerusteisiin(vastaus.suosikit);
        $scope.suosikit = _.map(vastaus.suosikit, function(s) {
          if (s.perusteId && s.suoritustapakoodi) {
            console.log('perusteenId', s.perusteId);
            console.log('suoritustapakoodi', s.suoritustapakoodi);
            s.url = $state.href('esitys.peruste', { perusteenId: s.perusteId, suoritustapa: s.suoritustapakoodi });
          }
          return s;
        });

        console.log('suosikit', $scope.suosikit);
        if ($scope.naytetaanKaikkiSuosikit) {
          $scope.suosikkiNapinTeksti = piilotaTeksti;
          $scope.suosikkiRaja = _.size($scope.suosikit);
        } else {
          $scope.suosikkiNapinTeksti = naytaKaikkiTeksti;
        }

        if (_.size($scope.suosikit) > $scope.suppeaMaara) {
          $scope.naytaSuosikkiNappi = true;
        } else {
          $scope.naytaSuosikkiNappi = false;
        }
      });
    };

    paivitaSuosikit();

    $scope.muutaSuosikkiMaara = function() {
      $scope.naytetaanKaikkiSuosikit = !$scope.naytetaanKaikkiSuosikit;

      if ($scope.naytetaanKaikkiSuosikit) {
        $scope.suosikkiRaja = _.size($scope.suosikit);
        $scope.suosikkiNapinTeksti = piilotaTeksti;

      } else {
        $scope.suosikkiRaja = $scope.suppeaMaara;
        $scope.suosikkiNapinTeksti = naytaKaikkiTeksti;
      }
    };

    $scope.$on('suosikitMuuttuivat', function() {
      paivitaSuosikit();
    });

  });

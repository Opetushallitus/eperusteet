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
  .controller('OmatperusteprojektitCtrl', function ($scope, $state, Kayttajaprofiilit) {
    $scope.projektit = {};
    $scope.suppeaMaara = 5;
    $scope.projektitRaja = $scope.suppeaMaara;
    $scope.naytetaanKaikkiProjektit = false;
    $scope.projektitNapinTeksti = '';

    var naytaKaikkiTeksti = 'sivupalkki-näytä-kaikki';
    var piilotaTeksti = 'sivupalkki-piilota';

    var paivitaOmatProjektit = function() {
      Kayttajaprofiilit.get({}, function(vastaus) {

        $scope.projektit = _.forEach(vastaus.perusteprojektit, function(pp) {
          pp.url = $state.href('perusteprojekti.suoritustapa.sisalto', { perusteProjektiId: pp.id, suoritustapa: 'naytto' });
        });

        if ($scope.naytetaanKaikkiSuosikit) {
          $scope.projektitNapinTeksti = piilotaTeksti;
          $scope.projektitRaja = _.size($scope.projektit);
        } else {
          $scope.projektitNapinTeksti = naytaKaikkiTeksti;
        }

        if (_.size($scope.projektit) > $scope.suppeaMaara) {
          $scope.naytaProjektitNappi = true;
        } else {
          $scope.naytaProjektitNappi = false;
        }
      });
    };

    paivitaOmatProjektit();

    $scope.muutaProjektienMaara = function() {
      $scope.naytetaanKaikkiProjektit = !$scope.naytetaanKaikkiProjektit;

      if ($scope.naytetaanKaikkiProjektit) {
        $scope.projektitRaja = _.size($scope.projektit);
        $scope.projektitNapinTeksti = piilotaTeksti;

      } else {
        $scope.projektitRaja = $scope.suppeaMaara;
        $scope.projektitNapinTeksti = naytaKaikkiTeksti;
      }
    };

    $scope.$on('update:perusteprojekti', function() {
      paivitaOmatProjektit();
    });
  });

'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('OmatperusteprojektitCtrl', function ($scope, Kayttajaprofiilit) {
    $scope.projektit = {};
    $scope.suppeaMaara = 5;
    $scope.projektitRaja = $scope.suppeaMaara;
    $scope.naytetaanKaikkiProjektit = false;
    $scope.projektitNapinTeksti = '';

    var naytaKaikkiTeksti = 'sivupalkki-näytä-kaikki';
    var piilotaTeksti = 'sivupalkki-piilota';

    var paivitaOmatProjektit = function() {
      Kayttajaprofiilit.get({}, function(vastaus) {

        $scope.projektit = vastaus.perusteprojektit;

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

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

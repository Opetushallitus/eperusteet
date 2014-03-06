'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('SuosikitCtrl', function($scope, Kayttajaprofiilit, YleinenData, Suosikitbroadcast) {

    $scope.suosikit = {};
    $scope.suppeaMaara = 5;
    $scope.suosikkiRaja = $scope.suppeaMaara;
    $scope.projektitRaja = $scope.suppeaMaara;
    $scope.lisaaSuosikkeja = false;
    $scope.naytetaanKaikkiSuosikit = false;
    $scope.naytetaanKaikkiProjektit = false;
    var naytaKaikkiTeksti = 'sivupalkki-näytä-kaikki';
    var piilotaTeksti = 'sivupalkki-piilota';
    $scope.kielet = YleinenData.kielet;
    $scope.kieli = YleinenData.kieli;


    var paivitaSuosikit = function() {
      Kayttajaprofiilit.get({}, function(vastaus) {

        YleinenData.lisääKontekstitPerusteisiin(vastaus.suosikit);
        $scope.suosikit = vastaus.suosikit;

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
    
    $scope.vaihdaKieli = function(kielikoodi) {
      console.log('Vaihdakieli: ' + kielikoodi);
      $scope.kieli = kielikoodi;
      YleinenData.vaihdaKieli(kielikoodi);
      Suosikitbroadcast.kieliVaihtui();
    };

    // Alustetaan UI alku hetken kielivalinnalla
    $scope.vaihdaKieli(YleinenData.kieli);

    $scope.valitseKieli = YleinenData.valitseKieli;

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

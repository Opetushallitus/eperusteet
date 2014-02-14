'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('SuosikitCtrl', function($scope, Kayttajaprofiilit, YleinenData, Suosikitbroadcast) {

    $scope.suosikit = {};
    $scope.suppeaSuosikkiMaara = 5;
    $scope.suosikkiRaja = $scope.suppeaSuosikkiMaara;
    $scope.lisaaSuosikkeja = false;
    $scope.naytetaanKaikkiSuosikit = false;
    var naytaKaikkiTeksti = 'haku-lisaa-suosikkeja';
    var piilotaSuosikitTeksti = 'haku-piilota-suosikkeja';
    $scope.kielet = YleinenData.kielet;
    $scope.kieli = YleinenData.kieli;


    var paivitaSuosikit = function() {
      Kayttajaprofiilit.get({}, function(vastaus) {

        YleinenData.lisääKontekstitPerusteisiin(vastaus.suosikit);
        $scope.suosikit = vastaus.suosikit;

        if ($scope.naytetaanKaikkiSuosikit) {
          $scope.suosikkiNapinTeksti = piilotaSuosikitTeksti;
          $scope.suosikkiRaja = _.size($scope.suosikit);
        } else {
          $scope.suosikkiNapinTeksti = naytaKaikkiTeksti;
        }

        if (_.size($scope.suosikit) > $scope.suppeaSuosikkiMaara) {
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
        $scope.suosikkiNapinTeksti = piilotaSuosikitTeksti;

      } else {
        $scope.suosikkiRaja = $scope.suppeaSuosikkiMaara;
        $scope.suosikkiNapinTeksti = naytaKaikkiTeksti;
      }
    };

    $scope.$on('suosikitMuuttuivat', function() {
      paivitaSuosikit();
    });

  });

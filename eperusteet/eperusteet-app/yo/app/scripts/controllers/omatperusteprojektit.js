'use strict';

angular.module('eperusteApp')
  .controller('OmatperusteprojektitCtrl', function ($scope, Kayttajaprofiilit) {
    $scope.projektit = {};
    $scope.suppeaMaara = 5;
    $scope.projektitRaja = $scope.suppeaMaara;
    $scope.naytetaanKaikkiProjektit = false;
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
          $scope.naytaProjektiNappi = true;
        } else {
          $scope.naytaProjektiNappi = false;
        }
      });
    };
    
    paivitaOmatProjektit();
    
    $scope.muutaProjektiMaara = function() {
      $scope.naytetaanKaikkiProjektit = !$scope.naytetaanKaikkiProjektit;

      if ($scope.naytetaanKaikkiProjektit) {
        $scope.projektiRaja = _.size($scope.projektit);
        $scope.projektitNapinTeksti = piilotaTeksti;

      } else {
        $scope.projektiRaja = $scope.suppeaMaara;
        $scope.projektitNapinTeksti = naytaKaikkiTeksti;
      }
    };
  });

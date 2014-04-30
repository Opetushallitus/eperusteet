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
  .controller('TuoTutkinnonOsaCtrl', function(PerusteenOsat, $scope, $modalInstance, Perusteet, PerusteRakenteet, PerusteTutkinnonosat, tyyppi) {
    $scope.haku = true;
    $scope.perusteet = [];
    $scope.perusteenosat = [];
    $scope.valittu = {};

    $scope.takaisin = function() { $scope.haku = true; };
    $scope.valitse = function() { $modalInstance.close(_.filter($scope.perusteenosat, function(osa) { return osa.$valitse; })); };

    $scope.paivitaHaku = function(haku, sivu) {
      Perusteet.get({
          nimi: haku,
          sivukoko: 10,
          sivu: sivu
      }, function(perusteet) {
        $scope.perusteet = perusteet;
      });
    };
    console.log(tyyppi);

    $scope.jatka = function(par) {
      $scope.haku = false;
      $scope.valittu = par;
      console.log(par.id, tyyppi);
      PerusteTutkinnonosat.query({
        perusteenId: par.id,
        suoritustapa: tyyppi,
      }, function(re) {
        $scope.perusteenosat = re;
      });
    };

    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });

'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('TutkinnonOsanTuonti', function($modal) {
    function modaali(successCb, failureCb) {
      failureCb = failureCb || function() {};
      return function() {
        $modal.open({
          templateUrl: 'views/modals/haetutkinnonosa.html',
          controller: 'TuoTutkinnonOsaCtrl'
        })
        .result.then(successCb, failureCb);
      };
    }

    return {
      modaali: modaali
    };
  })
  .controller('TuoTutkinnonOsaCtrl', function(PerusteenOsat, $scope, $modalInstance, Perusteet, PerusteRakenteet) {
    $scope.haku = true;
    $scope.perusteet = [];
    $scope.perusteenosat = [];
    $scope.valittu = {};

    $scope.takaisin = function() { $scope.haku = true; };
    $scope.valitse = function(tutkinnonosa) { $modalInstance.close(_.clone(tutkinnonosa)); };

    $scope.paivitaHaku = function(haku, sivu) {
      Perusteet.query({
          nimi: haku,
          sivukoko: 10,
          sivu: sivu
      }, function(perusteet) {
        $scope.perusteet = perusteet;
      });
    };

    $scope.jatka = function(par) {
      $scope.haku = false;
      $scope.valittu = par;
      PerusteRakenteet.query({ perusteenId: par.id }, function(re) {
        $scope.perusteenosat = _.values(re.tutkinnonOsat);
      });
    };

    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });

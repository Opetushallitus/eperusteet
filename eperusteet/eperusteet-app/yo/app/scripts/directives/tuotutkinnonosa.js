'use strict';

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
  .controller('TuoTutkinnonOsaCtrl', function(PerusteenOsat, $scope, $modalInstance) {
    $scope.vaihe = 0;
    $scope.haku = {};
    $scope.tulokset = [];
    $scope.tulos = {};

    $scope.valitse = function(tutkinnonosa) {
      $modalInstance.close(_.clone(tutkinnonosa));
    };

    $scope.jatka = function(par) {
      var old = $scope.vaihe;
      if ($scope.vaihe < 2) {
        $scope.vaihe += 1;
      }
      if ($scope.vaihe === 1 && old === 0) {
        PerusteenOsat.query({ nimi: $scope.haku.str }, function(re) {
          $scope.tulokset = re;
        });
      } else if ($scope.vaihe === 2 && old === 1) {
        $scope.tulos = par;
      }
    };

    $scope.takaisin = function() {
      if ($scope.vaihe > 0) {
        $scope.vaihe -= 1;
      }
    };

    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });

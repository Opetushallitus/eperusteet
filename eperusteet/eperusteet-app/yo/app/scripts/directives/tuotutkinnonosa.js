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
  .controller('TuoTutkinnonOsaCtrl', function($scope, $modalInstance) {
    $scope.vaihe = 0;

    $scope.jatka = function() {
      if ($scope.vaihe < 2) {
        $scope.vaihe += 1;
      }
    };

    $scope.takaisin = function() {
      if ($scope.vaihe > 0) {
        $scope.vaihe -= 1;
      }
    };

    $scope.ok = function(data) { $modalInstance.close(data); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });

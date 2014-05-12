'use strict';

angular.module('eperusteApp')
  .service('Varmistusdialogi', function($modal) {

    function dialogi(successCb, failureCb, otsikko, teksti) {
      console.log('dialogi');
      return function() {
        var resolve = {};
        resolve.otsikko = function() {
          return otsikko || '';
        };
        resolve.teksti = function() {
          return teksti || '';
        };
        failureCb = failureCb || function() {
        };

        $modal.open({
          templateUrl: 'views/modals/varmistusdialogi.html',
          controller: 'VarmistusDialogiCtrl',
          resolve: resolve
        }).result.then(successCb, failureCb);
      };
    }

    return {
      dialogi: dialogi
    };

  })
  .controller('VarmistusDialogiCtrl', function($scope, $modalInstance, otsikko, teksti) {
    
    $scope.otsikko = otsikko;
    $scope.teksti = teksti;
    
    $scope.ok = function() {
      $modalInstance.close();
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  });

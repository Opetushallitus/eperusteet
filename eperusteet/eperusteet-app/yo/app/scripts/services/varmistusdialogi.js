'use strict';

angular.module('eperusteApp')
  .service('Varmistusdialogi', function($modal) {

    function dialogi(options) {
      return function() {
        var resolve = {};
        resolve.otsikko = function() {
          return options.otsikko || '';
        };
        resolve.teksti = function() {
          return options.teksti || '';
        };
        var failureCb = options.failureCb || angular.noop;
        var successCb = options.successCb || angular.noop;

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

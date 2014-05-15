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
        resolve.cbData = function() {
          return options.data || null;
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
  .controller('VarmistusDialogiCtrl', function($scope, $modalInstance, otsikko, teksti, cbData) {

    $scope.otsikko = otsikko;
    $scope.teksti = teksti;

    $scope.ok = function() {
      if (cbData !== null) {
        $modalInstance.close(cbData);
      } else {
        $modalInstance.close();
      }
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  });

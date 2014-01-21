'use strict';

angular.module('eperusteApp')
.controller('UudelleenohjausModalCtrl', function($scope, $modalInstance, status) {
  $scope.status = status;

  $scope.ok = function() {
    $modalInstance.close();
  }
});

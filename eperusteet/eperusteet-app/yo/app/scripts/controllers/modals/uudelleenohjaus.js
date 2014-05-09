'use strict';

angular.module('eperusteApp')
.controller('UudelleenohjausModalCtrl', function($scope, $modalInstance, status, redirect) {
  $scope.status = status;
  $scope.casurl = redirect;

  $scope.ok = function() {
    $modalInstance.close();
  };
});

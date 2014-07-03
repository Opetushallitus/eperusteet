'use strict';

angular.module('eperusteApp')
  .controller('TilanvaihtovirheCtrl', function ($scope, $modalInstance, infot){
    $scope.infot = infot;
    $scope.ok = function() { $modalInstance.close(); };
  });

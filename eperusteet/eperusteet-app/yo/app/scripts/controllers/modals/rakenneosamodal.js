'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('RakenneosaModalCtrl', function ($scope, $modalInstance, rakenneosa) {


    var setupRyhma = function (rakenneosa) {
      $scope.rakenneosa = rakenneosa;
      if (!$scope.rakenneosa.kuvaus || !_.isObject($scope.rakenneosa.kuvaus)) {
        $scope.rakenneosa.kuvaus = {};
      }
    };
    setupRyhma(rakenneosa);

    $scope.ok = function() {
      $modalInstance.close($scope.rakenneosa);
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  });

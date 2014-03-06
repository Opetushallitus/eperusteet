'use strict';

angular.module('eperusteApp')
  .controller('ProjektinperustiedotCtrl', function($scope, PerusteProjektiService) {
  PerusteProjektiService.watcher($scope, 'projekti');

  $scope.kalenteriAuki = false;

  $scope.today = function() {
    $scope.dt = new Date();
  };
  $scope.today();

  $scope.showWeeks = true;
  $scope.toggleWeeks = function() {
    $scope.showWeeks = !$scope.showWeeks;
  };

  $scope.clear = function() {
    $scope.dt = null;
  };

  $scope.open = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    $scope.kalenteriAuki = !$scope.kalenteriAuki;
  };

  $scope.dateOptions = {
    'year-format': 'yy',
    'starting-day': 1
  };

  $scope.format = 'd.M.yyyy';
});


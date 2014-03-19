'use strict';
angular.module('eperusteApp')
  .directive('eperusteNavi', function() {
    return {
      templateUrl: 'views/partials/navi.html',
      restrict: 'E',
      transclude: false
    };
  })
  .controller('NaviCtrl', function($scope, $location, navigaatiopolku) {
    $scope.navigaatiopolut = navigaatiopolku.getKaikki();
    $scope.isActive = function(viewLocation) {
      return $location.path().substring(0, viewLocation.length) === viewLocation;
    };
  });

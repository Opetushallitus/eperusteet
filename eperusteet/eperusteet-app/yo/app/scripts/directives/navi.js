'use strict';

angular.module('eperusteApp')
  .directive('eperusteNavi', function() {
    return {
      templateUrl: 'views/partials/navi.html',
      restrict: 'E',
      transclude: false
    };
  })
  .controller('NaviCtrl', function($rootScope, $scope, $location, Navigaatiopolku) {
    $scope.navigaatiopolku = [];
    $rootScope.$on('update:navipolku', function() {
      $scope.navigaatiopolku = Navigaatiopolku.hae();
    });
  });

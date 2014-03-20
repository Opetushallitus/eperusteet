'use strict';
/* global _ */

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
    $rootScope.$on('naviUpdate', function() {
      $scope.navigaatiopolku = _.map(Navigaatiopolku.haeNavipolku(), function(npo) {
        if (_.isObject(npo.arvo)) {
          npo.arvo = npo.arvo.fi;
        }
        return npo;
      });
    });
  });

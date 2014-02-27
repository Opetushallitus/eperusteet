'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('SpinnerService', function($rootScope, SPINNER_WAIT) {
    var pyynnot = 0;

    function enableSpinner() {
      ++pyynnot;
      $rootScope.$emit('event:spinner_on');
      _.delay(function() {
        if (pyynnot > 0) {
          $rootScope.$emit('event:spinner_on');
        }
      }, SPINNER_WAIT);
    }

    function disableSpinner() {
      --pyynnot;
      if (pyynnot === 0) {
        $rootScope.$emit('event:spinner_off');
      }
    }

    return {
      enable: enableSpinner,
      disable: disableSpinner,
      isSpinning: function() { return pyynnot > 0; }
    };
  })
  .directive('spinner', function($rootScope) {
    return {
      template: '<div id="global-spinner" ng-show="isSpinning">' +
                '  SPINNER' +
                '</div>',
      restrict: 'E',
      link: function($scope) {
        $scope.isSpinning = false;

        function spin(state) {
          $scope.isSpinning = state;
        }

        $rootScope.$on('event:spinner_on', function() {
          spin(true);
        });

        $rootScope.$on('event:spinner_off', function() {
          spin(false);
        });
      }
    };
  });

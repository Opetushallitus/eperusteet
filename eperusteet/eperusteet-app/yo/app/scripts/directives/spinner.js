'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('SpinnerService', function(SPINNER_WAIT, $rootScope, $timeout) {
    var pyynnot = 0;

    function enableSpinner() {
      ++pyynnot;
      $timeout(function() {
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
                '<span class="glyphicon glyphicon-refresh spin"></span>' +
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

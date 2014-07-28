/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

'use strict';

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
      isSpinning: function() {
        return pyynnot > 0;
      }
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

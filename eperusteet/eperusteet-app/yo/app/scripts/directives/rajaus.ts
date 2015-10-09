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

/**
 * Rajaus
 * callback: optional, called on change
 *           usage callback="mycallback()" or callback="mycallback(value)"
 * size: 'small' or default
 * placeholder: optional, string or {{expression}}
 */
angular.module('eperusteApp')
  .directive('rajaus', function () {
    return {
      templateUrl: 'views/partials/rajaus.html',
      restrict: 'EA',
      scope: {
        model: '=',
        placeholder: '@',
        callback: '&',
        classes: '@?',
        size: '@?'
      },
      controller: function ($scope) {
        $scope.changed = function () {
          $scope.callback({value: $scope.model});
        };
        $scope.applyClasses = function () {
          var classes = 'input-group rajauslaatikko';
          if ($scope.classes) {
            classes += (' ' + $scope.classes);
          }
          return classes;
        };
        $scope.clear = function ($event) {
          if ($event) {
            $event.preventDefault();
          }
          $scope.model = '';
          $scope.changed();
        };
      },
      link: function (scope, element, attrs) {
        attrs.$observe('placeholder', function (value) {
          scope.placeholderstring = value;
        });
      }
    };
  });

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
/* global _ */

angular.module('eperusteApp')
  .directive('formfield', function ($parse, Kaanna) {
    var uniqueId = 0;
    return {
      templateUrl: 'views/partials/formfield.html',
      restrict: 'E',
      scope: {
        model: '=',
        label: '@',
        type: '@',
        options: '=?',
        modelVar: '@',
        form: '=',
        min: '@',
        max: '@',
        name: '@'
      },
      link: function (scope, element, attrs) {
        scope.postfix = '';
        attrs.$observe('required', function(value) {
          if (value) { scope.postfix = '*'; }
        });
        scope.inputClasses = function () {
          var classes = [];
          if (scope.type !== 'checkbox') {
            classes.push('form-control');
          }
          return classes;
        };
        scope.inputElId = scope.label.replace(/ /g, '-') + '-' + uniqueId++;
        element.find('label').attr('for', scope.inputElId);

        _.forEach(scope.options, function(opt) {
          opt.label = Kaanna.kaanna(opt.label);
        });

        // Two-way binding with deep object hierarchies needs some tricks
        var getter = $parse(scope.modelVar);
        var setter = getter.assign;
        scope.input = {};
        scope.input.model = getter(scope.model);
        scope.isObject = _.isObject(scope.input.model);
        // inner => outside
        scope.$watch('input.model', function () {
          setter(scope.model, scope.input.model);
        });
        // outside => inner
        scope.$watch(function () {
          return getter(scope.model);
        }, function (value) {
          scope.input.model = value;
        });
      }
    };
  });

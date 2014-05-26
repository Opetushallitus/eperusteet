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
  .directive('formfield', function ($parse) {
    var uniqueId = 0;
    return {
      template: '<div class="form-group">' +
        '<label class="col-sm-3 control-label">{{label | kaanna}}{{ postfix }}</label>' +
        '<div class="input-group col-sm-9">' +
        '<input ng-if="!options && !isObject" ng-class="inputClasses()" ng-model="input.model" ng-change="updateModel()" type="{{type}}">' +
        '<span ng-if="!options && isObject">' +
        '  <ml-input ml-data="input.model" ng-model="input.model" ng-change="updateModel()"></ml-input>' +
        '</span>' +
        '<select ng-if="options" class="form-control" ng-model="input.model" ng-change="updateModel()"' +
        'ng-options="obj.value as obj.label for obj in options">' +
        '</select>' +
        '</div></div>',
      restrict: 'E',
      scope: {
        ngModel: '=',
        label: '@',
        type: '@',
        options: '=?',
        modelVar: '@'
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
        element.find('label').attr('for', scope.label + '-' + uniqueId);
        element.find('input').attr('id', scope.label + '-' + uniqueId++);

        // Two-way binding with deep object hierarchies needs some tricks
        var getter = $parse(scope.modelVar);
        scope.input = {};
        scope.input.model = getter(scope.ngModel);
        scope.isObject = _.isObject(scope.input.model);
        scope.updateModel = function() {
          getter.assign(scope.ngModel, scope.input.model);
        };
      }
    };
  });

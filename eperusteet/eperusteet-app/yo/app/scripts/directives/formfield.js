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

/**
 * Form field with label and input field.
 * @param {Object} model Model base object for input, e.g. a.b.c => a is model
 * @param {String} modelVar Suffix for model, e.g. a.b.c => b.c is modelVar
 * @param {String} label Label in the ui, will be translated
 * @param {String} type Input type text|number|checkbox|..., default text
 * @param {Array} options Options for selector, doesn't require type attribute
 *     Flat array: value will be raw value, displayed label is translated from raw value
 *     Object array: {value: VALUE, label: LABEL}, label is translated
  * @param {form} form Form object
 * @param {Integer} min Minimum value for number input
 * @param {Integer} max Maximum value for number input or max length for text
 * @param {String} name Field name
 * @param {String} placeholder Placeholder for input or select, will be translated
 * @param {String/Expression} required 'required'|'true'|parent scope expression
 */
angular.module('eperusteApp')
  .directive('formfield', function ($parse, Kaanna, $timeout, YleinenData) {
    var uniqueId = 0;
    var checkInputType = function (scope) {
      scope.isObject = _.isObject(scope.input.model);
        scope.isNumber = !scope.options && !scope.isObject &&
          (scope.type === 'number' || scope.type === 'float' || scope.type === 'integer');
        scope.isDate = !scope.options && scope.type === 'date';
        scope.isText = !scope.options && !scope.isObject &&
          !(scope.type === 'number' || scope.type === 'float' || scope.type === 'integer' || scope.type === 'label');
        scope.isMultiText = !scope.options && scope.isObject && scope.type !== 'label';
        scope.datePicker = {
          options: YleinenData.dateOptions,
          format: YleinenData.dateFormatDatepicker,
          state: false,
          open: function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            scope.datePicker.state = !scope.datePicker.state;
          }
        };
        scope.isLabel = scope.type === 'label';
    };
    return {
      templateUrl: 'views/partials/formfield.html',
      transclude: true,
      restrict: 'E',
      scope: {
        model: '=',
        label: '@',
        type: '@?',
        options: '=?',
        modelVar: '@',
        form: '=',
        min: '@?',
        max: '@?',
        name: '@',
        placeholder: '@',
        step: '@?'
      },
      link: function (scope, element, attrs) {
        scope.postfix = '';
        scope.type = scope.type || 'text';
        scope.flatOptions = _.isArray(scope.options) &&
                scope.options.length > 0 && !_.isObject(scope.options[0]);

        if (!scope.flatOptions) {
          _.forEach(scope.options, function(opt) {
            opt.label = Kaanna.kaanna(opt.label);
          });
        }

        scope.kaanna = function (val) {
          return Kaanna.kaanna(val);
        };

        scope.inputClasses = function () {
          var classes = [];
          if (scope.type !== 'checkbox') {
            classes.push('form-control');
          }
          return classes;
        };

        function bindLabel() {
          scope.inputElId = scope.label.replace(/ /g, '-') + '-' + uniqueId++;
          element.find('label').attr('for', scope.inputElId);
        }

        if ((scope.type === 'text' || scope.type === 'diaari') && attrs.max) {
          $timeout(function () {
            element.find('input').attr('maxlength', attrs.max);
          });
        }

        attrs.$observe('required', function(value) {
          if (value === 'required' || value === 'true' || value === '') {
            scope.postfix = '*';
            $timeout(function () {
              element.find('input').attr('required', '');
            });
          } else if (value) {
            var parsed = $parse(value);
            scope.$watch(function () {
              return parsed(scope.$parent);
            }, function (value) {
              scope.postfix = value ? '*' : '';
            });
          }
        });

        bindLabel();

        // Two-way binding with deep object hierarchies needs some tricks
        var getter = $parse(scope.modelVar);
        var setter = getter.assign;
        scope.input = {};
        scope.input.model = getter(scope.model);
        // inner => outside
        scope.$watch('input.model', function () {
          checkInputType(scope);
          if (scope.input && !_.isUndefined(scope.input.model)) {
            setter(scope.model, scope.input.model);
          }
        });
        // outside => inner
        scope.$watch(function () {
          return getter(scope.model);
        }, function (value) {
          checkInputType(scope);
          scope.input.model = value;
        });

        checkInputType(scope);
      }
    };
  });

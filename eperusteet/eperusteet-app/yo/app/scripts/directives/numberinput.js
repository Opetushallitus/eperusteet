'use strict';

/**
 * Number input field
 * can be of type:
 * 'number' => integers and floats accepted (native HTML5 input type number)
 * 'integer' => only integers accepted
 * 'float' => integers and floats accepted (dot or comma separated)
 */
angular.module('eperusteApp')
  .directive('numberinput', function ($timeout, $compile) {
    return {
      templateUrl: 'views/partials/numberinput.html',
      restrict: 'E',
      scope: {
        model: '=',
        min: '@?',
        max: '@?',
        luokka: '@',
        form: '=',
        labelId: '@',
        type: '@?',
        step: '@?'
      },
      replace: true,
      link: function (scope, element, attrs) {
        $timeout(function () {
          var input = element.find('input');
          if (scope.$parent.inputElId) {
            input.attr('id', scope.$parent.inputElId);
          }
          if (attrs.step && attrs.type === 'float') {
            input.attr('step-validate', attrs.step);
            $compile(element.contents())(scope);
          }
        });
      }
    };
  })

  .directive('validateInteger', function() {
    var INTEGER_PATTERN = /^\-?\d+$/;
    function isValid(value) {
      return !value || INTEGER_PATTERN.test(value);
    }
    return {
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function (viewValue) {
          var valid = isValid(viewValue);
          ctrl.$setValidity('integer', valid);
          return valid ? viewValue : undefined;
        });

        ctrl.$formatters.unshift(function(value) {
          ctrl.$setValidity('integer', isValid(value));
          return value;
        });
      }
    };
  })

  .directive('validateFloat', function() {
    var FLOAT_PATTERN = /^\-?\d+((\.|\,)\d+)?$/;
    function isValid(value) {
      return !value || FLOAT_PATTERN.test(value);
    }
    return {
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(viewValue) {
          var valid = isValid(viewValue);
          ctrl.$setValidity('float', valid);
          return valid ? (viewValue === '' ? viewValue : parseFloat(viewValue.replace(',', '.'))) : undefined;
        });

        ctrl.$formatters.unshift(function(value) {
          ctrl.$setValidity('float', isValid(value));
          return value;
        });
      }
    };
  })

  .directive('stepValidate', function() {
    function isValid(value, step) {
      return !value || value % step === 0;
    }
    return {
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        var stepValue = parseFloat(attrs.stepValidate);
        ctrl.$parsers.push(function(viewValue) {
          // Precondition: viewValue is either
          // undefined, empty string, or a valid float
          var valid = isValid(viewValue, stepValue);
          ctrl.$setValidity('step', valid);
          return valid ? viewValue : undefined;
        });

        ctrl.$formatters.push(function(value) {
          ctrl.$setValidity('step', isValid(value, stepValue));
          return value;
        });
      }
    };
  });

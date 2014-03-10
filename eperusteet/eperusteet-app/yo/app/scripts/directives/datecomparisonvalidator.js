'use strict';
/*global moment*/

angular.module('eperusteApp')
  .directive('dateComparisonValidator', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      scope: {
        vertailtavaKenttaNimi: '@dateComparisonName',
        aikaisempiAjankohta: '@dateComparisonEarlier'
      },
      link: function(scope, element, attrs, ctrl) {
             
        ctrl.$parsers.push(function(viewValue) {
          var form = element.inheritedData('$formController');
          scope.vertailtavaKentta = form[scope.vertailtavaKenttaNimi].$modelValue;

          if (scope.aikaisempiAjankohta === 'true') {
            if (moment(scope.vertailtavaKentta).isAfter(viewValue, 'day') || !scope.vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[scope.vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
              return viewValue;
            } else {
              ctrl.$setValidity('dateComparisonValidator', false);
              return viewValue;
            }
          } else {
            if (moment(viewValue).isAfter(scope.vertailtavaKentta, 'day') || !scope.vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[scope.vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
              return viewValue;
            } else {
              ctrl.$setValidity('dateComparisonValidator', false);
              return viewValue;
            }
          }
        });
      }
    };
  });

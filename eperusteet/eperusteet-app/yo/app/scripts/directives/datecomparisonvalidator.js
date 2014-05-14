'use strict';
/*global moment*/

angular.module('eperusteApp')
  .directive('dateComparisonValidator', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
//      scope: {
//        vertailtavaKenttaNimi: '@dateComparisonName',
//        aikaisempiAjankohta: '@dateComparisonEarlier'
//      },
      link: function(scope, element, attrs, ctrl) {

        var vertailtavaKenttaNimi = attrs.dateComparisonName;
        var aikaisempiAjankohta = attrs.dateComparisonEarlier;

        ctrl.$parsers.push(function(viewValue) {
          var form = element.inheritedData('$formController');
          var vertailtavaKentta = form[vertailtavaKenttaNimi].$modelValue;

          if (aikaisempiAjankohta === 'true') {
            if (moment(vertailtavaKentta).isAfter(viewValue, 'day') || !vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
              return viewValue;
            } else {
              ctrl.$setValidity('dateComparisonValidator', false);
              return viewValue;
            }
          } else {
            if (moment(viewValue).isAfter(vertailtavaKentta, 'day') || !vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
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

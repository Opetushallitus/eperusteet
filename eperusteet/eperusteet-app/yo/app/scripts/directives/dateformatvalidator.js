'use strict';
/*global moment*/

angular.module('eperusteApp')
  .directive('dateformatvalidator', function (YleinenData) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, ngModel) {

        var parsedMoment = '';

        ngModel.$parsers.unshift(function(viewValue) {
          return validate(viewValue);
        });
        
        ngModel.$formatters.unshift(function (viewValue) {
          return validate(viewValue);
        });
        
        function validate (viewValue) {

          if (viewValue instanceof Date || viewValue === '' || viewValue === null) {
            ngModel.$setValidity('dateformatvalidator', true);
            return viewValue;
          } else if (typeof viewValue === 'string') {
              parsedMoment = moment(viewValue, YleinenData.dateFormatMomentJS, true);
          } else if (typeof viewValue === 'number') {
              parsedMoment = moment(viewValue);
          } else {
            ngModel.$setValidity('dateformatvalidator', false);
            return undefined;
          }
                   
          if (parsedMoment.isValid()) {
            ngModel.$setValidity('dateformatvalidator', true);
            return viewValue;
          } else {
            ngModel.$setValidity('dateformatvalidator', false);
            return undefined;
          }
        }
        
      }
    };
  });

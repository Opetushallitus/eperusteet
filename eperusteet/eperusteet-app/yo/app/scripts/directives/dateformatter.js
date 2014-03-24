'use strict';
/*global moment*/

angular.module('eperusteApp')
  .directive('dateformatter', function (YleinenData) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, ctrl) {
        
        ctrl.$parsers.unshift(function(viewValue) {
          if (typeof viewValue === 'object' || viewValue === '') {
            ctrl.$setValidity('dateformatter', true);
            return viewValue;
          }
          
          var parsedMoment = moment(viewValue, YleinenData.dateFormatMomentJS, true);
          
          if (parsedMoment.isValid()) {
            ctrl.$setValidity('dateformatter', true);
            return parsedMoment.toDate();
          } else {
            ctrl.$setValidity('dateformatter', false);
            return viewValue;
          }
        });
      }
    };
  });

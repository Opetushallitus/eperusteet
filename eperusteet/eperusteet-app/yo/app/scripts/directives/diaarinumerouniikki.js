'use strict';
/*global _*/

angular.module('eperusteApp')
  .directive('diaarinumerouniikki', function(DiaarinumeroUniqueResource) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ngModel) {

        ngModel.$parsers.push(function(viewValue) {
          if (!_.isEmpty(viewValue)) {
            return validate(viewValue);
          } else {
            ngModel.$setValidity('diaarinumerouniikki', true);
            return viewValue;
          }
        });

        var validate = _.debounce(function(viewValue) {
          DiaarinumeroUniqueResource.get({
            diaarinumero: viewValue,
          }, function(vastaus) {
            if (vastaus.vastaus === true) {
              ngModel.$setValidity('diaarinumerouniikki', true);
              return viewValue;
            } else {
              ngModel.$setValidity('diaarinumerouniikki', false);
              return viewValue;
            }
          });
        }, 300);
      }
    };
  });

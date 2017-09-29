"use strict";
import * as _ from "lodash";

angular.module("eperusteApp").directive("diaarinumerouniikki", function(DiaarinumeroUniqueResource) {
    return {
        restrict: "A",
        require: "ngModel",
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(viewValue) {
                if (!_.isEmpty(viewValue)) {
                    validate(viewValue);
                } else {
                    ngModel.$setValidity("diaarinumerouniikki", true);
                }
                return viewValue;
            });

            function doValidate(viewValue) {
                DiaarinumeroUniqueResource.get(
                    {
                        diaarinumero: viewValue
                    },
                    function(vastaus) {
                        ngModel.$setValidity("diaarinumerouniikki", !vastaus.loytyi || vastaus.tila === "julkaistu");
                        // ngModel.$setValidity('diaarinumerouniikki', vastaus.loytyi && vastaus.tila === 'julkaistu');
                    }
                );
            }

            var validate = _.debounce(function(viewValue) {
                scope.$apply(function() {
                    doValidate(viewValue);
                });
            }, 300);
        }
    };
});

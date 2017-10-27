import * as angular from "angular";
import * as _ from "lodash";

angular.module("eperusteApp").directive("diaarinumerouniikki", function(DiaarinumeroUniqueResource) {
    return {
        restrict: "A",
        require: "ngModel",
        link: function(scope, element, attrs, ngModel) {
            (ngModel as any).$parsers.push(function(viewValue) {
                if (!_.isEmpty(viewValue)) {
                    validate(viewValue);
                } else {
                    (ngModel as any).$setValidity("diaarinumerouniikki", true);
                }
                return viewValue;
            });

            function doValidate(viewValue) {
                DiaarinumeroUniqueResource.get(
                    {
                        diaarinumero: viewValue
                    },
                    function(vastaus) {
                        (ngModel as any).$setValidity(
                            "diaarinumerouniikki",
                            !vastaus.loytyi || vastaus.tila === "julkaistu"
                        );
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

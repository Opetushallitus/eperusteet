import * as angular from "angular";

angular.module("eperusteApp").directive("sticky", () => {
    return {
        restrict: "A",
        link: (scope, element, attrs) => {
            $(element).sticky({
                topSpacing: attrs.topSpacing || 0,
                className: attrs.classname
            });
        }
    };
});

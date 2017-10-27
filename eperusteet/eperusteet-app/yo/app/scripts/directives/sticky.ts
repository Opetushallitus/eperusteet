import * as angular from "angular";

angular.module("eperusteApp").directive("sticky", () => {
    return {
        restrict: "A",
        link: (scope, element, attrs) => {
            if (($(element) as any).sticky) {
                ($(element) as any).sticky({
                    topSpacing: attrs.topSpacing || 0,
                    className: attrs.classname
                });
            }
        }
    };
});

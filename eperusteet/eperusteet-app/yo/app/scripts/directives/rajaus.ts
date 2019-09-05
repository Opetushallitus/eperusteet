import * as angular from "angular";
import _ from "lodash";

/**
 * Rajaus
 * callback: optional, called on change
 *           usage callback="mycallback()" or callback="mycallback(value)"
 * size: 'small' or default
 * placeholder: optional, string or {{expression}}
 */
angular.module("eperusteApp").directive("rajaus", function() {
    return {
        template: require("views/partials/rajaus.html"),
        restrict: "EA",
        scope: {
            model: "=",
            placeholder: "@",
            callback: "&",
            classes: "@?",
            size: "@?"
        },
        controller: function($scope) {
            $scope.changed = function() {
                $scope.callback({ value: $scope.model });
            };
            $scope.applyClasses = function() {
                let classes = "input-group rajauslaatikko";
                if ($scope.classes) {
                    classes += " " + $scope.classes;
                }
                return classes;
            };
            $scope.clear = function($event) {
                if ($event) {
                    $event.preventDefault();
                }
                $scope.model = "";
                $scope.changed();
            };
        },
        link: function(scope: any, element, attrs) {
            attrs.$observe("placeholder", function(value) {
                scope.placeholderstring = value;
            });
        }
    };
});

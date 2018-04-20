/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

/**
 * Number input field
 * can be of type:
 * 'number' => integers and floats accepted (native HTML5 input type number)
 * 'integer' => only integers accepted
 * 'float' => integers and floats accepted (dot or comma separated)
 */

import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .directive("numberinput", function($timeout, $compile) {
        return {
            template: require("views/partials/numberinput.html"),
            restrict: "E",
            scope: {
                model: "=",
                min: "@?",
                max: "@?",
                luokka: "@",
                form: "=",
                labelId: "@",
                type: "@?",
                step: "@?"
            },
            replace: true,
            link: function(scope: any, element: any, attrs: any) {
                $timeout(function() {
                    var input = element.find("input");
                    if (attrs.labelId) {
                        input.attr("id", attrs.labelId);
                    } else if (scope.$parent.inputElId) {
                        input.attr("id", scope.$parent.inputElId);
                    }
                    if (attrs.step && attrs.type === "float") {
                        input.attr("step-validate", attrs.step);
                        $compile(element.contents())(scope);
                    }
                });
            }
        };
    })
    .directive("validateInteger", function() {
        var INTEGER_PATTERN = /^\-?\d+$/;
        function isValid(value) {
            return !value || INTEGER_PATTERN.test(value);
        }
        return {
            require: "ngModel",
            link: function(scope, element, attrs, ctrl: any) {
                ctrl.$parsers.unshift(function(viewValue) {
                    var valid = isValid(viewValue);
                    ctrl.$setValidity("integer", valid);
                    return valid ? viewValue : undefined;
                });

                ctrl.$formatters.unshift(function(value) {
                    ctrl.$setValidity("integer", isValid(value));
                    return value;
                });
            }
        };
    })
    .directive("validateFloat", function() {
        var FLOAT_PATTERN = /^\-?\d+((\.|\,)\d+)?$/;
        function isValid(value) {
            return !value || FLOAT_PATTERN.test(value);
        }

        function validateMinMax(scope, value, attrs, ctrl) {
            if (typeof value === "number") {
                if (attrs.min) {
                    ctrl.$setValidity("min", value >= parseFloat(scope.min));
                }
                if (attrs.max) {
                    ctrl.$setValidity("max", value <= parseFloat(scope.max));
                }
            }
            if (value === undefined || value === "") {
                if (attrs.max) {
                    ctrl.$setValidity("max", true);
                }
                if (attrs.min) {
                    ctrl.$setValidity("min", true);
                }
            }
        }
        return {
            require: "ngModel",
            link: function(scope, element, attrs, ctrl: any) {
                ctrl.$parsers.unshift(function(viewValue) {
                    var valid = isValid(viewValue);
                    ctrl.$setValidity("float", valid);
                    var value;
                    if (valid) {
                        value = viewValue === "" ? viewValue : parseFloat(viewValue.replace(",", "."));
                    }
                    validateMinMax(scope, value, attrs, ctrl);
                    return value;
                });

                ctrl.$formatters.unshift(function(value) {
                    ctrl.$setValidity("float", isValid(value));
                    return value;
                });
            }
        };
    })
    .directive("stepValidate", function() {
        function isValid(value, step) {
            return !value || ((value * 100) % (step * 100)) / 100 === 0;
        }
        return {
            require: "ngModel",
            link: function(scope, element, attrs: any, ctrl: any) {
                var stepValue = parseFloat(attrs.stepValidate);
                ctrl.$parsers.push(function(viewValue) {
                    // Precondition: viewValue is either
                    // undefined, empty string, or a valid float
                    var valid = isValid(viewValue, stepValue);
                    ctrl.$setValidity("step", valid);
                    return valid ? viewValue : undefined;
                });

                ctrl.$formatters.push(function(value) {
                    ctrl.$setValidity("step", isValid(value, stepValue));
                    return value;
                });
            }
        };
    });

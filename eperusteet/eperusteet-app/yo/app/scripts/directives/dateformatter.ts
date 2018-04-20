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

import _ from "lodash";
import * as angular from "angular";
import moment from "moment";

angular
    .module("eperusteApp")
    .directive("dateformatter", function(YleinenData) {
        return {
            restrict: "A",
            require: "ngModel",
            link: function(scope, element, attrs, ctrl: any) {
                ctrl.$parsers.unshift(function(viewValue) {
                    if (typeof viewValue === "object" || viewValue === "") {
                        ctrl.$setValidity("dateformatter", true);
                        return viewValue;
                    }

                    var parsedMoment = moment(viewValue, YleinenData.dateFormatMomentJS, true);

                    if (parsedMoment.isValid()) {
                        ctrl.$setValidity("dateformatter", true);
                        return parsedMoment.toDate();
                    } else {
                        ctrl.$setValidity("dateformatter", false);
                        return viewValue;
                    }
                });
            }
        };
    })
    .directive("pvm", function(Kaanna) {
        return {
            restrict: "A",
            link: function(scope, element: any, attrs: any) {
                scope.$watch(attrs.pvm, function(value: any) {
                    if (!value) {
                        element.text(Kaanna.kaanna("ei-asetettu"));
                        return;
                    }
                    var date = new Date(value);
                    element.text(moment(date).format("D.M.YYYY"));
                });
            }
        };
    });

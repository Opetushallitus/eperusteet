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

import * as _ from "lodash";
import * as angular from "angular";
import moment from "moment";

angular.module("eperusteApp").directive("dateformatvalidator", function(YleinenData) {
    return {
        restrict: "A",
        require: "ngModel",
        link: function(scope, element, attrs, ngModel: any) {
            var parsedMoment: any;

            ngModel.$parsers.unshift(function(viewValue) {
                return validate(viewValue);
            });

            ngModel.$formatters.unshift(function(viewValue) {
                return validate(viewValue);
            });

            function validate(viewValue) {
                if (viewValue instanceof Date || viewValue === "" || viewValue === null || viewValue === undefined) {
                    ngModel.$setValidity("dateformatvalidator", true);
                    return viewValue;
                } else if (typeof viewValue === "string") {
                    parsedMoment = moment(viewValue, YleinenData.dateFormatMomentJS, true);
                } else if (typeof viewValue === "number") {
                    parsedMoment = moment(viewValue);
                } else {
                    ngModel.$setValidity("dateformatvalidator", false);
                    return undefined;
                }

                if (parsedMoment.isValid()) {
                    ngModel.$setValidity("dateformatvalidator", true);
                    return viewValue;
                } else {
                    ngModel.$setValidity("dateformatvalidator", false);
                    return undefined;
                }
            }
        }
    };
});

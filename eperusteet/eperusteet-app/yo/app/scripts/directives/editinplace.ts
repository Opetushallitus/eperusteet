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

import * as angular from "angular";
import * as _ from "lodash";

angular.module("eperusteApp").directive("editInPlace", function($compile, $parse, Kaanna, $document) {
    return {
        restrict: "A",
        scope: {
            editInPlace: "="
        },
        link: function(scope, element) {
            scope.editing = false;
            var readOnlyEl = angular.element("<span></span>");
            readOnlyEl
                .attr("ng-show", "!editing")
                .attr("ng-click", "edit()")
                .text('{{editInPlace || "nimetön" | kaanna }}');
            var inputEl = angular.element("<input>");
            inputEl
                .attr("ng-show", "editing")
                .attr("ng-model", "editInPlace")
                .attr("slocalized", "")
                .css({
                    "font-size": element.css("font-size"),
                    display: "inline-block",
                    height: parseInt(element.css("line-height"), 10) * 1.2
                });

            element.append(readOnlyEl);
            element.append(inputEl);
            $compile(element.contents())(scope);
            $document.on("click", function(event) {
                if (element.find(event.target).length > 0) {
                    return;
                }
                scope.editing = false;
                scope.$apply();
            });

            scope.edit = function() {
                var fakeEl = angular
                    .element("<div>")
                    .hide()
                    .text(readOnlyEl.text());
                element.append(fakeEl);
                var width = fakeEl.width();
                fakeEl.remove();
                inputEl.css("width", Math.min(Math.max(parseInt(width, 10) + 100, 400), 800));
                scope.editing = true;
            };
        }
    };
});

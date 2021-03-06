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
import _ from "lodash";

/**
 * Override pagination template to get glyphs as first/last icons.
 */
angular.module("template/pagination/pagination.html", []).run([
    "$templateCache",
    function($templateCache) {
        $templateCache.put(
            "template/pagination/pagination.html",
            '<ul class="pagination">\n' +
                '  <li ng-if="boundaryLinks" ng-class="{disabled: noPrevious()}"><a href ng-click="selectPage(1)" icon-role="first">{{getText(\'first\')}}</a></li>\n' +
                '  <li ng-if="directionLinks" ng-class="{disabled: noPrevious()}"><a href ng-click="selectPage(page - 1)" icon-role="previous">{{getText(\'previous\')}}</a></li>\n' +
                '  <li ng-repeat="page in pages track by $index" ng-class="{active: page.active}"><a href ng-click="selectPage(page.number)">{{page.text}}</a></li>\n' +
                '  <li ng-if="directionLinks" ng-class="{disabled: noNext()}"><a href ng-click="selectPage(page + 1)" icon-role="next">{{getText(\'next\')}}</a></li>\n' +
                '  <li ng-if="boundaryLinks" ng-class="{disabled: noNext()}"><a href ng-click="selectPage(totalPages)" icon-role="last">{{getText(\'last\')}}</a></li>\n' +
                "</ul>"
        );
    }
]);

// startFrom filter for easy pagination
angular.module("eperusteApp").filter("startFrom", function() {
    return function(input, start) {
        if (!input) {
            return input;
        }
        return input.slice(start);
    };
});

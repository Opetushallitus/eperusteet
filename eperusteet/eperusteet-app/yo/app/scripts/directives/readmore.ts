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

angular.module("eperusteApp").directive("readmore", function($compile, $timeout) {
    return {
        restrict: "AE",
        scope: {
            maxheight: "@"
        },
        controller: function($scope) {
            $scope.collapsed = true;
            $scope.needsCollapser = false;
        },
        link: function(scope, element) {
            // Todo: Korjaa direktiivi, sillä nämä watcherit aiheuttavat infinite digestin
            /*
        var maxheight = parseInt(scope.maxheight, 10) || 100;
        var togglerEl = angular.element(
          '<a class="action-link readmore-toggler" ng-show="needsCollapser" ng-click="collapsed = !collapsed">' +
          '{{collapsed && \'lue-lisaa\' || \'piilota\' | kaanna}}</a>');
        var toggler = $compile(togglerEl)(scope);
        element.append(toggler);
        var child = element.children().eq(0);

        scope.$watch(function () {
          scope.elHeight = element[0].offsetHeight;
        });
        scope.$watch('elHeight', function (value) {
          scope.needsCollapser = value > maxheight;
        });
        scope.$watch('needsCollapser', function (value) {
          if (value) {
            child.addClass('readmore-collapsable');
            child.css('height', maxheight);
          } else {
            child.removeClass('readmore-collapsable');
            child.css('height', 'auto');
          }
        });
        scope.$watch('collapsed', function (value) {
          if (!scope.needsCollapser) {
            return;
          }
          if (value) {
            child.css('height', maxheight);
          } else {
            child.css('height', child[0].scrollHeight);
          }
        });

        $timeout(function () {
          element.hide().show(0); // webkit: force reflow so height returns correct value
          scope.elHeight = element.height();
        });
        */
        }
    };
});

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

'use strict';

/**
 * Ohje
 * Ikoni tooltipille ja siihen liittyvä kelluva ohjeteksti.
 * Tukee sekä hiirtä (mouse enter) että touch/click-eventtejä.
 *
  * suunta: left|right(default)|top|bottom
 */
angular.module('eperusteApp')
  .directive('ohje', function ($timeout, $compile, $document) {
    return {
      templateUrl: 'views/partials/ohje.html',
      restrict: 'EA',
      scope: {
        teksti: '@',
        suunta: '@?'
      },
      link: function (scope, element) {
        var el = element.find('.badge');

        scope.show = function (visible) {
          var opening = angular.isUndefined(visible) || visible;
          $timeout(function () {
            el.trigger(opening ? 'show' : 'hide');
            if (opening) {
              var title = element.find('.popover-title');
              var closer = $compile(angular.element(
                '<span class="closer pull-right" ng-click="show(false)">&#x2715;</span>'))(scope);
              title.append(closer);
            }
          });
        };

        // Click anywhere to close
        $document.on('click', function (event) {
          if (element.find(event.target).length > 0) {
            return;
          }
          scope.show(false);
          scope.$apply();
        });
      }
    };
  });

// Modify popover template for binding unsafe html
angular.module('template/popover/popover.html', []).run(['$templateCache', function ($templateCache) {
  $templateCache.put('template/popover/popover.html',
      '<div class="popover {{placement}}" ng-class="{ in: isOpen(), fade: animation() }">\n' +
      '  <div class="arrow"></div>\n' +
      '\n' +
      '  <div class="popover-inner">\n' +
      '      <h3 class="popover-title" ng-bind-html="title | unsafe" ng-show="title"></h3>\n' +
      '      <div class="popover-content" ng-bind-html="content | unsafe"></div>\n' +
      '  </div>\n' +
      '</div>\n' +
      '');
}]);

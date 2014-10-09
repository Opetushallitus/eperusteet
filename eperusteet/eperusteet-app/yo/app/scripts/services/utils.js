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
/* global _ */


angular.module('eperusteApp')
  .service('Utils', function($window, YleinenData) {
    this.scrollTo = function (selector) {
      var element = angular.element(selector);
      if (element.length) {
        $window.scrollTo(0, element.eq(0).offset().top);
      }
    };

    this.hasLocalizedText = function (field) {
      if (!_.isObject(field)) {
        return false;
      }
      var hasContent = false;
      var langs = _.values(YleinenData.kielet);
      _.each(langs, function (key) {
        if (!_.isEmpty(field[key])) {
          hasContent = true;
        }
      });
      return hasContent;
    };

    this.supportsFileReader = function () {
      return !_.isUndefined($window.FormData);
    };
  })

  /* Shows "back to top" link when scrolled beyond cutoff point */
  .directive('backtotop', function ($window, $document, Utils) {
    var CUTOFF_PERCENTAGE = 33;

    return {
      restrict: 'AE',
      scope: {},
      template: '<div id="backtotop" ng-hide="hidden" title="{{\'Takaisin ylös\' | kaanna}}">' +
        '<a class="action-link" icon-role="arrow-up" ng-click="backToTop()"></a></div>',
      link: function (scope) {
        var active = true;
        scope.backToTop = function () {
          Utils.scrollTo('#ylasivuankkuri');
        };

        scope.hidden = true;
        var window = angular.element($window);
        var document = angular.element($document);
        var scroll = function () {
          var fitsOnScreen = document.height() <= window.height()*1.5;
          var scrollDistance = document.height() - window.height();
          var inTopArea = window.scrollTop() < scrollDistance * CUTOFF_PERCENTAGE / 100;
          scope.$apply(function () {
            scope.hidden = !active || fitsOnScreen || inTopArea;
          });
        };
        window.on('scroll', scroll);
        // Disable when in edit mode
        scope.$on('enableEditing', function () {
          active = false;
        });
        scope.$on('disableEditing', function () {
          active = true;
        });
        scope.$on('$destroy', function() {
          window.off('scroll', scroll);
        });
      }
    };
  });

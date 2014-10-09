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
  .service('Kaanna', function($translate) {
    return {
      kaanna: function(input, config) {
        var lang = $translate.use() || $translate.preferredLanguage();

        if (_.isObject(input)) {
          return input[lang] || input['kieli_' + lang + '#1'];
        } else if (_.isString(input)) {
          return $translate.instant(input, config);
        }
        return '';
      }
    };
  })
  .directive('kaanna', function(Kaanna, $compile, IconMapping) {
    function getAttr(attr, scope) {
      if (!_.isString(attr) || _.size(attr) === 0) {
        return;
      }
      return scope.$eval(attr) || attr;
    }
    return {
      restrict: 'A',
      link: function (scope, el, attrs) {
        var original = getAttr(attrs.kaanna, scope) || el.text();
        if (_.isObject(original)) {
          el.text(Kaanna.kaanna(original));
          if (attrs.iconRole) {
            IconMapping.addIcon(attrs.iconRole, el);
          }
          scope.$watch(function () {
            return getAttr(attrs.kaanna, scope);
          }, function (value) {
            el.text(Kaanna.kaanna(value));
          });
        } else {
          var textEl = angular.element('<span>').attr('translate', original);
          if (attrs.kaannaValues) {
            textEl.attr('translate-values', attrs.kaannaValues);
          }
          el.html('').append(textEl);
          if (attrs.iconRole) {
            var iconEl = angular.element('<span>').attr('icon-role', attrs.iconRole);
            el.removeAttr('icon-role');
            el.prepend(iconEl);
          }
          el.removeAttr('kaanna');
          el.removeAttr('kaanna-values');
          $compile(el.contents())(scope);
        }
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

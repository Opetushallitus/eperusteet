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
  .service('Kaanna', function($translate, Lokalisointi) {
    return {
      kaanna: function(input, config) {
        var lang = $translate.use() || $translate.preferredLanguage();

        if (_.isObject(input)) {
          return input[lang] || input['kieli_' + lang + '#1'];
        }
        else if (_.isString(input)) {
          return Lokalisointi.hae(input) || $translate.instant(input, config);
        }
        else {
          return '';
        }
      }
    };
  })
  .directive('kaanna', function(Kaanna) {
    function getAttr(attr, scope) {
      if (!_.isString(attr) || _.size(attr) === 0) {
        return;
      }
      return scope.$eval(attr) || attr;
    }
    return {
      restrict: 'A',
      link: function(scope, el, attrs) {
        var translated = '';
        var original = getAttr(attrs.kaanna, scope) || el.text();
        if (attrs.kaannaValues) {
          translated = Kaanna.kaanna(original, scope.$eval(attrs.kaannaValues));
        } else {
          translated = Kaanna.kaanna(original);
        }
        el.text(translated);
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

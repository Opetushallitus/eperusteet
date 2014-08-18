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
  .service('Utils', function($window) {
    this.scrollTo = function (selector) {
      var element = angular.element(selector);
      if (element.length) {
        $window.scrollTo(0, element[0].offsetTop);
      }
    };

    this.hasLocalizedText = function (field) {
      if (!_.isObject(field)) {
        return false;
      }
      var hasContent = false;
      _.each(field, function (value) {
        if (!_.isEmpty(value)) {
          hasContent = true;
        }
      });
      return hasContent;
    };
  });

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

angular.module('eperusteApp')

  /**
   * Prepends a glyphicon to the element, see mapping in IconMapping.
   */
  .directive('iconRole', function (IconMapping) {
    return {
      restrict: 'A',
      compile: function() {
        return function postLink(scope, element, attrs) {
          if (attrs.kaanna) {
            return;
          }
          IconMapping.addIcon(attrs.iconRole, element);
        };
      }
    };
  })
  .service('IconMapping', function () {
    this.addIcon = function (key, el) {
      var iconEl = this.getIconEl(key);
      if (el.text()) {
        el.addClass('iconlink');
      }
      el.prepend(iconEl);
    };
    this.getIconEl = function (key) {
      var suffix = this.icons[key] || key;
      return angular.element('<span>')
            .addClass('glyphicon').addClass('glyphicon-' + suffix);
    };
    this.icons = {
      add: 'plus',
      back: 'chevron-left',
      download: 'download-alt',
      drag: 'resize-vertical',
      edit: 'pencil',
      first: 'fast-backward',
      forward: 'chevron-right',
      import: 'cloud-download',
      info: 'info-sign',
      last: 'fast-forward',
      minus: 'minus',
      next: 'forward',
      previous: 'backward',
      remove: 'remove',
      save: 'save',
      search: 'search',
      settings: 'cog',
    };
  });


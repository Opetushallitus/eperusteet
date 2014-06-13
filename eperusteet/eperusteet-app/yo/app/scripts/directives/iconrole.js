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
      link: function (scope, element, attrs) {
        var suffix = IconMapping.icons[attrs.iconRole] || attrs.iconRole;
        var iconEl = angular.element('<span>')
          .addClass('glyphicon').addClass('glyphicon-' + suffix);
        element.addClass('iconlink').prepend(iconEl);
      }
    };
  })

  .service('IconMapping', function () {
    this.icons = {
      add: 'plus',
      back: 'chevron-left',
      edit: 'pencil',
      first: 'fast-backward',
      import: 'cloud-download',
      info: 'info-sign',
      last: 'fast-forward',
      minus: 'minus',
      next: 'forward',
      previous: 'backward',
      remove: 'remove',
      save: 'cloud-upload',
      settings: 'cog'
    };
  });


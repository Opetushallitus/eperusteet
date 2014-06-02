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
/* global $ */

angular.module('eperusteApp')
  .directive('expandableTa', function() {
    return {
      template: '<textarea style="overflow: hidden" class="form-control" id="expandableTextArea"></textarea>',
      restrict: 'E',
      required: 'ngModel',
      replace: true,
      scope: {
        ngModel: '=',
      },
      link: function() {
        // http://stackoverflow.com/questions/2948230/auto-expand-a-textarea-using-jquery
        $('#expandableTextArea').keyup(function() {
          while($(this).outerHeight() < this.scrollHeight + parseFloat($(this).css('borderTopWidth')) + parseFloat($(this).css('borderBottomWidth'))) {
              $(this).height($(this).height()+1);
          }
        });
      }
    };
  });

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
/*global moment*/

angular.module('eperusteApp')
  .directive('dateComparisonValidator', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
//      scope: {
//        vertailtavaKenttaNimi: '@dateComparisonName',
//        aikaisempiAjankohta: '@dateComparisonEarlier'
//      },
      link: function(scope, element, attrs, ctrl) {

        var vertailtavaKenttaNimi = attrs.dateComparisonName;
        var aikaisempiAjankohta = attrs.dateComparisonEarlier;

        ctrl.$parsers.push(function(viewValue) {
          var form = element.inheritedData('$formController');
          var vertailtavaKentta = form[vertailtavaKenttaNimi].$modelValue;

          if (aikaisempiAjankohta === 'true') {
            if (moment(vertailtavaKentta).isAfter(viewValue, 'day') || !vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
              return viewValue;
            } else {
              ctrl.$setValidity('dateComparisonValidator', false);
              return viewValue;
            }
          } else {
            if (moment(viewValue).isAfter(vertailtavaKentta, 'day') || !vertailtavaKentta ||  !viewValue) {
              ctrl.$setValidity('dateComparisonValidator', true);
              form[vertailtavaKenttaNimi].$setValidity('dateComparisonValidator', true);
              return viewValue;
            } else {
              ctrl.$setValidity('dateComparisonValidator', false);
              return viewValue;
            }
          }
        });
      }
    };
  });

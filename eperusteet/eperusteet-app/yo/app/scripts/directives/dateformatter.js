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
  .directive('dateformatter', function (YleinenData) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, element, attrs, ctrl) {

        ctrl.$parsers.unshift(function(viewValue) {
          if (typeof viewValue === 'object' || viewValue === '') {
            ctrl.$setValidity('dateformatter', true);
            return viewValue;
          }

          var parsedMoment = moment(viewValue, YleinenData.dateFormatMomentJS, true);

          if (parsedMoment.isValid()) {
            ctrl.$setValidity('dateformatter', true);
            return parsedMoment.toDate();
          } else {
            ctrl.$setValidity('dateformatter', false);
            return viewValue;
          }
        });
      }
    };
  });

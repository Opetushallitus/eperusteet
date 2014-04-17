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
/*global _*/

angular.module('eperusteApp')
  .directive('kenttalistaus', function($q, MuokkausUtils) {
    return {
      templateUrl: 'views/partials/muokkaus/kenttalistaus.html',
      restrict: 'E',
      transclude: true,
      scope: {
        fields: "=",
        objectPromise: "="
      },
      link: function(scope, element, attrs) {

        scope.innerObjectPromise = scope.objectPromise.then(function(object) {
          setInnerObjectPromise();
        });

        scope.$watch('objectPromise', function() {
          setInnerObjectPromise();
        });

        scope.removeField = function(fieldToRemove) {
          _.remove(scope.visibleFields, fieldToRemove);
          scope.hiddenFields.push(fieldToRemove);
        };

        scope.addFieldToVisible = function(field) {
          _.remove(scope.hiddenFields, field);
          scope.visibleFields.push(field);
        };

        scope.isEmpty = function(object) {
          return _.isEmpty(object);
        };

        function splitFields(object) {
          scope.visibleFields = _.filter(scope.fields, function(field) {
            return field.mandatory || MuokkausUtils.hasValue(object, field.path);
          });
          scope.hiddenFields = _.difference(scope.fields, scope.visibleFields);
        }

        function setInnerObjectPromise() {
          scope.innerObjectPromise = scope.objectPromise.then(function(object) {
            splitFields(object);
            return object;
          });
        }
      }
    };
  });
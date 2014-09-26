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
  .directive('kenttalistaus', function($q, MuokkausUtils, $timeout) {
    return {
      templateUrl: 'views/partials/muokkaus/kenttalistaus.html',
      restrict: 'E',
      transclude: true,
      scope: {
        fields: '=',
        objectPromise: '=',
        editEnabled: '='
      },
      link: function(scope, element) {
        scope.updateContentTip = function () {
          scope.noContent = element.find('ul.muokkaus').children().length === 0;
        };
        $timeout(function () {
          scope.updateContentTip();
        }, 3000);
      },
      controller: function ($scope) {
        $scope.noContent = false;
        $scope.expandedFields = $scope.fields;
        $scope.removeField = function(fieldToRemove) {
          fieldToRemove.visible = false;
        };

        $scope.$watch('objectPromise', function() {
          setInnerObjectPromise();
        });

        $scope.innerObjectPromise = $scope.objectPromise.then(function() {
          setInnerObjectPromise();
        });

        function splitFields(object) {
          $scope.expandedFields = [];
          _.each($scope.fields, function (field) {
            var parts = field.path.split('[');
            if (parts.length === 2) {
              // Expand array to individual fields
              _.each(object[parts[0]], function (item, index) {
                var newfield = angular.copy(field);
                newfield.path = parts[0] + '[' + index + parts[1];
                newfield.localeKey = item[field.localeKey];
                newfield.visible = true;
                $scope.expandedFields.push(newfield);
              });
            } else {
              field.inMenu = field.path !== 'nimi' && field.path !== 'koodiUri';
              field.visible = field.mandatory || MuokkausUtils.hasValue(object, field.path);
              $scope.expandedFields.push(field);
            }
          });
          $scope.updateContentTip();
        }

        function setInnerObjectPromise() {
          $scope.innerObjectPromise = $scope.objectPromise.then(function(object) {
            splitFields(object);
            return object;
          });
        }
      }
    };
  })

  .factory('ArviointiHelper', function () {
    var remove = function (arr, str) {
      var index = _.findIndex(arr, {path: str});
      if (index >= 0) {
        arr.splice(index, 1);
      }
    };

    /**
     * Arviointi voi olla tekstinä tai taulukkona (mutta ei kumpanakin).
     * Helper hanskaa mitä näytetään kenttälistauksessa ja "lisää osio"-menussa
     */
    function Helper() {
      var TAULUKKO_PATH = 'arviointi.arvioinninKohdealueet';
      var TEKSTI_PATH = 'arviointi.lisatiedot';
      var self = {};
      self.obj = {};

      self.hasTeksti = function () {
        return self.obj.teksti && self.obj.teksti.visible;
      };

      self.hasTaulukko = function () {
        return self.obj.taulukko && self.obj.taulukko.visible;
      };

      self.initFromFields = function (fields) {
        var obj = {teksti: null, taulukko: null};
        _.each(fields, function (field) {
          if (field.path === TAULUKKO_PATH) {
            obj.taulukko = field;
          } else if (field.path === TEKSTI_PATH) {
            obj.teksti = field;
          }
        });
        if (self.hasTeksti()) {
          obj.taulukko.visible = false;
        }
        self.obj = obj;
        return self.obj;
      };

      self.setMenu = function (menu) {
        if (self.exists()) {
          remove(menu, TAULUKKO_PATH);
          remove(menu, TEKSTI_PATH);
        }
      };

      self.exists = function () {
        return self.hasTeksti() || self.hasTaulukko();
      };

      return self;
    }

    return {
      create: function () {
        return new Helper();
      }
    };
  });

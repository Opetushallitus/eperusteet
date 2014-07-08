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
  .directive('kenttalistaus', function($q, MuokkausUtils, ArviointiHelper, $timeout, $window) {
    return {
      templateUrl: 'views/partials/muokkaus/kenttalistaus.html',
      restrict: 'E',
      transclude: true,
      scope: {
        fields: '=',
        objectPromise: '=',
        editEnabled: '='
      },
      link: function(scope) {
        scope.menuItems = [];

        scope.innerObjectPromise = scope.objectPromise.then(function() {
          setInnerObjectPromise();
        });

        scope.$watch('objectPromise', function() {
          setInnerObjectPromise();
        });

        scope.removeField = function(fieldToRemove) {
          fieldToRemove.visible = false;
        };

        function scrollTo(selector) {
          var element = angular.element(selector);
          if (element.length) {
            $window.scrollTo(0, element[0].offsetTop);
          }
        }

        scope.addFieldToVisible = function(field) {
          field.visible = true;
          // Varmista että menu sulkeutuu klikin jälkeen
          $timeout(function () {
            angular.element('h1').click();
            scrollTo('li[otsikko='+field.localeKey+']');
          });
        };

        /**
         * Palauttaa true jos kaikki mahdolliset osiot on jo lisätty
         */
        scope.allVisible = function() {
          var lisatty = _.all(scope.fields, function (field) {
            return (_.contains(field.path, 'arviointi.') ||
                    !field.inMenu ||
                    (field.inMenu && field.visible));
          });
          return lisatty && scope.arviointiHelper.exists();
        };

        scope.updateMenu = function () {
          scope.menuItems = _.reject(scope.fields, 'mandatory');
          if (scope.arviointiHelper) {
            scope.arviointiHelper.setMenu(scope.menuItems);
          }
        };

        scope.$watch('arviointiFields.teksti.visible', scope.updateMenu);
        scope.$watch('arviointiFields.taulukko.visible', scope.updateMenu);

        function splitFields(object) {
          _.each(scope.fields, function (field) {
            field.inMenu = field.path !== 'nimi' && field.path !== 'koodiUri';
            field.visible = field.mandatory || MuokkausUtils.hasValue(object, field.path);
          });
          if (!scope.arviointiHelper) {
            scope.arviointiHelper = ArviointiHelper.create();
          }
          scope.arviointiFields = scope.arviointiHelper.initFromFields(scope.fields);
          scope.updateMenu();
        }

        function setInnerObjectPromise() {
          scope.innerObjectPromise = scope.objectPromise.then(function(object) {
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
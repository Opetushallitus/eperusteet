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
  .directive('kenttalistaus', function($q, MuokkausUtils, $timeout, FieldSplitter) {
    return {
      templateUrl: 'views/partials/muokkaus/kenttalistaus.html',
      restrict: 'E',
      transclude: true,
      scope: {
        fields: '=',
        objectPromise: '=',
        editEnabled: '=',
        mode: '@?',
        hideEmptyPlaceholder: '@?'
      },
      link: function(scope, element) {
        scope.updateContentTip = function () {
          $timeout(function () {
            scope.noContent = element.find('ul.muokkaus').children().length === 0;
          });
        };
        scope.updateContentTip();
        scope.$watch('editEnabled', function () {
          scope.updateContentTip();
        });
        scope.$watch('fields', function () {
          scope.updateContentTip();
        }, true);
      },
      controller: function ($scope) {
        var model;
        $scope.noContent = false;
        $scope.expandedFields = $scope.fields;

        $scope.removeField = function(fieldToRemove) {
          var splitfield = FieldSplitter.process(fieldToRemove);
          if (splitfield.isMulti()) {
            splitfield.remove(model);
          } else {
            fieldToRemove.visible = false;
            fieldToRemove.$added = false;
          }
          setInnerObjectPromise();
        };

        $scope.getClass = FieldSplitter.getClass;

        $scope.hasEditableTitle = function (field) {
          return _.has(field, 'titleplaceholder');
        };

        function setInnerObjectPromise() {
          $scope.innerObjectPromise = $scope.objectPromise.then(function(object) {
            splitFields(object);
            model = object;
            return object;
          });
        }

        $scope.$watch('objectPromise', setInnerObjectPromise);
        $scope.$on('osafield:update', setInnerObjectPromise);

        $scope.innerObjectPromise = $scope.objectPromise.then(function() {
          setInnerObjectPromise();
        });

        function splitFields(object) {
          $scope.expandedFields = [];
          _.each($scope.fields, function (field) {
            var splitfield = FieldSplitter.process(field);
            if (splitfield.isMulti() && splitfield.needsSplit()) {
              // Expand array to individual fields
              splitfield.each(object, function (item, index) {
                var newfield = angular.copy(field);
                newfield.path = splitfield.getPath(index);
                newfield.localeKey = item[field.localeKey];
                newfield.originalLocaleKey = field.localeKey;
                newfield.visible = true;
                if (field.isolateEdit && index === field.$setEditable) {
                  newfield.$editing = true;
                  delete field.$setEditable;
                }
                $scope.expandedFields.push(newfield);
              });
            } else {
              field.inMenu = field.path !== 'nimi' && field.path !== 'koodiUri';
              field.visible = field.divider ? false :
                (field.$added || field.mandatory || MuokkausUtils.hasValue(object, field.path));
              $scope.expandedFields.push(field);
            }
          });
          $scope.updateContentTip();
        }
      }
    };
  })

  .service('FieldSplitter', function () {
    function getCssClass(path) {
      return path.replace(/\[/, '').replace(/\]/, '').replace(/\./, '');
    }

    function SplitField(data) {
      this.original = data;
      this.parts = [];
    }

    SplitField.prototype.split = function () {
      if (!this.original.path) {
        return;
      }
      this.parts = this.original.path.split('[');
      var index = this.original.path.match(/\[(\d+)\]/);
      this.index = index ? index[1] : null;
    };

    SplitField.prototype.isMulti = function () {
      return this.parts.length === 2;
    };

    SplitField.prototype.needsSplit = function () {
      return this.index === null;
    };

    SplitField.prototype.each = function (obj, cb) {
      return _.each(this.getObject(obj), cb);
    };

    SplitField.prototype.getPath = function (index) {
      return this.parts[0] + '[' + index + this.parts[1];
    };

    SplitField.prototype.getObject = function (obj) {
      return obj[this.parts[0]];
    };

    SplitField.prototype.addArrayItem = function (obj) {
      // TODO oletettu tekstikappale
      var object = this.getObject(obj);
      object.push({nimi: {}, teksti: {}});
      return object.length - 1;
    };

    SplitField.prototype.remove = function (obj) {
      var index = parseInt(this.parts[1], 10);
      obj[this.parts[0]].splice(index, 1);
    };

    SplitField.prototype.getClass = function (index) {
      return getCssClass(this.getPath(index));
    };

    this.process = function (field) {
      var obj = new SplitField(field);
      obj.split();
      return obj;
    };

    this.getClass = function (field) {
      return getCssClass(field.path);
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

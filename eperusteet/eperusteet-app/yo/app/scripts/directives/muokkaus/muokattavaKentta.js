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
  .directive('muokkauskenttaRaamit', function(Utils) {
    return {
      templateUrl: 'views/partials/muokkaus/muokattavaKentta.html',
      restrict: 'A',
      transclude: true,
      scope: {
        model: '=',
        piilotaOtsikko: '@?',
        field: '='
      },
      link: function(scope, element, attrs) {
        scope.otsikko = _.isString(scope.model) ? 'muokkaus-' + scope.model + '-header' : scope.model;
        scope.hasModel = !_.isString(scope.model);
        element.addClass('list-group-item ');
        element.attr('ng-class', '');

        scope.canCollapse = attrs.collapsible || false;
        scope.collapsed = false;
        scope.isEmpty = function (model) {
          return !Utils.hasLocalizedText(model);
        };
        scope.$watch('field.$editing', function (value) {
          if (value) {
            scope.collapsed = false;
          }
        });
      }
    };
  })
  .directive('muokattavaKentta', function($compile, $rootScope,
    Editointikontrollit, $q, $timeout) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        field: '=fieldInfo',
        objectReady: '=objectPromise',
        removeField: '&?',
        editEnabled: '='
      },
      controller: function ($scope, YleinenData, MuokkausUtils, Varmistusdialogi, Utils) {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

        $scope.$watch('objectReady', function(newObjectReadyPromise) {
          newObjectReadyPromise.then(function(newObject) {
            $scope.object = newObject;
          });
        });

        function poistaOsio(value) {
          if(angular.isString(value)) {
            MuokkausUtils.nestedSet($scope.object, $scope.field.path, '.', '');
          } else {
            MuokkausUtils.nestedSet($scope.object, $scope.field.path, '.', undefined);
          }
          if(!$scope.mandatory) {
            $scope.removeField({fieldToRemove: $scope.field});
          }
        }

        $scope.suljeOsio = function() {
          // Jos kentässä on dataa, kysytään varmistus.
          var getValue = MuokkausUtils.nestedGet($scope.object, $scope.field.path, '.');
          if (!_.isEmpty(getValue)) {
            Varmistusdialogi.dialogi({
              otsikko: 'varmista-osion-poisto-otsikko',
              teksti: 'varmista-osion-poisto-teksti',
              primaryBtn: 'poista',
              successCb: function () {
                poistaOsio(getValue);
              }
            })();
          } else {
            // Tyhjän voi poistaa heti.
            poistaOsio(getValue);
          }
        };

        function getTitlePath() {
          return _.initial($scope.field.path.split('.'), 1).join('.') + '.' + $scope.field.originalLocaleKey;
        }

        $scope.editOsio = function () {
          // Assumed that field has a title at upper level in hierarchy
          $scope.titlePath = getTitlePath();
          $scope.originalContent = angular.copy(MuokkausUtils.nestedGet($scope.object, $scope.field.path, '.'));
          $scope.originalTitle = angular.copy(MuokkausUtils.nestedGet($scope.object, $scope.titlePath, '.'));
          $scope.field.$editing = true;
        };

        $scope.okEdit = function () {
          $scope.titlePath = $scope.titlePath || getTitlePath();
          var title = MuokkausUtils.nestedGet($scope.object, $scope.titlePath, '.');
          console.log($scope.object, $scope.titlePath, title, Utils.hasLocalizedText(title));
          if (Utils.hasLocalizedText(title)) {
            // Force model update
            $rootScope.$broadcast('notifyCKEditor');

            $scope.originalContent = null;
            $scope.originalTitle = null;
            $scope.field.$editing = false;
          }
        };


        $scope.cancelEdit = function () {
          if (!$scope.originalContent) {
            // New, can delete
            poistaOsio(MuokkausUtils.nestedGet($scope.object, $scope.field.path, '.'));
          } else {
            MuokkausUtils.nestedSet($scope.object, $scope.field.path, '.', $scope.originalContent, true);
            MuokkausUtils.nestedSet($scope.object, $scope.titlePath, '.', $scope.originalTitle, true);
            $scope.field.$editing = false;
          }
        };

      },
      link: function(scope, element) {
        var typeParams = scope.field.type.split('.');

        $q.all({object: scope.objectReady, editMode: Editointikontrollit.getEditModePromise()}).then(function(values) {
          scope.object = values.object;
          scope.editMode = values.editMode;

          if(!scope.field.mandatory) {
            var contentFrame = angular.element('<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>')
              .append(getElementContent(typeParams[0]));

            populateElementContent(contentFrame);
          } else {
            populateElementContent(getElementContent(typeParams[0]));
          }
        });

        var ELEMENT_MAP = {
          'editor-header': ['addEditorAttributesFor', '<h3>'],
          'text-input': ['addInputAttributesFor', '<input>', {'editointi-kontrolli': ''}],
          'input-area': ['addInputAttributesFor', '<textarea>', {'editointi-kontrolli': ''}],
          'editor-text': ['addEditorAttributesFor', '<p>'],
          'editor-area': ['addEditorAttributesFor', '<div>'],
          'arviointi': ['', '<arviointi>', {
            'editointi-sallittu': 'true',
            'arviointi': 'object.' + scope.field.path,
            'edit-enabled': 'editEnabled'
          }],
          'vuosiluokkakokonaisuuden-osaaminen': ['', '<div>', {
            'editointi-sallittu': 'true',
            'vuosiluokkakokonaisuuden-osaaminen': 'object.' + scope.field.path,
            'edit-enabled': 'editEnabled'
          }]
        };

        var mapperFns = {
          addEditorAttributesFor: function (element) {
            element
            .addClass('list-group-item-text')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('ckeditor', '')
            .attr('editing-enabled', '{{editMode}}');
            var placeholder = scope.field.placeholder ? scope.field.placeholder :
              'muokkaus-' + scope.field.localeKey + '-placeholder';
            element.attr('editor-placeholder', placeholder);
            return element;
          },
          addInputAttributesFor: function (element) {
            return element
            .addClass('form-control')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('placeholder','{{ \'muokkaus-' + scope.field.localeKey + '-placeholder\' | kaanna }}');
          }
        };

        function wrapEditor(element) {
          var editWrapper = angular.element('<div ng-if="field.$editing"></div>');
          editWrapper.append(element);
          var viewWrapper = angular.element('<div ng-if="!field.$editing" ng-bind-html="valitseKieli(object.' +
                                            scope.field.path + ') | unsafe"></div>');
          var wrapper = angular.element('<div>');
          wrapper.append(editWrapper, viewWrapper);
          return wrapper;
        }

        function getElementContent(elementType) {
          var element = null;
          var mapped = ELEMENT_MAP[elementType];
          if (!mapped) {
            return null;
          }
          element = (mapped[0] ? mapperFns[mapped[0]] : _.identity )(angular.element(mapped[1]));
          _.each(mapped[2], function (value, key) {
            element.attr(key, value);
          });

          if(element !== null && scope.field.localized) {
            element.attr('localized', '');
          }
          if (scope.field.isolateEdit) {
            element = wrapEditor(element);
          }
          return element;
        }

        function populateElementContent(content) {
          element.append(content);
          $timeout(function () {
            $compile(element.contents())(scope);
          });

        }
      }
    };
  })
  .directive('vaihtoehtoisenKentanRaami', function() {
    return {
      templateUrl: 'views/directives/vaihtoehtoisenkentanraami.html',
      restrict: 'E',
      transclude: true,
      link: function (scope, element) {
        scope.$watch('editEnabled', function () {
          var buttons = element.find('.field-buttons');
          var header = element.closest('li.kentta').find('.osio-otsikko');
          buttons.detach().appendTo(header);
        });
      },
      controller: function ($scope) {
        $scope.callFn = function ($event, fn) {
          if ($event) {
            $event.preventDefault();
            $event.stopPropagation();
          }
          $scope[fn]();
        };
      }
    };
  })
  .directive('localized', function($rootScope, YleinenData)  {
    return {
      priority: 5,
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ngModelCtrl) {

        ngModelCtrl.$formatters.push(function(modelValue) {
          if(angular.isUndefined(modelValue)) { return; }
          if(modelValue === null) { return; }
          return modelValue[YleinenData.kieli];
        });

        ngModelCtrl.$parsers.push(function(viewValue) {
          var localizedModelValue = ngModelCtrl.$modelValue;

          if(angular.isUndefined(localizedModelValue)) {
            localizedModelValue = {};
          }
          if(localizedModelValue === null) {
            localizedModelValue = {};
          }
          localizedModelValue[YleinenData.kieli] = viewValue;
          return localizedModelValue;
        });

        scope.$on('$translateChangeSuccess', function() {
          if(!angular.isUndefined(ngModelCtrl.$modelValue) && !_.isEmpty(ngModelCtrl.$modelValue[YleinenData.kieli])) {
            ngModelCtrl.$setViewValue(ngModelCtrl.$modelValue[YleinenData.kieli]);
          } else {
            ngModelCtrl.$setViewValue('');
          }
          ngModelCtrl.$render();
        });
      }
    };
  });

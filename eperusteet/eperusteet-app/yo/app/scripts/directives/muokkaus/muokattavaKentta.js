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
  .directive('muokkauskenttaRaamit', function() {
    return {
      templateUrl: 'views/partials/muokkaus/muokattavaKentta.html',
      restrict: 'A',
      transclude: true,
      scope: {
        localeKey: '@otsikko',
        piilotaOtsikko: '@?'
      },
      link: function(scope, element, attrs) {
        scope.otsikko = 'muokkaus-' + scope.localeKey + '-header';
        element.addClass('list-group-item ');
        element.attr('ng-class', '');

        scope.canCollapse = attrs.collapsible || false;
        scope.collapsed = false;
      }
    };
  })
  .directive('muokattavaKentta', function($compile, $rootScope, MuokkausUtils,
    YleinenData, Editointikontrollit, $q, Varmistusdialogi) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        field: '=fieldInfo',
        objectReady: '=objectPromise',
        removeField: '&?',
        editEnabled: '='
      },
      link: function(scope, element) {

        scope.$watch('objectReady', function(newObjectReadyPromise) {
          newObjectReadyPromise.then(function(newObject) {
            scope.object = newObject;
          });
        });

        var typeParams = scope.field.type.split('.');

        function poistaOsio(value) {
          if(angular.isString(value)) {
            MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', '');
          } else {
            MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', undefined);
          }
          if(!scope.mandatory) {
            scope.removeField({fieldToRemove: scope.field});
          }
        }

        scope.suljeOsio = function() {
          // Jos kentässä on dataa, kysytään varmistus.
          var getValue = MuokkausUtils.nestedGet(scope.object, scope.field.path, '.');
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

        $q.all({object: scope.objectReady, editMode: Editointikontrollit.getEditModePromise()}).then(function(values) {
          scope.object = values.object;
          scope.editMode = values.editMode;

          if(!scope.field.mandatory) {
            var contentFrame = angular.element('<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>')
            .attr('osion-nimi', scope.field.header)
            .append(getElementContent(typeParams[0]));

            contentFrame.attr('sulje-osio', 'suljeOsio()');

            populateElementContent(contentFrame);
          } else {
            populateElementContent(getElementContent(typeParams[0]));
          }
        });

        function getElementContent(elementType) {
          function addEditorAttributesFor(element) {
            return element
            .addClass('list-group-item-text')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('ckeditor', '')
            .attr('editing-enabled', '{{editMode}}')
            .attr('editor-placeholder', 'muokkaus-' + scope.field.localeKey + '-placeholder');
          }

          function addInputAttributesFor(element) {
            return element
            .addClass('form-control')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('placeholder','{{\'muokkaus-' + scope.field.localeKey + '-placeholder\' | translate}}');
          }

          var element = null;
          if(elementType === 'editor-header') {
            element = addEditorAttributesFor(angular.element('<h3></h3>'));
          }

          else if(elementType === 'text-input') {
            element = addInputAttributesFor(angular.element('<input></input>').attr('editointi-kontrolli', ''));
          }

          else if(elementType === 'input-area') {
            element = addInputAttributesFor(angular.element('<textarea></textarea>').attr('editointi-kontrolli', ''));
          }

          else if(elementType === 'editor-text') {
            element = addEditorAttributesFor(angular.element('<p></p>'));
          }

          else if(elementType === 'editor-area') {
            element = addEditorAttributesFor(angular.element('<div></div>'));
          }

          else if(elementType === 'arviointi') {
            element =
            angular.element('<arviointi></arviointi>')
            .attr('arviointi', 'object.' + scope.field.path)
            .attr('editointi-sallittu', 'true')
            .attr('edit-enabled', 'editEnabled');
          }

          if(element !== null && scope.field.localized) {
            element.attr('localized', '');
          }
          return element;
        }

        // function replaceElementContent(content) {
        //   element.empty();
        //   populateElementContent(content);
        // }

        function populateElementContent(content) {
          element.append(content);
          $compile(element.contents())(scope);
        }
      }
    };
  })
  .directive('vaihtoehtoisenKentanRaami', function() {
    return {
      template:
        '<div ng-transclude></div>' +
        '<button icon-role="remove" ng-if="$parent.editEnabled" editointi-kontrolli type="button"' +
        ' class="pull-right poista-osio btn btn-default btn-xs" ng-click="suljeOsio($event)">' +
        '{{\'poista-osio\' | translate}}</button>',
      restrict: 'E',
      transclude: true,
      scope: {
        osionNimi: '@',
        suljeOsio: '&'
      },
      link: function (scope, element) {
        scope.$watch('$parent.editEnabled', function () {
          var button = element.find('button.poista-osio');
          var header = angular.element('li[otsikko='+scope.$parent.field.localeKey+'] .osio-otsikko');
          button.detach().appendTo(header);
        });
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

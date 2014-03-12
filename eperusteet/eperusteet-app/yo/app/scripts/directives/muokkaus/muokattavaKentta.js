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
      template:
        '<h4 ng-hide="piilotaOtsikko" class="list-group-item-heading" >{{otsikko | translate}}&nbsp;&nbsp;' +
        '<span class="glyphicon glyphicon-plus" ng-show="canCollapse && collapsed" ng-click="collapsed = false"></span>' +
        '<span class="glyphicon glyphicon-minus" ng-show="canCollapse && !collapsed" ng-click="collapsed = true"></span></h4>' +
        '<div collapse="collapsed" ng-transclude></div>',
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

        if(attrs.kiinniOletuksena) {
          scope.canCollapse = true;
          scope.collapsed = attrs.kiinniOletuksena;
        } else {
          scope.canCollapse = false;
          scope.collapsed = false;
        }
      }
    };
  })
  .directive('muokattavaKentta', function($compile, $rootScope, MuokkausUtils, YleinenData, Editointikontrollit, $q) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        field: '=fieldInfo',
        objectReady: '=tutkinnonOsaReady',
        removeField: '&?'
      },
      link: function(scope, element, attrs) {

        scope.$watch('objectReady', function(newObjectReadyPromise) {
          newObjectReadyPromise.then(function(newObject) {
            scope.object = newObject;
          });
        });
        
        var typeParams = scope.field.type.split('.');

        $q.all({object: scope.objectReady, editMode: Editointikontrollit.getEditModePromise()}).then(function(values) {
          scope.object = values.object;
          scope.editMode = values.editMode;
          
          if(!scope.field.mandatory) {
            var contentFrame = angular.element('<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>')
            .attr('osion-nimi', scope.field.header)
            .append(getElementContent(typeParams[0]));

            scope.suljeOsio = function() {
              console.log('suljetaan ja poistetaan sisältö');

              if(angular.isString(MuokkausUtils.nestedGet(scope.object, scope.field.path, '.'))) {
                MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', '');
              } else {
                MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', undefined);
              }

              if(!scope.mandatory) {
                scope.removeField({fieldToRemove: scope.field});
              }
            };

            contentFrame.attr('sulje-osio', 'suljeOsio()');

            populateElementContent(contentFrame);
          } else {
            populateElementContent(getElementContent(typeParams[0]));
          }
        });

        function getElementContent(elementType) {
          
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
            .attr('editointi-sallittu', 'true');
          } 
          
          else if (elementType === 'koodisto-select') {
            scope.tuoKoodi = function(koodi) {
              MuokkausUtils.nestedSet(scope.object, scope.field.path, ',', koodi);
            };
            element = angular.element('<div></div>').addClass('input-group')
            .append(
                angular.element('<input/>')
                .addClass('form-control')
                .attr('type', 'text')
                .attr('ng-model', 'object.' + scope.field.path)
                .attr('editointi-kontrolli', ''))
            .append(
                angular.element('<koodisto-select></koodisto-select>')
                .addClass('input-group-btn')
                .attr('valmis', 'tuoKoodi'));
          }
          
          if(element !== null && scope.field.localized) {
            element.attr('localized', '');
          }
          return element;

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
        }

        function replaceElementContent(content) {
          element.empty();
          populateElementContent(content);
        }

        function populateElementContent(content) {
          element.append(content);
          $compile(element.contents())(scope);
        }
      }
    };
  })
  .directive('vaihtoehtoisenKentanRaami', function($rootScope) {
    return {
      template:
        '<div ng-transclude></div>' +
        '<button editointi-kontrolli type="button" class="btn btn-default btn-xs" ng-click="suljeOsio()">{{\'poista\' | translate}}&nbsp;{{osionNimi | translate}}&nbsp;&nbsp;<span class="glyphicon glyphicon-minus"></span></button>',
      restrict: 'E',
      transclude: true,
      scope: {
        osionNimi: '@',
        suljeOsio: '&',
      },
    };
  })
  .directive('editointiKontrolli', function($rootScope, Editointikontrollit) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        
        Editointikontrollit.getEditModePromise().then(function(editMode) {
          if(!editMode) {
            element.attr('disabled', 'disabled');
          }
        });

        $rootScope.$on('enableEditing', function() {
          if(!element.attr('ng-disabled') || !scope.$eval(element.attr('ng-disabled'))) {
            element.removeAttr('disabled');
          }

        });
        $rootScope.$on('disableEditing', function() {
          element.attr('disabled', 'disabled');
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
          if(angular.isUndefined(modelValue)) return;
          return modelValue[YleinenData.kieli];
        });
        
        ngModelCtrl.$parsers.push(function(viewValue) {
          var localizedModelValue = ngModelCtrl.$modelValue;
          
          if(angular.isUndefined(localizedModelValue)) {
            localizedModelValue = {};
          }
          localizedModelValue[YleinenData.kieli] = viewValue;
          return localizedModelValue;
        });
        
        $rootScope.$on('$translateChangeSuccess', function() {
          console.log(YleinenData.kieli);
          if(!angular.isUndefined(ngModelCtrl.$modelValue) && !_.isEmpty(ngModelCtrl.$modelValue[YleinenData.kieli])) {
            ngModelCtrl.$setViewValue(ngModelCtrl.$modelValue[YleinenData.kieli]);
          } else {
            ngModelCtrl.$setViewValue('');
          }
          console.log(ngModelCtrl);
          ngModelCtrl.$render();
        });
      }
    };
  });
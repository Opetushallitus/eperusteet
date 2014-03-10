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
        '<h4 class="list-group-item-heading" >{{otsikko | translate}}&nbsp;&nbsp;' +
        '<span class="glyphicon glyphicon-plus" ng-show="canCollapse && collapsed" ng-click="collapsed = false"></span>' +
        '<span class="glyphicon glyphicon-minus" ng-show="canCollapse && !collapsed" ng-click="collapsed = true"></span></h4>' +
        '<div collapse="collapsed" ng-transclude></div>',
      restrict: 'A',
      transclude: true,
      scope: {
        localeKey: '@otsikko'
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
  .directive('muokattavaKentta', function($compile, $rootScope, MuokkausUtils) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        field: '=fieldInfo',
        objectReady: '=tutkinnonOsaReady',
        removeField: '&?'
      },
      link: function(scope, element, attrs) {

        var typeParams = scope.field.type.split('.');

        scope.objectReady.then(function(value) {
          scope.object = value;
          if(!scope.field.mandatory) {
            var contentFrame = angular.element('<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>')
            .attr('osion-nimi', scope.field.header)
            .append(getElementContent(typeParams[0]));

            scope.suljeOsio = function() {
              console.log('suljetaan ja poistetaan sisältö');

              if(angular.isString(MuokkausUtils.nestedGet(scope.object, scope.field.path, '.'))) {
                MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', '');
              } else {
                MuokkausUtils.nestedSet(scope.object, scope.field.path, '.', null);
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
          if(elementType === 'text-input') {
            if(MuokkausUtils.hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<input></input>').attr('editointi-kontrolli', ''));
          }
          else if(elementType === 'text-area') {
            if(MuokkausUtils.hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<textarea></textarea>').attr('editointi-kontrolli', ''));
          } else if(elementType === 'arviointi') {
            return angular.element('<arviointi></arviointi>').attr('arviointi', 'object.' + scope.field.path).attr('editointi-sallittu', 'true');
          } else if (elementType === 'koodisto-select') {
            scope.tuoKoodi = function(koodi) {
              MuokkausUtils.nestedSet(scope.object, scope.field.path, ',', koodi);
            };
            return angular.element('<div></div>').addClass('input-group')
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

          function addEditorAttributesFor(element) {
            return element
            .addClass('list-group-item-text')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('ckeditor', '')
            .attr('editing-enabled', 'false')
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
        if(!Editointikontrollit.editMode) {
          element.attr('disabled', 'disabled');
        }

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
  });
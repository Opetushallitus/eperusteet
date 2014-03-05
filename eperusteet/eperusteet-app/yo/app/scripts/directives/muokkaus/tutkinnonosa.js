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
  .directive('muokkausTutkinnonosa', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '=tutkinnonOsa'
      },
      controller: function($scope, $location, Editointikontrollit, PerusteenOsat, $compile, $q, $route) {
        
        $scope.panelType = 'panel-default';
        
        function setupTutkinnonOsa(osa) {
          $scope.editableTutkinnonOsa = angular.copy(osa);
          
          $scope.tutkinnonOsanMuokkausOtsikko = $scope.editableTutkinnonOsa.id ? "muokkaus-tutkinnon-osa" : "luonti-tutkinnon-osa";
          
          Editointikontrollit.registerCallback({
            edit: function() {
              console.log('tutkinnon osa - edit');
              $scope.editClass = 'editing';
              $scope.panelType = 'panel-info';
            },
            save: function() {
              $scope.editClass = '';
              $scope.panelType = 'panel-default';
              //TODO: Validate tutkinnon osa
              console.log('validate tutkinnon osa');
              if($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa({}, function() {
                  console.log('MORO');
                  $route.reload();
                });
              } else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa).$promise.then(function(response) {
                  $location.path('/muokkaus/tutkinnonosa/' + response.id);
                });
              }
              $scope.tutkinnonOsa = angular.copy($scope.editableTutkinnonOsa);
            },
            cancel: function() {
              $scope.editClass = '';
              $scope.panelType = 'panel-default';
              console.log('tutkinnon osa - cancel');
              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
            }
          });
        }
             
        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsaReady = $scope.tutkinnonOsa.$promise.then(function(response) {
            setupTutkinnonOsa(response);
            return $scope.editableTutkinnonOsa;
          });
        } else {
          var objectReadyDefer = $q.defer();
          $scope.tutkinnonOsaReady = objectReadyDefer.promise;
          $scope.tutkinnonOsa = {};
          setupTutkinnonOsa($scope.tutkinnonOsa);
          objectReadyDefer.resolve($scope.editableTutkinnonOsa);
        }
        
        
        
        $scope.visibleFields = 
          new Array({
                       path: 'nimi.fi', 
                       placeholder: 'muokkaus-otsikko-placeholder',
                       header: 'muokkaus-tutkinnon-osan-nimi',
                       type: 'text-input'
                     },{
                       path: 'tavoitteet.fi', 
                       placeholder: 'muokkaus-tavoitteet-placeholder',
                       header: 'muokkaus-tutkinnon-osan-tavoitteet',
                       type: 'text-area.default-closed'
                     },{
                       path: 'ammattitaitovaatimukset.fi', 
                       placeholder: 'muokkaus-ammattitaitovaatimukset-placeholder',
                       header: 'muokkaus-tutkinnon-osan-ammattitaitovaatimukset',
                       type: 'text-area.default-closed'
                     },{
                       path: 'ammattitaidonOsoittamistavat.fi', 
                       placeholder: 'muokkaus-ammattitaidon-osoittamistavat-placeholder',
                       header: 'muokkaus-tutkinnon-osan-ammattitaidon-osoittamistavat',
                       type: 'text-input.default-closed'
                     },{
                       path: 'osaamisala.fi', 
                       placeholder: 'muokkaus-osaamisala-placeholder',
                       header: 'muokkaus-tutkinnon-osan-osaamisala',
                       type: 'text-input.default-closed'
                     },{
                       path: 'koodi', 
                       placeholder: 'muokkaus-koodi-placeholder',
                       header: 'muokkaus-tutkinnon-osan-koodi',
                       type: 'text-input.default-closed'
                     });
      }
    };
  })
  .directive('muokkauskenttaRaamit', function() {
    return {
      template:
        '<h4 class="list-group-item-heading" translate>{{otsikko}}</h4>' +
        '<span ng-transclude></span>',
      restrict: 'A',
      transclude: true,
      scope: {
        otsikko: "@"
      },
      link: function(scope, element, attrs) {
        element.addClass('list-group-item ');
        element.attr('ng-class', '');
      }
    };
  })
  .directive('muokattavaKentta', function($compile) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        field: '=fieldInfo',
        objectReady: '=tutkinnonOsaReady'
      },
      link: function(scope, element, attrs) {
        var typeParams = scope.field.type.split('.');
        
        scope.objectReady.then(function(value) {
          scope.object = value;
          if(typeParams.length > 1 && typeParams[1] === 'default-closed') {
            var contentFrame = angular.element('<vaihtoehtoisen-kentan-raami></vaihtoehtoisen-kentan-raami>')
            .attr('osion-nimi', scope.field.header)
            .attr('oletuksena-auki', hasValue(scope.object, scope.field.path) ? 'true' : 'false')
            .append(getElementContent(typeParams[0]));
            
            scope.avaaOsio = function() {
              console.log('avataan');
            };
            scope.suljeOsio = function() {
              console.log('suljetaan ja poistetaan sisältö');
              var collapseElement = contentFrame.children().first();
              
              scope.object = nestedOmit(scope.object, scope.field.path, '.');
              
              collapseElement.empty().append(getElementContent(typeParams[0]));
              $compile(collapseElement.contents())(scope);
     
              
            };
            
            contentFrame.attr('avaa-osio', 'avaaOsio()').attr('sulje-osio', 'suljeOsio()');
            
            populateElementContent(contentFrame);
          } else {
            populateElementContent(getElementContent(typeParams[0]));
          }
        });
        
        function getElementContent(elementType) {
          if(elementType === 'text-input') {
            if(hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<input></input>'));
          }
          else if(elementType === 'text-area') {
            if(hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<textarea></textarea>'));
          }
          
          function addEditorAttributesFor(element) {
            return element
            .addClass('list-group-item-text')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('ckeditor', '')
            .attr('editing-enabled', 'false')
            .attr('editor-placeholder', '{{' + scope.field.placeholder + '}}');
          }
          
          function addInputAttributesFor(element) {
            return element
            .addClass('form-control')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('placeholder','{{\'' + scope.field.placeholder + '\' | translate}}');
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
        
        function hasValue(obj, path) {
          if (nestedHas(obj, path, '.') && angular.isString(nestedGet(obj, path, '.')) && nestedGet(obj, path, '.').length > 0) {
            return true;
          } else {
            return false;
          }
        }
        
        function nestedHas(obj, path, delimiter) {
          var propertyNames = path.split(delimiter);
          
          return innerNestedHas(obj, propertyNames);
          
          function innerNestedHas(obj, names) {
            if(_.has(obj, names[0])) {
              return names.length > 1 ? innerNestedHas(obj[names[0]], names.splice(1, names.length)) : true;
            } else {
              return false;
            }
          };
        }
        
        function nestedGet(obj, path, delimiter) {
          var propertyNames = path.split(delimiter);
          
          return innerNestedGet(obj, propertyNames);
          
          function innerNestedGet(obj, names) {
            if(names.length > 1) {
              return innerNestedGet(obj[names[0]], names.splice(1, names.length));
            } else {
              return obj[names[0]];
            }
          }
        }
        
        function nestedSet(obj, path, delimiter, value) {
          var propertyNames = path.split(delimiter);
          
          innerNestedSet(obj, propertyNames, value);
          
          function innerNestedSet(obj, names, newValue) {
            if(names.length > 1) {
              innerNestedSet(obj[names[0]], names.splice(1, names.length), newValue);
            }  else {
              obj[names[0]] = newValue;
            }
          }
        }
        
        function nestedOmit(obj, path, delimiter) {
          var propertyNames = path.split(delimiter);
          
          return innerNestedOmit(obj, propertyNames);
          
          function innerNestedOmit(obj, names) {
            if(names.length > 1) {
              obj[names[0]] = innerNestedOmit(obj[names[0]], names.splice(1, names.length));
              return obj;
            } else {
              return _.omit(obj, names[0]);
            }
          }
        }
      }
    };
  })
  .directive('vaihtoehtoisenKentanRaami', function($compile) {
    return {
      template:
        '<div ng-show="osioAuki" ng-transclude></div>' +
        '<button type="button" class="btn btn-default btn-xs" ng-hide="osioAuki" ng-click="avaaOsio(); osioAuki = true;">{{\'lisaa\' | translate}}&nbsp;{{osionNimi | translate}}&nbsp;&nbsp;<span class="glyphicon glyphicon-plus"></span></button>' +
        '<button type="button" class="btn btn-default btn-xs" ng-show="osioAuki" ng-click="osioAuki = false; suljeOsio();">{{\'poista\' | translate}}&nbsp;{{osionNimi | translate}}&nbsp;&nbsp;<span class="glyphicon glyphicon-minus"></span></button>',
      restrict: 'E',
      transclude: true,
      scope: {
        osionNimi: "@",
        avaaOsio: "&",
        suljeOsio: "&",
        oletuksenaAuki: "@"
      },
      link: function(scope, element, attrs) {
        scope.osioAuki = scope.oletuksenaAuki === 'true';
      }
    };
  });


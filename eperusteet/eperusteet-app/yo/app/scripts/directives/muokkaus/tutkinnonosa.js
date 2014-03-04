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
      controller: function($scope, $location, Editointikontrollit, PerusteenOsat, $compile) {
        
        $scope.panelType = 'panel-default';
        
        function hasNested(obj, path, delimiter) {
          var propertyNames = path.split(delimiter);
          
          function innerHasNested(obj, names) {
            if(_.has(obj, names[0])) {
              return names.length > 1 ? innerHasNested(obj[names[0]], names.splice(1, names.length)) : true;
            } else {
              return false;
            }
          };
          
          return innerHasNested(obj, propertyNames);
        }
        
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
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa();  
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
        
        function setupEditTutkinnonOsaHtml(fields) {
          console.log('EDIT HTML');
          console.log(document.getElementById('muokkaus-sisaltoalue'));
          var listElement = angular.element('<ul></ul>').addClass('list-group').addClass('muokkaus');
          angular.forEach(fields, function(field) {
            console.log(field);
            var el = 
              angular.element('<li></li>')
              .attr('muokkauskentta-raamit', '')
              .attr('otsikko', field.header);
            if(hasNested($scope, field.path, '.')) {
              el = $compile(el.append(
                  angular.element('<inline-muokkauskentta></inline-muokkauskentta>')
                  .attr('editable-field', field.path)
                  .attr('placeholder', field.placeholder)))($scope);
            } else {
              el = $compile(el.append(
                  angular.element('<input></input>').addClass('form-control')
                  .attr('ng-model', field.path)
                  .attr('placeholder','{{\'' + field.placeholder + '\' | translate}}')))($scope);
            }
            console.log(el);
            console.log(angular.element('#muokkaus-sisaltoalue'));
            listElement.append(el);
          });
          angular.element(document.getElementById('muokkaus-sisaltoalue')).replaceWith(listElement);          
        }
        
        var visibleFields = 
          new Array({
                       path: 'editableTutkinnonOsa.nimi.fi', 
                       placeholder: 'muokkaus-otsikko-placeholder',
                       header: 'muokkaus-tutkinnon-osan-nimi'
                     },{
                       path: 'editableTutkinnonOsa.tavoitteet.fi', 
                       placeholder: 'muokkaus-tavoitteet-placeholder',
                       header: 'muokkaus-tutkinnon-osan-tavoitteet'
                     },{
                       path: 'editableTutkinnonOsa.ammattitaitovaatimukset.fi', 
                       placeholder: 'muokkaus-ammattitaitovaatimukset-placeholder',
                       header: 'muokkaus-tutkinnon-osan-ammattitaitovaatimukset'
                     },{
                       path: 'editableTutkinnonOsa.ammattitaidonOsoittamistavat.fi', 
                       placeholder: 'muokkaus-ammattitaidon-osoittamistavat-placeholder',
                       header: 'muokkaus-tutkinnon-osan-ammattitaidon-osoittamistavat'
                     },{
                       path: 'editableTutkinnonOsa.osaamisala.fi', 
                       placeholder: 'muokkaus-osaamisala-placeholder',
                       header: 'muokkaus-tutkinnon-osan-osaamisala'
                     },{
                       path: 'editableTutkinnonOsa.koodi', 
                       placeholder: 'muokkaus-koodi-placeholder',
                       header: 'muokkaus-tutkinnon-osan-koodi'
                     });
        
        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsa.$promise.then(function(response) {
            setupTutkinnonOsa(response);
            setupEditTutkinnonOsaHtml(visibleFields);
          });
        } else {
          $scope.tutkinnonOsa = {};
          setupTutkinnonOsa($scope.tutkinnonOsa);
          setupEditTutkinnonOsaHtml(visibleFields);
        }
        
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
  .directive('inlineMuokkauskentta', function() {
    return {
      template: '<p class="list-group-item-text" ng-model="editableField" ckeditor editing-enabled="false" editor-placeholder="{{placeholder}}"></p>',
      restrict: 'E',
      scope: {
        editableField: '=',
        placeholder: '@?',
        testikentta: '='
      }
    };
  });


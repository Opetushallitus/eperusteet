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
      controller: function($scope, $location, Editointikontrollit, PerusteenOsat, $compile, $q, $route, tutkinnonosaUtils) {
        $scope.editableTutkinnonOsa = {};
        $scope.panelType = 'panel-default';

        $scope.tuoKoodi = function(koodi) {
          $scope.editableTutkinnonOsa.koodi = koodi;
        };

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

        var tutkinnonOsaReadyPromise;

        if($scope.tutkinnonOsa) {
          tutkinnonOsaReadyPromise = $scope.tutkinnonOsa.$promise.then(function(response) {
            setupTutkinnonOsa(response);
            return $scope.editableTutkinnonOsa;
          });
        } else {
          var objectReadyDefer = $q.defer();
          tutkinnonOsaReadyPromise = objectReadyDefer.promise;
          $scope.tutkinnonOsa = {};
          setupTutkinnonOsa($scope.tutkinnonOsa);
          objectReadyDefer.resolve($scope.editableTutkinnonOsa);
        }

        var allFields =
          new Array({
             path: 'nimi.fi',
             placeholder: 'muokkaus-otsikko-placeholder',
             header: 'muokkaus-tutkinnon-osan-nimi',
             type: 'text-input',
             mandatory: true
           },{
             path: 'tavoitteet.fi',
             placeholder: 'muokkaus-tavoitteet-placeholder',
             header: 'muokkaus-tutkinnon-osan-tavoitteet',
             type: 'text-area',
             defaultClosed: true
           },{
             path: 'ammattitaitovaatimukset.fi',
             placeholder: 'muokkaus-ammattitaitovaatimukset-placeholder',
             header: 'muokkaus-tutkinnon-osan-ammattitaitovaatimukset',
             type: 'text-area',
             defaultClosed: true
           },{
             path: 'ammattitaidonOsoittamistavat.fi',
             placeholder: 'muokkaus-ammattitaidon-osoittamistavat-placeholder',
             header: 'muokkaus-tutkinnon-osan-ammattitaidon-osoittamistavat',
             type: 'text-input',
             defaultClosed: true
           },{
             path: 'osaamisala.fi',
             placeholder: 'muokkaus-osaamisala-placeholder',
             header: 'muokkaus-tutkinnon-osan-osaamisala',
             type: 'text-input',
             defaultClosed: true
           },{
             path: 'arviointi',
             header: 'muokkaus-tutkinnon-osan-arviointi',
             type: 'arviointi',
             defaultClosed: true,
             mandatory: true
           },{
             path: 'koodi',
             placeholder: 'muokkaus-koodi-placeholder',
             header: 'muokkaus-tutkinnon-osan-koodi',
             type: 'koodisto-select',
             defaultClosed: false
           });

        $scope.tutkinnonOsaReady = tutkinnonOsaReadyPromise.then(function(tutkinnonOsa) {
          console.log(tutkinnonOsa);
          $scope.visibleFields = _.filter(allFields, function(field) {
            console.log(tutkinnonOsa);
            console.log(tutkinnonosaUtils.nestedGet(tutkinnonOsa, field.path, '.'));
            console.log(field.path);
            return field.mandatory || tutkinnonosaUtils.hasValue(tutkinnonOsa, field.path);
          });
          $scope.hiddenFields = _.difference(allFields, $scope.visibleFields);
          return tutkinnonOsa;
        });

        $scope.removeField = function(fieldToRemove) {
          console.log('remove field:');
          console.log(fieldToRemove);

          _.remove($scope.visibleFields, fieldToRemove);
          $scope.hiddenFields.push(fieldToRemove);
        };

        $scope.addFieldToVisible = function(field) {
          _.remove($scope.hiddenFields, field);
          $scope.visibleFields.push(field);
        };
      }
    };
  })
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
        otsikko: '@'
      },
      link: function(scope, element, attrs) {
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
  .directive('muokattavaKentta', function($compile, $rootScope, tutkinnonosaUtils) {
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

              if(angular.isString(tutkinnonosaUtils.nestedGet(scope.object, scope.field.path, '.'))) {
                tutkinnonosaUtils.nestedSet(scope.object, scope.field.path, '.', '');
              } else {
                tutkinnonosaUtils.nestedSet(scope.object, scope.field.path, '.', null);
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
            if(tutkinnonosaUtils.hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<input></input>').attr('editointi-kontrolli', ''));
          }
          else if(elementType === 'text-area') {
            if(tutkinnonosaUtils.hasValue(scope.object, scope.field.path)) {
              return addEditorAttributesFor(angular.element('<p></p>'));
            }
            return addInputAttributesFor(angular.element('<textarea></textarea>').attr('editointi-kontrolli', ''));
          } else if(elementType === 'arviointi') {
            return angular.element('<arviointi></arviointi>').attr('arviointi', 'object.' + scope.field.path).attr('editointi-sallittu', 'true');
          } else if (elementType === 'koodisto') {
            return angular.element('<koodisto-select></koodisto-select>').attr('valmis', 'object.' + scope.field.path).attr('editointi-sallittu', 'true');
          }

          function addEditorAttributesFor(element) {
            return element
            .addClass('list-group-item-text')
            .attr('ng-model', 'object.' + scope.field.path)
            .attr('ckeditor', '')
            .attr('editing-enabled', 'false')
            .attr('editor-placeholder', scope.field.placeholder);
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
  })
  .service('tutkinnonosaUtils', function() {
    this.hasValue = function(obj, path) {
      if (this.nestedHas(obj, path, '.')) {
        if (angular.isString(this.nestedGet(obj, path, '.'))) {
          if(this.nestedGet(obj, path, '.').length > 0) {
            return true;
          } else {
            return false;
          }
        } else if(!angular.isUndefined(this.nestedGet(obj, path, '.')) && this.nestedGet(obj, path, '.') !== null) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    };

    this.nestedHas = function(obj, path, delimiter) {
      var propertyNames = path.split(delimiter);

      return innerNestedHas(obj, propertyNames);

      function innerNestedHas(obj, names) {
        if(_.has(obj, names[0])) {
          return names.length > 1 ? innerNestedHas(obj[names[0]], names.splice(1, names.length)) : true;
        } else {
          return false;
        }
      }
    };

    this.nestedGet = function(obj, path, delimiter) {
      if(!this.nestedHas(obj, path, delimiter)) {
        return undefined;
      }
      var propertyNames = path.split(delimiter);

      return innerNestedGet(obj, propertyNames);

      function innerNestedGet(obj, names) {
        if(names.length > 1) {
          return innerNestedGet(obj[names[0]], names.splice(1, names.length));
        } else {
          return obj[names[0]];
        }
      }
    };

    this.nestedSet = function(obj, path, delimiter, value) {
      var propertyNames = path.split(delimiter);

      innerNestedSet(obj, propertyNames, value);

      function innerNestedSet(obj, names, newValue) {
        if(names.length > 1) {
          innerNestedSet(obj[names[0]], names.splice(1, names.length), newValue);
        }  else {
          obj[names[0]] = newValue;
        }
      }
    };

    this.nestedOmit = function(obj, path, delimiter) {
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
    };
  });

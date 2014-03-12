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
      controller: function($scope, $location, $q, $modal, MuokkausUtils, Editointikontrollit, PerusteenOsat) {
        
        var allFields =
          new Array({
             path: 'nimi',
             hideHeader: true,
             localeKey: 'tutkinnon-osan-nimi',
             type: 'editor-header',
             localized: true,
             mandatory: true
           },{
             path: 'koodiUri',
             localeKey: 'tutkinnon-osan-koodi',
             type: 'koodisto-select',
             mandatory: true
           },{
             path: 'tavoitteet',
             localeKey: 'tutkinnon-osan-tavoitteet',
             type: 'editor-area',
             localized: true,
             defaultClosed: true
           },{
             path: 'ammattitaitovaatimukset',
             localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
             type: 'editor-area',
             localized: true,
             defaultClosed: true
           },{
             path: 'ammattitaidonOsoittamistavat',
             localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
             type: 'editor-text',
             localized: true,
             defaultClosed: true
           },{
             path: 'osaamisala',
             localeKey: 'tutkinnon-osan-osaamisala',
             type: 'editor-text',
             localized: true,
             defaultClosed: true
           },{
             path: 'arviointi',
             localeKey: 'tutkinnon-osan-arviointi',
             type: 'arviointi',
             defaultClosed: true,
             mandatory: true
           });
        
        $scope.editableTutkinnonOsa = {};
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
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa();
                openNotificationDialog();
              } else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa).$promise.then(function(response) {
                  openNotificationDialog().result.then(function() {
                    $location.path('/muokkaus/tutkinnonosa/' + response.id);
                  });
                });
              }
              $scope.tutkinnonOsa = angular.copy($scope.editableTutkinnonOsa);
            },
            cancel: function() {
              $scope.editClass = '';
              $scope.panelType = 'panel-default';
              console.log('tutkinnon osa - cancel');
              
              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
              var tutkinnonOsaDefer = $q.defer();
              $scope.tutkinnonOsaReady = tutkinnonOsaDefer.promise;
              
              splitFields($scope.editableTutkinnonOsa);
              tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
            }
          });
          
          function openNotificationDialog() {
            return $modal.open({
              templateUrl: 'views/modals/ilmoitusdialogi.html',
              controller: 'IlmoitusdialogiCtrl',
              resolve: {
                sisalto: function() {
                  return {
                    otsikko: 'tallennettu',
                    ilmoitus: 'muokkaus-tutkinnon-osa-tallennettu'
                  };
                }
              }
            });
          }
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

        $scope.tutkinnonOsaReady = tutkinnonOsaReadyPromise.then(function(tutkinnonOsa) {
          splitFields(tutkinnonOsa);
          return tutkinnonOsa;
        });

        $scope.removeField = function(fieldToRemove) {
          _.remove($scope.visibleFields, fieldToRemove);
          $scope.hiddenFields.push(fieldToRemove);
        };

        $scope.addFieldToVisible = function(field) {
          _.remove($scope.hiddenFields, field);
          $scope.visibleFields.push(field);
        };
        
        function splitFields(tutkinnonOsa) {
          $scope.visibleFields = _.filter(allFields, function(field) {
            return field.mandatory || MuokkausUtils.hasValue(tutkinnonOsa, field.path);
          });
          $scope.hiddenFields = _.difference(allFields, $scope.visibleFields);
        }
      }
    };
  });

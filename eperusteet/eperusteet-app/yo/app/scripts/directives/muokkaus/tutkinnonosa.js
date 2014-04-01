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
      template: '<kenttalistaus object-promise="tutkinnonOsaPromise" fields="fields">{{tutkinnonOsanMuokkausOtsikko | translate}}</kenttalistaus>',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '='
      },
      controller: function($scope, $state, $q, $modal, Editointikontrollit, PerusteenOsat) {

        $scope.fields =
          new Array({
             path: 'nimi',
             hideHeader: true,
             localeKey: 'tutkinnon-osan-nimi',
             type: 'editor-header',
             localized: true,
             mandatory: true,
             order: 1
           },{
             path: 'koodiUri',
             localeKey: 'tutkinnon-osan-koodi',
             type: 'koodisto-select',
             mandatory: true,
             order: 2
           },{
             path: 'tavoitteet',
             localeKey: 'tutkinnon-osan-tavoitteet',
             type: 'editor-area',
             localized: true,
             defaultClosed: true,
             order: 3
           },{
             path: 'ammattitaitovaatimukset',
             localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
             type: 'editor-area',
             localized: true,
             defaultClosed: true,
             order: 4
           },{
             path: 'ammattitaidonOsoittamistavat',
             localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
             type: 'editor-text',
             localized: true,
             defaultClosed: true,
             order: 5
           },{
             path: 'osaamisala',
             localeKey: 'tutkinnon-osan-osaamisala',
             type: 'editor-text',
             localized: true,
             defaultClosed: true,
             order: 6
           },{
             path: 'arviointi',
             localeKey: 'tutkinnon-osan-arviointi',
             type: 'arviointi',
             defaultClosed: true,
             mandatory: true,
             order: 7
           });

        $scope.editableTutkinnonOsa = {};

        function setupTutkinnonOsa(osa) {
          $scope.editableTutkinnonOsa = angular.copy(osa);

          $scope.tutkinnonOsanMuokkausOtsikko = $scope.editableTutkinnonOsa.id ? "muokkaus-tutkinnon-osa" : "luonti-tutkinnon-osa";

          Editointikontrollit.registerCallback({
            edit: function() {
              console.log('tutkinnon osa - edit');
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              console.log('validate tutkinnon osa');

              if($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa().then(function (response) {
                  $scope.editableTutkinnonOsa = angular.copy(response);
                  $scope.tutkinnonOsa = angular.copy(response);
                  
                  Editointikontrollit.lastModified = response;
                  
                  openNotificationDialog().result.then(function() {
                    var tutkinnonOsaDefer = $q.defer();
                    $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

                    tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
                  });
                });
              } else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa).$promise.then(function(response) {
                  
                  Editointikontrollit.lastModified = response;
                  
                  openNotificationDialog().result.then(function() {
                    $state.go('muokkaus.tutkinnonosa', { id: response.id });
                  });
                });
              }
              $scope.tutkinnonOsa = angular.copy($scope.editableTutkinnonOsa);
            },
            cancel: function() {
              console.log('tutkinnon osa - cancel');

              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
              var tutkinnonOsaDefer = $q.defer();
              $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

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

        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsaPromise = $scope.tutkinnonOsa.$promise.then(function(response) {
            setupTutkinnonOsa(response);
            return $scope.editableTutkinnonOsa;
          });
        } else {
          var objectReadyDefer = $q.defer();
          $scope.tutkinnonOsaPromise = objectReadyDefer.promise;
          $scope.tutkinnonOsa = {};
          setupTutkinnonOsa($scope.tutkinnonOsa);
          objectReadyDefer.resolve($scope.editableTutkinnonOsa);
        }
      }
    };
  });

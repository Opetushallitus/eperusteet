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
  .directive('muokkausTutkinnonosa', function(Notifikaatiot) {
    return {
      template: '<kenttalistaus edit-enabled="editEnabled" object-promise="tutkinnonOsaPromise" fields="fields">{{tutkinnonOsanMuokkausOtsikko | kaanna}}</kenttalistaus>',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '='
      },
      controller: function($rootScope, $scope, $state, $q, $modal, Editointikontrollit, PerusteenOsat, Editointicatcher) {
        $scope.fields =
          new Array({
             path: 'nimi',
             hideHeader: false,
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
        $scope.editEnabled = false;

        function cleanAccordionData(obj) {
          if (_.has(obj, 'accordionOpen')) {
            delete obj.accordionOpen;
          }
          _.each(obj, function (innerObj) {
            if (_.isObject(innerObj)) {
              cleanAccordionData(innerObj);
            }
          });
        }

        function setupTutkinnonOsa(osa) {
          $scope.editableTutkinnonOsa = angular.copy(osa);

          $scope.tutkinnonOsanMuokkausOtsikko = $scope.editableTutkinnonOsa.id ? $scope.editableTutkinnonOsa.nimi : 'luonti-tutkinnon-osa';

          Editointikontrollit.registerCallback({
            edit: function() {
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              cleanAccordionData($scope.editableTutkinnonOsa.arviointi);
              if ($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa(function (response) {
                  $scope.editableTutkinnonOsa = angular.copy(response);
                  $scope.tutkinnonOsa = angular.copy(response);
                  Editointikontrollit.lastModified = response;

                  openNotificationDialog();
                  // FIXME: Näillä ei mitään virkaa?
                  var tutkinnonOsaDefer = $q.defer();
                  $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                  tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
                }, Notifikaatiot.serverCb);
              } else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa, function(response) {
                  Editointikontrollit.lastModified = response;
                  openNotificationDialog();
                }, Notifikaatiot.serverCb);
              }
              Editointicatcher.give(_.clone($scope.editableTutkinnonOsa));
            },
            cancel: function() {
              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);

              // FIXME: Näillä ei mitään virkaa?
              var tutkinnonOsaDefer = $q.defer();
              $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
              tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
            },
            notify: function (mode) {
              $scope.editEnabled = mode;
            }
          });

          function openNotificationDialog() {
            Notifikaatiot.onnistui('tallennettu', 'muokkaus-tutkinnon-osa-tallennettu');
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

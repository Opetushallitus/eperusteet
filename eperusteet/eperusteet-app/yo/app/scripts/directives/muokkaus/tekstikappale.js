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

angular.module('eperusteApp')
  .directive('muokkausTekstikappale', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tekstikappale.html',
      restrict: 'E',
      scope: {
        tekstikappale: '='
      },
      controller: function($scope, $state, $q, $modal, Editointikontrollit, PerusteenOsat) {

        $scope.fields =
          new Array({
             path: 'nimi',
             hideHeader: true,
             localeKey: 'teksikappaleen-nimi',
             type: 'editor-header',
             localized: true,
             mandatory: true,
             order: 1
           },{
             path: 'teksti',
             localeKey: 'tekstikappaleen-teksti',
             type: 'editor-area',
             localized: true,
             mandatory: true,
             order: 2
           });

        function setupTekstikappale(kappale) {
          $scope.editableTekstikappale = angular.copy(kappale);

          $scope.tekstikappaleenMuokkausOtsikko = $scope.editableTekstikappale.id ? "muokkaus-tekstikappale" : "luonti-tekstikappale";

          Editointikontrollit.registerCallback({
            edit: function() {
              console.log('tutkinnon osa - edit');
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              console.log('validate tekstikappale');
              if($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale();
                openNotificationDialog();
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale).$promise.then(function(response) {
                  openNotificationDialog().result.then(function() {
                    $state.go('perusteprojekti.editoi.perusteenosa', { perusteenOsanTyyppi: 'tekstikappale', perusteenOsaId: response.id });
                  });
                });
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
            },
            cancel: function() {
              console.log('tutkinnon osa - cancel');

              $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
              var tekstikappaleDefer = $q.defer();
              $scope.tekstikappalePromise = tekstikappaleDefer.promise;

              tekstikappaleDefer.resolve($scope.editableTekstikappale);
            }
          });
        }

        function openNotificationDialog() {
          return $modal.open({
            templateUrl: 'views/modals/ilmoitusdialogi.html',
            controller: 'IlmoitusdialogiCtrl',
            resolve: {
              sisalto: function() {
                return {
                  otsikko: 'tallennettu',
                  ilmoitus: 'muokkaus-tekstikappale-tallennettu'
                };
              }
            }
          });
        }

        if($scope.tekstikappale) {
          $scope.tekstikappalePromise = $scope.tekstikappale.$promise.then(function(response) {
            setupTekstikappale(response);
            return $scope.editableTekstikappale;
          });
        } else {
          var objectReadyDefer = $q.defer();
          $scope.tekstikappalePromise = objectReadyDefer.promise;
          $scope.tekstikappale = {};
          setupTekstikappale($scope.tekstikappale);
          objectReadyDefer.resolve($scope.editableTekstikappale);
        }
      }
    };
  });


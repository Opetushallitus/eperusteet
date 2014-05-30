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
      controller: function($scope, $state, $stateParams, $q, $modal,
        Editointikontrollit, PerusteenOsat, Notifikaatiot, SivunavigaatioService) {
        $scope.versiot = {};

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
            },
            validate: function() {
              console.log('Tekstikappaleelta puuttuu validointi. Toteuta.');
              return true;
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              if ($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale(successCb, Notifikaatiot.serverCb);
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale, successCb, Notifikaatiot.serverCb);
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
              // Päivitä versiot
              $scope.versiot.tiedot = null;
              $scope.haeVersiot();
            },
            cancel: function() {
              $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
              var tekstikappaleDefer = $q.defer();
              $scope.tekstikappalePromise = tekstikappaleDefer.promise;

              tekstikappaleDefer.resolve($scope.editableTekstikappale);
            },
            notify: function (mode) {
              $scope.editEnabled = mode;
            }
          });

          $scope.haeVersiot();
        }

        function successCb() {
          Notifikaatiot.onnistui('tallennettu', 'muokkaus-tutkinnon-osa-tallennettu');
          // Päivitä sivunaviin mahdollisesti muuttuneet otsikot
          SivunavigaatioService.update();
        }

        if ($scope.tekstikappale) {
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

        $scope.muokkaa = function () {
          Editointikontrollit.startEditing();
        };

        $scope.$watch('editEnabled', function (editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.uusin = function () {
          return _.first($scope.versiot.tiedot) || {};
        };

        $scope.viimeksiMuokattu = function () {
          if ($scope.versiot && $scope.versiot.valittu) {
            var found = _.find($scope.versiot.tiedot, {number: $scope.versiot.valittu.number});
            if (found) {
              return found.date;
            }
          }
        };

        $scope.haeVersiot = function () {
          if (!$scope.versiot.tiedot) {
            $scope.versiot.tiedot = PerusteenOsat.revisions({osanId: $scope.tekstikappale.id}, function () {
              $scope.versiot.valittu = $scope.uusin();
              $scope.versiot.uusin = true;
              _.each($scope.versiot.tiedot, function (item, index) {
                // reverse numbering for UI, oldest = 1
                item.index = $scope.versiot.tiedot.length - index;
              });
            });
          }
        };
        $scope.vaihdaVersio = function () {
          PerusteenOsat.getRevision({
            osanId: $scope.tekstikappale.id,
            revisionId: $scope.versiot.valittu.number
          }).$promise.then(function(response) {
            $scope.tekstikappale = response;
            setupTekstikappale(response);
            $scope.versiot.uusin = $scope.versiot.valittu.number === $scope.uusin().number;
            var tekstikappaleDefer = $q.defer();
            $scope.tekstikappalePromise = tekstikappaleDefer.promise;
            tekstikappaleDefer.resolve($scope.editableTekstikappale);
          });
        };
      }
    };
  });


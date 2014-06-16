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

/* global _ */
'use strict';

angular.module('eperusteApp')
  .directive('muokkausTekstikappale', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tekstikappale.html',
      restrict: 'E',
      scope: {
        tekstikappale: '='
      },
      controller: function($scope, $q, Editointikontrollit, PerusteenOsat,
        Notifikaatiot, SivunavigaatioService, VersionHelper, Lukitus, $state,
        TutkinnonOsaEditMode, PerusteenOsaViitteet, Varmistusdialogi, $timeout,
        $translate, Kaanna, PerusteprojektiTiedotService, $stateParams, $rootScope) {

        document.getElementById('ylasivuankkuri').scrollIntoView(); // FIXME: Keksi tälle joku oikea ratkaisu

        $scope.versiot = {};
        $scope.sisalto = {};

        PerusteprojektiTiedotService.then(function (instance) {
          instance.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function(res) {
            $scope.sisalto = res;
          });
        });

        $scope.viiteId = function () {
          var found = _.find($scope.sisalto.lapset, function (item) {
            return item.perusteenOsa.id === $scope.tekstikappale.id;
          });
          if (found) {
            return found.id;
          }
        };

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
          function successCb(res) {
            // Päivitä versiot
            $scope.haeVersiot(true);
            SivunavigaatioService.update();
            Lukitus.vapautaPerusteenosa(res.id);
            Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
          }

          $scope.editableTekstikappale = angular.copy(kappale);
          $scope.tekstikappaleenMuokkausOtsikko = $scope.editableTekstikappale.id ? 'muokkaus-tekstikappale' : 'luonti-tekstikappale';

          Editointikontrollit.registerCallback({
            edit: function() { },
            validate: function() { return true; },
            save: function() {
              if ($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale(successCb, Notifikaatiot.serverCb);
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale, successCb, Notifikaatiot.serverCb);
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
            },
            cancel: function() {
              $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
              var tekstikappaleDefer = $q.defer();
              $scope.tekstikappalePromise = tekstikappaleDefer.promise;
              tekstikappaleDefer.resolve($scope.editableTekstikappale);
              Lukitus.vapautaPerusteenosa($scope.tekstikappale.id);
            },
            notify: function (mode) {
              $scope.editEnabled = mode;
            }
          });

          $scope.haeVersiot();
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
          Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, function() {
            Editointikontrollit.startEditing();
          });
        };

        $scope.$watch('editEnabled', function (editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.haeVersiot = function (force) {
          VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
        };

        $scope.vaihdaVersio = function () {
          VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, function (response) {
            $scope.tekstikappale = response;
            setupTekstikappale(response);
            var tekstikappaleDefer = $q.defer();
            $scope.tekstikappalePromise = tekstikappaleDefer.promise;
            tekstikappaleDefer.resolve($scope.editableTekstikappale);
          });
        };

        $scope.poista = function() {
          var nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

          Varmistusdialogi.dialogi({
            successCb: poistaminenVarmistettu,
            otsikko: 'poista-tekstikappale-otsikko',
            teksti: $translate('poista-tekstikappale-teksti', {nimi: nimi})
          })();
        };

        var poistaminenVarmistettu = function() {
          PerusteenOsaViitteet.delete({viiteId: $scope.viiteId()}, {}, function() {
            Editointikontrollit.cancelEditing();
            Notifikaatiot.onnistui('poisto-onnistui');
            $state.go('perusteprojekti.suoritustapa.sisalto', {}, {reload: true});
          }, function(virhe) {
            Notifikaatiot.varoitus(virhe);
          });
        };

        // Odota tekstikenttien alustus ennen siirtymistä editointitilaan
        var received = 0;
        $rootScope.$on('ckEditorInstanceReady', function () {
          if (++received === $scope.fields.length) {
            if (TutkinnonOsaEditMode.getMode()) {
              $timeout(function () {
                $scope.muokkaa();
              }, 50);
            }
          }
        });
      }
    };
  });


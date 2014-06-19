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
        $translate, Kaanna, PerusteprojektiTiedotService, $stateParams, SuoritustapaSisalto) {
        document.getElementById('ylasivuankkuri').scrollIntoView(); // FIXME: Keksi tälle joku oikea ratkaisu

        $scope.versiot = {};
        $scope.sisalto = {};

        PerusteprojektiTiedotService.then(function (instance) {
          instance.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function(res) {
            $scope.sisalto = res;
            $scope.setNavigation();
          });
        });

        $scope.setNavigation = function () {
          $scope.tree.init();
          SivunavigaatioService.setCrumb($scope.tree.get());
          $timeout(function () {
            SivunavigaatioService.unCollapseFor($scope.tekstikappale.id);
          }, 50);
        };

        function storeTree (sisalto, level) {
          level = level || 0;
          _.each(sisalto.lapset, function (lapsi) {
            if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
              $scope.viitteet[lapsi.perusteenOsa.id] = {};
            }
            $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
            $scope.viitteet[lapsi.perusteenOsa.id].level = level;
            if (sisalto.perusteenOsa) {
              $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
            }
            storeTree(lapsi, level + 1);
          });
        }

        $scope.tree = {
          init: function () {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
          },
          get: function () {
            var ids = [];
            var id = $scope.tekstikappale.id;
            do {
              ids.push(id);
              id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
            } while (id);
            return ids;
          }
        };

        $scope.viiteId = function () {
          return $scope.viitteet[$scope.tekstikappale.id] ? $scope.viitteet[$scope.tekstikappale.id].viite : null;
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

        function saveCb(res) {
          // Päivitä versiot
          $scope.haeVersiot(true);
          SivunavigaatioService.update();
          Lukitus.vapautaPerusteenosa(res.id);
          Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
          $scope.setNavigation();
        }

        function setupTekstikappale(kappale) {
          $scope.editableTekstikappale = angular.copy(kappale);
          $scope.tekstikappaleenMuokkausOtsikko = $scope.editableTekstikappale.id ? 'muokkaus-tekstikappale' : 'luonti-tekstikappale';

          Editointikontrollit.registerCallback({
            edit: function() { },
            validate: function() { return true; },
            save: function() {
              if ($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale(saveCb, Notifikaatiot.serverCb);
              } else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale, saveCb, Notifikaatiot.serverCb);
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
          $scope.setNavigation();
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

        $scope.canAddLapsi = function () {
          // Vain kolme tasoa hierarkiaa sallitaan
          return $scope.tekstikappale.id &&
                 $scope.viitteet[$scope.tekstikappale.id] &&
                 $scope.viitteet[$scope.tekstikappale.id].level < 2;
        };

        $scope.addLapsi = function () {
          SuoritustapaSisalto.addChild({
            perusteId: $scope.$parent.peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            perusteenosaViiteId: $scope.viiteId()
          }, {}, function (response) {
            TutkinnonOsaEditMode.setMode(true);
            $state.go('perusteprojekti.suoritustapa.perusteenosa', {
              perusteenOsanTyyppi: 'tekstikappale',
              perusteenOsaId: response._perusteenOsa
            });
          }, function (virhe) {
            Notifikaatiot.varoitus(virhe);
          });
        };

        $scope.setCrumbs = function (crumbs) {
          $scope.crumbs = crumbs;
        };

        $scope.$watch('editEnabled', function (editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.haeVersiot = function (force) {
          VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
        };

        function responseFn(response) {
          $scope.tekstikappale = response;
          setupTekstikappale(response);
          var tekstikappaleDefer = $q.defer();
          $scope.tekstikappalePromise = tekstikappaleDefer.promise;
          tekstikappaleDefer.resolve($scope.editableTekstikappale);
        }

        $scope.vaihdaVersio = function () {
          VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
        };

        $scope.revertCb = function (response) {
          responseFn(response);
          saveCb(response);
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
        $scope.$on('ckEditorInstanceReady', function () {
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


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
        tekstikappale: '=',
        versiot: '='
      },
      controller: function($scope, $q, Editointikontrollit, PerusteenOsat,
        Notifikaatiot, SivunavigaatioService, VersionHelper, Lukitus, $state,
        TutkinnonOsaEditMode, PerusteenOsaViitteet, Varmistusdialogi, $timeout,
        Kaanna, PerusteprojektiTiedotService, $stateParams, SuoritustapaSisalto) {
        document.getElementById('ylasivuankkuri').scrollIntoView(); // FIXME: Keksi tälle joku oikea ratkaisu

        $scope.sisalto = {};
        $scope.viitteet = {};

        PerusteprojektiTiedotService.then(function(instance) {
          instance.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function(res) {
            $scope.sisalto = res;
            $scope.setNavigation();
          });
        });

        $scope.setNavigation = function() {
          $scope.tree.init();
          SivunavigaatioService.setCrumb($scope.tree.get());
          $timeout(function() {
            SivunavigaatioService.unCollapseFor($scope.tekstikappale.id);
          }, 50);
          VersionHelper.setUrl($scope.versiot);
        };

        function lukitse(cb) {
          Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, cb);
        }

        function fetch(cb) {
          cb = cb || angular.noop;
          PerusteenOsat.get({ osanId: $stateParams.perusteenOsaId }, function(res) {
            $scope.tekstikappale = res;
            cb(res);
          });
        }

        function storeTree (sisalto, level) {
          level = level || 0;
          _.each(sisalto.lapset, function(lapsi) {
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
          init: function() {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
          },
          get: function() {
            var ids = [];
            var id = $scope.tekstikappale.id;
            do {
              ids.push(id);
              id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
            } while (id);
            return ids;
          }
        };

        $scope.viiteId = function() {
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

        function refreshPromise() {
          $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
          var tekstikappaleDefer = $q.defer();
          $scope.tekstikappalePromise = tekstikappaleDefer.promise;
          tekstikappaleDefer.resolve($scope.editableTekstikappale);
        }

        function saveCb(res) {
          // Päivitä versiot
          $scope.haeVersiot(true, function () {
            VersionHelper.setUrl($scope.versiot);
          });
          SivunavigaatioService.update();
          Lukitus.vapautaPerusteenosa(res.id);
          Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
          $scope.setNavigation();
        }

        function doDelete() {
          PerusteenOsaViitteet.delete({viiteId: $scope.viiteId()}, {}, function() {
            Editointikontrollit.cancelEditing();
            Notifikaatiot.onnistui('poisto-onnistui');
            $state.go('root.perusteprojekti.suoritustapa.sisalto', {}, {reload: true});
          }, Notifikaatiot.serverCb);
        }

        function setupTekstikappale(kappale) {
          $scope.editableTekstikappale = angular.copy(kappale);
          $scope.isNew = !$scope.editableTekstikappale.id;

          Editointikontrollit.registerCallback({
            edit: function() {
              fetch(function() {
                refreshPromise();
              });
            },
            asyncValidate: function(cb) {
              lukitse(function() { cb(); });
            },
            save: function() {
              if ($scope.editableTekstikappale.id) {
                $scope.editableTekstikappale.$saveTekstikappale(saveCb, Notifikaatiot.serverCb);
              }
              else {
                PerusteenOsat.saveTekstikappale($scope.editableTekstikappale, saveCb, Notifikaatiot.serverCb);
              }
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
              $scope.isNew = false;
            },
            cancel: function() {
              if ($scope.isNew) {
                doDelete();
              }
              else {
                fetch(function() {
                  refreshPromise();
                  $scope.isNew = false;
                });
              }
              Lukitus.vapautaPerusteenosa($scope.tekstikappale.id);
            },
            notify: function(mode) {
              $scope.editEnabled = mode;
            }
          });

          $scope.haeVersiot();
          $scope.setNavigation();
          Lukitus.tarkista($scope.tekstikappale.id, $scope);
        }

        if ($scope.tekstikappale) {
          $scope.tekstikappalePromise = $scope.tekstikappale.$promise.then(function(response) {
            setupTekstikappale(response);
            return $scope.editableTekstikappale;
          });
        }
        else {
          var objectReadyDefer = $q.defer();
          $scope.tekstikappalePromise = objectReadyDefer.promise;
          $scope.tekstikappale = {};
          setupTekstikappale($scope.tekstikappale);
          objectReadyDefer.resolve($scope.editableTekstikappale);
        }


        $scope.kopioiMuokattavaksi = function() {
          PerusteenOsaViitteet.kloonaaTekstikappale({
            viiteId: $scope.viitteet[$scope.tekstikappale.id].viite
          }, function(tk) {
            TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
            Notifikaatiot.onnistui('tekstikappale-kopioitu-onnistuneesti');
            $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
              perusteenOsanTyyppi: 'tekstikappale',
              perusteenOsaId: tk.perusteenOsa.id
            });
          });
        };

        $scope.muokkaa = function() {
          lukitse(function() {
            Editointikontrollit.startEditing();
          });
        };

        $scope.canAddLapsi = function() {
          return $scope.tekstikappale.id && $scope.viitteet[$scope.tekstikappale.id];
        };

        $scope.addLapsi = function() {
          SuoritustapaSisalto.addChild({
            perusteId: $scope.$parent.peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            perusteenosaViiteId: $scope.viiteId()
          }, {}, function(response) {
            TutkinnonOsaEditMode.setMode(true);
            $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
              perusteenOsanTyyppi: 'tekstikappale',
              perusteenOsaId: response._perusteenOsa
            });
          }, function(virhe) {
            Notifikaatiot.varoitus(virhe);
          });
        };

        $scope.setCrumbs = function(crumbs) {
          $scope.crumbs = crumbs;
        };

        $scope.$watch('editEnabled', function(editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.haeVersiot = function(force) {
          VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
        };

        function responseFn(response) {
          $scope.tekstikappale = response;
          setupTekstikappale(response);
          var tekstikappaleDefer = $q.defer();
          $scope.tekstikappalePromise = tekstikappaleDefer.promise;
          tekstikappaleDefer.resolve($scope.editableTekstikappale);
          VersionHelper.setUrl($scope.versiot);
        }

        $scope.vaihdaVersio = function () {
          $scope.versiot.hasChanged = true;
          VersionHelper.setUrl($scope.versiot);
          //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
        };

        $scope.revertCb = function(response) {
          responseFn(response);
          saveCb(response);
        };

        $scope.poista = function() {
          var nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

          Varmistusdialogi.dialogi({
            successCb: doDelete,
            otsikko: 'poista-tekstikappale-otsikko',
            teksti: Kaanna.kaanna('poista-tekstikappale-teksti', {nimi: nimi})
          })();
        };

        // Odota tekstikenttien alustus ennen siirtymistä editointitilaan
        var received = 0;
        $scope.$on('ckEditorInstanceReady', function() {
          if (++received === $scope.fields.length) {
            if (TutkinnonOsaEditMode.getMode()) {
              $scope.isNew = true;
              $timeout(function() {
                $scope.muokkaa();
              }, 50);
            }
          }
        });
      }
    };
  });


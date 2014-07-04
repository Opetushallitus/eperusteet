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
  .directive('muokkausTutkinnonosa', function(Notifikaatiot, Koodisto) {
    return {
      templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '='
      },
      controller: function($scope, $state, $stateParams, $q, Navigaatiopolku,
        Editointikontrollit, PerusteenOsat, Editointicatcher, PerusteenRakenne,
        PerusteTutkinnonosa, TutkinnonOsaEditMode, $timeout, Varmistusdialogi,
        SivunavigaatioService, VersionHelper, Lukitus, MuokkausUtils) {

        document.getElementById('ylasivuankkuri').scrollIntoView(); // FIXME: Keksi tälle joku oikea ratkaisu

        $scope.suoritustapa = $stateParams.suoritustapa;
        $scope.rakenne = {};
        $scope.versiot = {};
        $scope.test = angular.noop;

        function getRakenne() {
          PerusteenRakenne.hae($stateParams.perusteProjektiId, $stateParams.suoritustapa, function(res) {
            $scope.rakenne = res;
            if (TutkinnonOsaEditMode.getMode()) {
              $scope.isNew = true;
              $timeout(function () {
                $scope.muokkaa();
              }, 50);
            }
          });
        }
        getRakenne();

        function lukitse(cb) {
          Lukitus.lukitsePerusteenosa($scope.tutkinnonOsa.id, cb);
        }

        function fetch(cb) {
          cb = cb || angular.noop;
          PerusteenOsat.get({ osanId: $stateParams.perusteenOsaId }, function(res) {
            $scope.tutkinnonOsa = res;
            cb(res);
          });
        }

        $scope.viiteosa = {};
        $scope.viiteosa.laajuus = {};

        $scope.fields =
          new Array({
             path: 'tavoitteet',
             localeKey: 'tutkinnon-osan-tavoitteet',
             type: 'editor-area',
             localized: true,
             collapsible: true,
             order: 3
           },{
             path: 'ammattitaitovaatimukset',
             localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
             type: 'editor-area',
             localized: true,
             collapsible: true,
             order: 6
           },{
             path: 'ammattitaidonOsoittamistavat',
             localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
             type: 'editor-area',
             localized: true,
             collapsible: true,
             order: 7
           },{
             path: 'osaamisala',
             localeKey: 'tutkinnon-osan-osaamisala',
             type: 'editor-text',
             localized: true,
             collapsible: true,
             order: 8
           },{
             path: 'arviointi.lisatiedot',
             localeKey: 'tutkinnon-osan-arviointi-teksti',
             type: 'editor-text',
             localized: true,
             collapsible: true,
             order: 4
           },{
             path: 'arviointi.arvioinninKohdealueet',
             localeKey: 'tutkinnon-osan-arviointi-taulukko',
             type: 'arviointi',
             collapsible: true,
             order: 5
           });

        $scope.koodistoClick = Koodisto.modaali(function(koodisto) {
          MuokkausUtils.nestedSet($scope.editableTutkinnonOsa, 'koodiUri', ',', koodisto.koodi);
        }, {
          tyyppi: function() { return 'tutkinnonosat'; },
          ylarelaatioTyyppi: function() { return ''; }
        });

        $scope.editableTutkinnonOsa = {};
        $scope.editEnabled = false;

        function refreshPromise() {
          $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
          var tutkinnonOsaDefer = $q.defer();
          $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
          tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
        }

        function saveCb(res) {
          Lukitus.vapautaPerusteenosa(res.id);
          Notifikaatiot.onnistui('muokkaus-tutkinnon-osa-tallennettu');
          $scope.haeVersiot(true);
        }

        function doDelete(osaId) {
          PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.rakenne.$peruste.id,
            $stateParams.suoritustapa, function() {
            Notifikaatiot.onnistui('tutkinnon-osa-rakenteesta-poistettu');
            $state.go('perusteprojekti.suoritustapa.tutkinnonosat');
          });
        }

        function setupTutkinnonOsa(osa) {
          $scope.editableTutkinnonOsa = angular.copy(osa);
          $scope.isNew = !$scope.editableTutkinnonOsa.id;

          Editointikontrollit.registerCallback({
            edit: function() {
              fetch(function() {
                refreshPromise();
                $scope.viiteosa = _.find($scope.rakenne.tutkinnonOsat, {'_tutkinnonOsa': $scope.editableTutkinnonOsa.id.toString()}) || {};
                $scope.viiteosa.laajuus = $scope.viiteosa.laajuus || 'OSAAMISPISTE';
              });
            },
            asyncValidate: function(cb) {
              if ($scope.tutkinnonOsaHeaderForm.$valid) {
                lukitse(function() { cb(); });
              }
            },
            save: function() {
              if ($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa(function(response) {
                  $scope.editableTutkinnonOsa = angular.copy(response);
                  $scope.tutkinnonOsa = angular.copy(response);
                  Editointikontrollit.lastModified = response;
                  saveCb(response);
                  getRakenne();

                  var tutkinnonOsaDefer = $q.defer();
                  $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                  tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
                },
                Notifikaatiot.serverCb);

                // Viiteosa (laajuus) tallennetaan erikseen
                PerusteTutkinnonosa.save({
                  perusteId: $scope.rakenne.$peruste.id,
                  suoritustapa: $stateParams.suoritustapa,
                  osanId: $scope.viiteosa.id
                },
                $scope.viiteosa,
                angular.noop,
                Notifikaatiot.serverCb);
              }
              else {
                PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa, function(response) {
                  Editointikontrollit.lastModified = response;
                  saveCb(response);
                  getRakenne();
                },
                Notifikaatiot.serverCb);
              }
              Editointicatcher.give(_.clone($scope.editableTutkinnonOsa));
              $scope.isNew = false;
            },
            cancel: function() {
              if ($scope.isNew) {
                doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsa.id].id);
                $scope.isNew = false;
              }
              else {
                fetch(function() {
                  refreshPromise();
                  Lukitus.vapautaPerusteenosa($scope.tutkinnonOsa.id);
                });
              }
            },
            notify: function (mode) {
              $scope.editEnabled = mode;
            }
          });
          $scope.haeVersiot();
        }

        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsaPromise = $scope.tutkinnonOsa.$promise.then(function(response) {
            /*Navigaatiopolku.asetaElementit({
              perusteenosa: {
                nimi: response.nimi
              }
            });*/
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

        $scope.poistaTutkinnonOsa = function(osaId) {
          var onRakenteessa = PerusteenRakenne.validoiRakennetta($scope.rakenne.rakenne, function(osa) {
            return osa._tutkinnonOsaViite && $scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].id === osaId;
          });
          if (onRakenteessa) {
            Notifikaatiot.varoitus('tutkinnon-osa-rakenteessa-ei-voi-poistaa');
          } else {
            Varmistusdialogi.dialogi({
              otsikko: 'poistetaanko-tutkinnonosa',
              primaryBtn: 'poista',
              successCb: function () {
                $scope.isNew = false;
                Editointikontrollit.cancelEditing();
                doDelete(osaId);
              }
            })();
          }
        };

        $scope.muokkaa = function () {
          lukitse(function() {
            fetch(function() {
              Editointikontrollit.startEditing();
            });
          });
        };
        $scope.$watch('editEnabled', function (editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.haeVersiot = function (force) {
          VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tutkinnonOsa.id}, force);
        };

        function responseFn(response) {
          $scope.tutkinnonOsa = response;
          setupTutkinnonOsa(response);
          var objDefer = $q.defer();
          $scope.tutkinnonOsaPromise = objDefer.promise;
          objDefer.resolve($scope.editableTutkinnonOsa);
        }

        $scope.vaihdaVersio = function () {
          VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tutkinnonOsa.id}, responseFn);
        };

        $scope.revertCb = function (response) {
          responseFn(response);
          saveCb(response);
        };
      }
    };
  });

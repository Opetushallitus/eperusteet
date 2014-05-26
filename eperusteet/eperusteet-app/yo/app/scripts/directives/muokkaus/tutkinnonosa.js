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
      templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '='
      },
      controller: function($scope, $state, $stateParams, $q, Navigaatiopolku,
        Editointikontrollit, PerusteenOsat, Editointicatcher, PerusteenRakenne,
        PerusteTutkinnonosa, TutkinnonOsaEditMode, $timeout, Varmistusdialogi,
        SivunavigaatioService, VersionHelper, Lukitus) {

        $scope.suoritustapa = $stateParams.suoritustapa;
        $scope.rakenne = {};
        $scope.versiot = {};

        PerusteenRakenne.hae($stateParams.perusteProjektiId, $stateParams.suoritustapa, function(res) {
          $scope.rakenne = res;
          if (TutkinnonOsaEditMode.getMode()) {
            $timeout(function () {
              $scope.muokkaa();
            }, 50);
          }
        });
        $scope.viiteosa = {};

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
             collapsible: true,
             order: 3
           },{
             path: 'ammattitaitovaatimukset',
             localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
             type: 'editor-area',
             localized: true,
             collapsible: true,
             order: 4
           },{
             path: 'ammattitaidonOsoittamistavat',
             localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
             type: 'editor-text',
             localized: true,
             collapsible: true,
             order: 5
           },{
             path: 'osaamisala',
             localeKey: 'tutkinnon-osan-osaamisala',
             type: 'editor-text',
             localized: true,
             collapsible: true,
             order: 6
           },{
             path: 'arviointi',
             localeKey: 'tutkinnon-osan-arviointi',
             type: 'arviointi',
             collapsible: true,
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
          function successCb(res) {
            Lukitus.vapauta(res.id);
            Notifikaatiot.onnistui('muokkaus-tutkinnon-osa-tallennettu');
            $state.go('perusteprojekti.suoritustapa.tutkinnonosat');
          }

          $scope.editableTutkinnonOsa = angular.copy(osa);


          $scope.tutkinnonOsanMuokkausOtsikko = $scope.editableTutkinnonOsa.id ? $scope.editableTutkinnonOsa.nimi : 'luonti-tutkinnon-osa';

          Editointikontrollit.registerCallback({
            edit: function() {
              $scope.viiteosa = _.clone($scope.rakenne.tutkinnonOsat[$scope.editableTutkinnonOsa.id] || {});
              $scope.viiteosa.yksikko = $scope.viiteosa.yksikko || 'OSAAMISPISTE';
            },
            validate: function() {
              return $scope.tutkinnonOsaHeaderForm.$valid;
            },
            save: function() {
              //TODO: Validate tutkinnon osa
              cleanAccordionData($scope.editableTutkinnonOsa.arviointi);
              if ($scope.editableTutkinnonOsa.id) {
                $scope.editableTutkinnonOsa.$saveTutkinnonOsa(function(response) {
                  $scope.editableTutkinnonOsa = angular.copy(response);
                  $scope.tutkinnonOsa = angular.copy(response);
                  Editointikontrollit.lastModified = response;
                  successCb(response);

                  // FIXME: Näillä ei mitään virkaa?
                  var tutkinnonOsaDefer = $q.defer();
                  $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                  tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
                },
                Notifikaatiot.serverCb);

                // Viiteosa (laajuus) tallennetaan erikseen
                PerusteTutkinnonosa.save({
                  perusteenId: $scope.rakenne.$peruste.id,
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
                  successCb(response);
                },
                Notifikaatiot.serverCb);
              }

              Editointicatcher.give(_.clone($scope.editableTutkinnonOsa));
              $scope.haeVersiot(true);

            },
            cancel: function() {
              $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);

              // FIXME: Näillä ei mitään virkaa?
              var tutkinnonOsaDefer = $q.defer();
              $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
              tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
              Lukitus.vapauta($scope.tutkinnonOsa.id);
            },
            notify: function (mode) {
              $scope.editEnabled = mode;
            }
          });
          $scope.haeVersiot();
        }

        if($scope.tutkinnonOsa) {
          $scope.tutkinnonOsaPromise = $scope.tutkinnonOsa.$promise.then(function(response) {
            Navigaatiopolku.asetaElementit({
              perusteenosa: {
                nimi: response.nimi
              }
            });
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
            return osa._tutkinnonOsa && $scope.rakenne.tutkinnonOsat[osa._tutkinnonOsa].id === osaId;
          });
          if (onRakenteessa) {
            Notifikaatiot.varoitus('tutkinnon-osa-rakenteessa-ei-voi-poistaa');
          } else {
            Varmistusdialogi.dialogi({
              otsikko: 'poistetaanko-tutkinnonosa',
              primaryBtn: 'poista',
              successCb: function () {
                Editointikontrollit.cancelEditing();
                PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.rakenne.$peruste.id,
                  $stateParams.suoritustapa, function() {
                  Notifikaatiot.onnistui('tutkinnon-osa-rakenteesta-poistettu');
                  $state.go('perusteprojekti.suoritustapa.tutkinnonosat');
                });
              }
            })();
          }
        };

        $scope.muokkaa = function () {
          Lukitus.lukitse($scope.tutkinnonOsa.id, function() {
            Editointikontrollit.startEditing();
          });
        };
        $scope.$watch('editEnabled', function (editEnabled) {
          SivunavigaatioService.aseta({osiot: !editEnabled});
        });

        $scope.haeVersiot = function (force) {
          VersionHelper.getVersions($scope.versiot, $scope.tutkinnonOsa.id, force);
        };

        $scope.vaihdaVersio = function () {
          VersionHelper.change($scope.versiot, $scope.tutkinnonOsa.id, function (response) {
            $scope.tutkinnonOsa = response;
            setupTutkinnonOsa(response);
            var objDefer = $q.defer();
            $scope.tutkinnonOsaPromise = objDefer.promise;
            objDefer.resolve($scope.editableTutkinnonOsa);
          });
        };

      }
    };
  });

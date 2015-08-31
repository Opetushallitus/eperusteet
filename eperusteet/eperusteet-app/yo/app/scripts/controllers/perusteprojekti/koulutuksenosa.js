/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
*
* This program is free software: Licensed under the EUPL
* Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
*
* This program is distributed in the hope that it will be usefu
* l
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('muokkausKoulutuksenosaCtrl', function($scope, Utils, Kommentit, $stateParams, KommentitByPerusteenOsa, $state,
        muokkausCtrlCommons, Editointikontrollit, VersionHelper, virheService, TutkinnonOsaViitteet, Algoritmit, rakenne,
        Lukitus, PerusteenOsaViite, $q, Tutke2Service, Notifikaatiot, PerusteenRakenne, TutkinnonOsaEditMode, PerusteTutkinnonosa,
        PerusteenOsat, ProjektinMurupolkuService) {
    Utils.scrollTo('#ylasivuankkuri');

    function refreshPromise() {
      $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus = $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus || {};
      $scope.editableTutkinnonOsaViite = angular.copy($scope.tutkinnonOsaViite);
      tutkinnonOsaDefer = $q.defer();
      $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
      tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
    }

    function doDelete(osaId) {
      PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.peruste.id, $stateParams.suoritustapa, function() {
        Notifikaatiot.onnistui('koulutuksen-osa-rakenteesta-poistettu');
        $state.go('root.perusteprojekti.suoritustapa.koulutuksenosat');
      });
    }

    function saveCb(res) {
      Lukitus.vapautaPerusteenosa(res.id);
      ProjektinMurupolkuService.set('tutkinnonOsaViiteId', $scope.tutkinnonOsaViite.id, $scope.tutkinnonOsaViite.tutkinnonOsa.nimi);
      Notifikaatiot.onnistui('muokkaus-koulutuksen-osa-tallennettu');
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
    }

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, cb);
    }

    var fetch = muokkausCtrlCommons.scoped($scope).fetch;
    var tutkinnonOsaDefer = $q.defer();
    $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

    { // Defaults
      muokkausCtrlCommons.setupScope($scope, doDelete);
      Kommentit.haeKommentit(KommentitByPerusteenOsa, {
        id: $stateParams.perusteProjektiId,
        perusteenOsaId: $stateParams.tutkinnonOsaViiteId
      });
      $scope.$laajuusRangena = false;
      $scope.toggleLaajuusRange = function() {
        $scope.$laajuusRangena = !$scope.$laajuusRangena;
      };
      $scope.tutkinnonOsaViite = {};
      $scope.versiot = {};
      $scope.suoritustapa = $stateParams.suoritustapa;
      $scope.rakenne = rakenne;
      $scope.test = angular.noop;
      $scope.editableTutkinnonOsaViite = {};
      $scope.editEnabled = false;
      $scope.editointikontrollit = Editointikontrollit;
      $scope.nimiValidationError = false;
      $scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko($scope.peruste, $scope.suoritustapa);

      $scope.getTosa = function() {
        return !$scope.rakenne.tutkinnonOsat ? undefined : $scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id];
      };

      { // Versionhallinta
        $scope.haeVersiot = function(force, cb) {
          VersionHelper.getTutkinnonOsaViiteVersions($scope.versiot, {id: $scope.tutkinnonOsaViite.id}, force, cb);
        };

        var errorCb = function() {
          virheService.virhe('virhe-koulutuksen-ei-löytynyt');
        };

        var successCb = function(re) {
          re.tyyppi = 'tutke2';
          setupTutkinnonOsaViite(re);
          tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
          $scope.$laajuusRangena = $scope.editableTutkinnonOsaViite.laajuusMaksimi > 0;
          if (TutkinnonOsaEditMode.getMode()) {
            $scope.isNew = true;
            $scope.muokkaa();
          }
        };

        var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
        if (versio) {
          VersionHelper.getTutkinnonOsaViiteVersions($scope.versiot, {id: $stateParams.tutkinnonOsaViiteId}, true, function () {
            var revNumber = VersionHelper.select($scope.versiot, versio);
            if (!revNumber) {
              errorCb();
            }
            else {
              TutkinnonOsaViitteet.getVersio({
                viiteId: $stateParams.tutkinnonOsaViiteId,
                versioId: revNumber
              }, successCb, errorCb);
            }
          });
        }
        else {
          PerusteenOsaViite.get({
            perusteId: $scope.peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            viiteId: $stateParams.tutkinnonOsaViiteId
          }, successCb, errorCb);
        }
      }
    }

    $scope.muokkaa = function () {
      Editointikontrollit.registerCallback(editointikontrollit);
      lukitse(function() {
        fetch(function() {
          Editointikontrollit.startEditing();
          refreshPromise();
        });
      });
    };

    var editointikontrollit = {
      edit: function() {
        console.log('editing');
        console.log(_.clone($scope.editableTutkinnonOsaViite.tutkinnonOsa));
        Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
      },
      asyncValidate: function(done) {
        if ($scope.$laajuusRangena &&
            $scope.editableTutkinnonOsaViite.laajuusMaksimi &&
            $scope.editableTutkinnonOsaViite.laajuus >= $scope.editableTutkinnonOsaViite.laajuusMaksimi) {
          Notifikaatiot.varoitus('laajuuden-maksimi-ei-voi-olla-pienempi-tai-sama-kuin-minimi');
        }
        else {
          done();
        }
      },
      save: function(kommentti) {
        if (!$scope.$laajuusRangena || !$scope.editableTutkinnonOsaViite.laajuusMaksimi) {
          $scope.editableTutkinnonOsaViite.laajuusMaksimi = undefined;
          $scope.$laajuusRangena = false;
        }

        Tutke2Service.mergeOsaAlueet($scope.editableTutkinnonOsaViite.tutkinnonOsa);
        $scope.editableTutkinnonOsaViite.metadata = { kommentti: kommentti };

        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.id) {
          PerusteTutkinnonosa.save({
            perusteId: $scope.peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            osanId: $scope.editableTutkinnonOsaViite.tutkinnonOsa.id
          },
          $scope.editableTutkinnonOsaViite, function(response){
            $scope.editableTutkinnonOsaViite = angular.copy(response);
            $scope.tutkinnonOsaViite = angular.copy(response);
            Editointikontrollit.lastModified = response;
            saveCb(response.tutkinnonOsa);
            // getRakenne();

            tutkinnonOsaDefer = $q.defer();
            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
          }, Notifikaatiot.serverCb);
        }
        else {
          PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsaViite.tutkinnonOsa, function(response) {
            Editointikontrollit.lastModified = response;
            saveCb(response);
            // getRakenne();
          }, Notifikaatiot.serverCb);
        }
        $scope.isNew = false;
      },
      cancel: function() {
        if ($scope.isNew) {
          doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id].id);
          $scope.isNew = false;
        }
        else {
          Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
          fetch(function() {
            refreshPromise();
            Lukitus.vapautaPerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id);
          });
        }
      },
      notify: function(mode) {
        $scope.editEnabled = mode;
      },
      validate: function() {
        return true;
        // if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
        //   $scope.nimiValidationError = true;
        // }
        // return $scope.tutkinnonOsaHeaderForm.$valid && Tutke2Service.validate($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
      }
    };

    var setupTutkinnonOsaViite = muokkausCtrlCommons.setupTutkinnonOsaViite($scope, editointikontrollit);

  });

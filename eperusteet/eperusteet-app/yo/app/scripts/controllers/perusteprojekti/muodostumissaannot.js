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
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $stateParams,
    PerusteenRakenne, Notifikaatiot, Editointikontrollit, SivunavigaatioService, PerusteProjektiService,
    Kommentit, KommentitBySuoritustapa, Lukitus, VersionHelper, Muodostumissaannot,
    virheService) {
    $scope.editoi = false;
    // $scope.peruste = PerusteProjektiService.getPeruste();
    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };
    $scope.versiot = {};
    $scope.isLocked = false;

    Kommentit.haeKommentit(KommentitBySuoritustapa, {id: $stateParams.perusteProjektiId, suoritustapa: $scope.suoritustapa});

    function lukitse(cb) {
      Lukitus.lukitseSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa, cb);
    }

    var errorCb = function() {
      virheService.virhe('virhe-perusteenosaa-ei-löytynyt');
    };
    var successCb = function (res) {
      res.$suoritustapa = $scope.suoritustapa;
      res.$resolved = true;
      $scope.rakenne = res;
      Muodostumissaannot.laskeLaajuudet($scope.rakenne.rakenne, $scope.rakenne.tutkinnonOsaViitteet, $scope.rakenne.tutkinnonOsat);
      haeVersiot();
      Lukitus.tarkista($scope.rakenne.$peruste.id, $scope, $scope.suoritustapa);
    };

    function haeRakenne(cb, versio) {
      cb = cb || angular.noop;
      PerusteenRakenne.haeByPerusteprojekti($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        successCb(res);
        if (versio) {
          haeVersiot(true, function () {
            var revNumber = VersionHelper.select($scope.versiot, versio);
            if (!revNumber) {
              errorCb();
            } else {
              $scope.vaihdaVersio(cb);
            }
          });
        } else {
          cb();
        }
      });
    }
    $scope.haeRakenne = haeRakenne;
    var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
    haeRakenne(angular.noop, versio);

    function tallennaRakenne(rakenne) {
      PerusteenRakenne.tallennaRakenne(
        rakenne,
        rakenne.$peruste.id,
        $scope.suoritustapa,
        function() {
          Notifikaatiot.onnistui('tallennus-onnistui');
          haeVersiot(true);
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        },
        function() {
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        }
      );
      $scope.isLocked = false;
    }

    function haeVersiot(force, cb) {
      VersionHelper.getRakenneVersions($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, force, cb);
    }

    $scope.vaihdaVersio = function(cb) {
      cb = cb || angular.noop;
      $scope.versiot.hasChanged = true;
      // Ideally we would reload the data and rewrite version to url without changing state
      VersionHelper.setUrl($scope.versiot, true);
      /*VersionHelper.changeRakenne($scope.versiot, {id: $scope.rakenne.$peruste.id, suoritustapa: $scope.suoritustapa}, function(response) {
        $scope.rakenne.rakenne = response;
        VersionHelper.setUrl($scope.versiot, true);
        cb();
      });*/
    };

    $scope.revert = function () {
      haeRakenne(function () {
        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        haeVersiot(true);
      });
    };

    $scope.muokkaa = function() {
      lukitse(function() {
        haeRakenne(function() {
          Muodostumissaannot.validoiRyhma($scope.rakenne.rakenne, $scope.rakenne.tutkinnonOsaViitteet, $scope.tutkinnonOsat);
          Editointikontrollit.startEditing();
          $scope.editoi = true;
        });
      });
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.editoi = true;
      },
      asyncValidate: function(cb) {
        lukitse(function() {
          cb();
        });
      },
      save: function(kommentti) {
        $scope.rakenne.metadata = { kommentti: kommentti };
        console.log($scope.rakenne);
        tallennaRakenne($scope.rakenne);
        $scope.editoi = false;
      },
      cancel: function() {
        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        haeRakenne(function() {
          $scope.editoi = false;
        });
      }
    });

    $scope.$watch('rakenne.rakenne', function(uusirakenne) {
      if ($scope.editoi) {
        Muodostumissaannot.laskeLaajuudet(uusirakenne, $scope.rakenne.tutkinnonOsaViitteet);
        Muodostumissaannot.validoiRyhma(uusirakenne, $scope.rakenne.tutkinnonOsaViitteet);
      }
    }, true);

    $scope.$watch('editoi', function(editoi) {
      SivunavigaatioService.aseta({osiot: !editoi});
    });
  });

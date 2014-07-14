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
    PerusteenRakenne, Notifikaatiot, Editointikontrollit, SivunavigaatioService,
    Kommentit, KommentitBySuoritustapa, Lukitus, VersionHelper, Muodostumissaannot,
    virheService) {
    $scope.editoi = false;
    // $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };
    $scope.versiot = {};

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
      Muodostumissaannot.laskeLaajuudet($scope.rakenne.rakenne, $scope.rakenne.tutkinnonOsat);
      haeVersiot();
    };

    function haeRakenne(cb, versio) {
      cb = cb || angular.noop;
      PerusteenRakenne.hae($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
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
        },
        function() {
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        }
      );
    }

    function haeVersiot(force, cb) {
      VersionHelper.getRakenneVersions($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, force, cb);
    }

    $scope.vaihdaVersio = function(cb) {
      cb = cb || angular.noop;
      $scope.versiot.hasChanged = true;
      VersionHelper.changeRakenne($scope.versiot, {id: $scope.rakenne.$peruste.id, suoritustapa: $scope.suoritustapa}, function(response) {
        $scope.rakenne.rakenne = response;
        VersionHelper.setUrl($scope.versiot, true);
        cb();
      });
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
          Muodostumissaannot.validoiRyhma($scope.rakenne.rakenne, $scope.tutkinnonOsat);
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
      save: function() {
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
        Muodostumissaannot.laskeLaajuudet(uusirakenne, $scope.rakenne.tutkinnonOsat);
        Muodostumissaannot.validoiRyhma(uusirakenne, $scope.tutkinnonOsat);
      }
    }, true);

    $scope.$watch('editoi', function(editoi) {
      SivunavigaatioService.aseta({osiot: !editoi});
    });
  });

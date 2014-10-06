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
  .controller('MuodostumisryhmaModalCtrl', function($scope, $modalInstance, ryhma,
    vanhempi, suoritustapa, leikelauta, Varmistusdialogi, YleinenData, Koodisto, Utils) {

    $scope.vanhempi = vanhempi;
    $scope.leikelauta = leikelauta;
    $scope.suoritustapa = suoritustapa;
    $scope.roolit = _.map(YleinenData.rakenneRyhmaRoolit, function(rooli) {
      return { value: rooli, label: rooli };
    });

    $scope.luonti = !_.isObject(ryhma);

    (function setupRyhma() {
      $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
      $scope.osaamisala = ryhma && ryhma.osaamisala ? angular.copy(ryhma.osaamisala) : {};
      $scope.ryhma.rooli = $scope.ryhma.rooli || YleinenData.rakenneRyhmaRoolit[0];
      $scope.ryhma.muodostumisSaanto = $scope.ryhma.muodostumisSaanto || {};

      if (!$scope.ryhma.muodostumisSaanto) {
        $scope.ryhma.muodostumisSaanto = {};
      }
      if (!$scope.ryhma.nimi) {
        $scope.ryhma.nimi = {};
      }
      // if (!$scope.ryhma.kuvaus) {
      //   $scope.ryhma.kuvaus = {};
      // }
    })();

    var koodistoHaku = function (koodisto) {
      $scope.osaamisala = {};
      $scope.osaamisala.nimi = koodisto.nimi;
      $scope.osaamisala.osaamisalakoodiArvo = koodisto.koodiArvo;
      $scope.osaamisala.osaamisalakoodiUri = koodisto.koodiUri;
      if (!Utils.hasLocalizedText($scope.ryhma.nimi)) {
        $scope.ryhma.nimi = $scope.osaamisala.nimi;
      }
    };

    $scope.avaaKoodistoModaali = function() {
      Koodisto.modaali(function(koodi) {
        koodistoHaku(koodi);
      }, {
        tyyppi: function() {return 'osaamisala'; },
        ylarelaatioTyyppi: function() { return ''; }
      }, angular.noop, null)();
    };

    $scope.tyhjennaOsaamisala = function () {
      $scope.osaamisala = {};
    };

    $scope.ok = function(uusiryhma) {
      if (uusiryhma) {
        if (uusiryhma.osat === undefined) {
          uusiryhma.osat = [];
        }
        if (uusiryhma.muodostumisSaanto.laajuus) {
          var ml = uusiryhma.muodostumisSaanto.laajuus;
          ml.maksimi = ml.minimi && (!ml.maksimi || ml.minimi > ml.maksimi) ? ml.minimi : ml.maksimi;
        }
        if (!_.isEmpty($scope.osaamisala)) {
          uusiryhma.rooli = YleinenData.osaamisalaRooli;
          uusiryhma.osaamisala = $scope.osaamisala;
        }
      }
      $modalInstance.close(uusiryhma);
    };

    $scope.poista = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'poistetaanko-ryhma',
        successCb: function () {
          $scope.ok(null);
        }
      })();
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };

    $scope.$watch('ryhma.rooli', function(rooli) {
      if (rooli !== YleinenData.osaamisalaRooli) {
        $scope.ryhma.osaamisala = {};
        $scope.osaamisala = {};
      }
    });
  });

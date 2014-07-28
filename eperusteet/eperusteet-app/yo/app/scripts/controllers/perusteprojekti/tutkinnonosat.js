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
/* global _ */

angular.module('eperusteApp')
  .controller('PerusteprojektiTutkinnonOsatCtrl', function($scope, $rootScope, $state, $stateParams,
    Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, Notifikaatiot,
    Editointikontrollit, Kaanna, PerusteTutkinnonosa, TutkinnonOsanTuonti, TutkinnonOsaEditMode) {

    $scope.editoi = false;
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.tosarajaus = '';
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };

    $scope.paivitaRajaus = function(rajaus) { $scope.tosarajaus = rajaus; };

    function haeRakenne() {
      PerusteenRakenne.hae($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        res.$suoritustapa = $scope.suoritustapa;
        res.$resolved = true;
        $scope.rakenne = res;
      });
    }
    $scope.haeRakenne = haeRakenne;
    haeRakenne();

    function tallennaTutkinnonosat(rakenne) {
      PerusteenRakenne.tallennaTutkinnonosat(
        rakenne,
        rakenne.$peruste.id,
        $scope.suoritustapa,
        function() { Notifikaatiot.onnistui(); },
        Notifikaatiot.serverCb
      );
    }

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.editoi = true;
      },
      validate: function() {
        console.log('Tutkinnon rakenteelta puuttuu validointi. Toteuta.');
        return true;
      },
      save: function() {
        tallennaTutkinnonosat($scope.rakenne);
        $scope.editoi = false;
      },
      cancel: function() {
        haeRakenne();
        $scope.editoi = false;
      }
    });

    $scope.rajaaTutkinnonOsia = function(haku) {
      return Kaanna.kaanna(haku.nimi).toLowerCase().indexOf($scope.tosarajaus.toLowerCase()) !== -1;
    };

    $scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali($scope.suoritustapa, function(osat) {
      _.forEach(osat, function(osa) { $scope.lisaaTutkinnonOsa(osa); });
    });

    $scope.navigoiTutkinnonosaan = function (osa) {
      $state.go('perusteprojekti.suoritustapa.perusteenosa', {
        perusteenOsaId: osa._tutkinnonOsa,
        perusteenOsanTyyppi: 'tutkinnonosa'
      });
    };

    $scope.lisaaTutkinnonOsa = function(osa, cb) {
      osa = osa ? {_tutkinnonOsa: osa._tutkinnonOsa} : {};
      cb = cb || angular.noop;

      PerusteTutkinnonosa.save({
        perusteenId: $scope.rakenne.$peruste.id,
        suoritustapa: $scope.rakenne.$suoritustapa
      }, osa,
      function(res) {
        $scope.rakenne.tutkinnonOsat[res._tutkinnonOsa] = res;
        cb();
        TutkinnonOsaEditMode.setMode(true);
        $scope.navigoiTutkinnonosaan(res);
      },
      function(err) {
        Notifikaatiot.fataali('tallennus-epäonnistui', err);
        cb();
      });
    };
  });

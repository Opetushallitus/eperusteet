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
  .controller('PerusteprojektisisaltoCtrl', function($scope, $state, $stateParams,
    SuoritustapaSisalto, PerusteProjektiService, perusteprojektiTiedot, TutkinnonOsaEditMode, Notifikaatiot, YleinenData, Kaanna) {

    function kaikilleTutkintokohtaisilleOsille(juuri, cb) {
      var lapsellaOn = false;
      _.forEach(juuri.lapset, function(osa) {
        lapsellaOn = kaikilleTutkintokohtaisilleOsille(osa, cb) || lapsellaOn;
      });
      return cb(juuri, lapsellaOn) || lapsellaOn;
    }

    $scope.rajaus = '';
    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();

    kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, function(osa) {
      osa.$opened = false;
    });

    $scope.valittuSuoritustapa = PerusteProjektiService.getSuoritustapa();

    $scope.aakkosJarjestys = function(data) { return Kaanna.kaanna(data.perusteenOsa.nimi); };

    $scope.rajaaSisaltoa = function() {
      kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || YleinenData.rajausVertailu($scope.rajaus, osa, 'perusteenOsa', 'nimi');
        return osa.$filtered;
      });
    };
    $scope.rajaaSisaltoa();

    $scope.createSisalto = function() {
      SuoritustapaSisalto.save({perusteId: $scope.projekti._peruste, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {}, function(response) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        $scope.navigoi('perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tekstikappale',
          perusteenOsaId: response._perusteenOsa
        });
      },
      Notifikaatiot.serverCb);
    };

    // scope.sortableOptions = {
    //   connectWith: '.',
    //   cursor: 'move',
    //   cursorAt: { top : 2, left: 2 },
    //   delay: 100,
    //   disabled: !scope.muokkaus,
    //   placeholder: 'placeholder',
    //   tolerance: 'pointer',
    //   // start: function(e, ui) {
    //   //   ui.placeholder.html('<div class="group-placeholder"></div>');
    //   // },
    //   // cancel: '.ui-state-disabled',
    // };

    $scope.vaihdaSuoritustapa = function(suoritustapakoodi) {
      $scope.valittuSuoritustapa = suoritustapakoodi;
      PerusteProjektiService.setSuoritustapa(suoritustapakoodi);

      $state.go('perusteprojekti.suoritustapa.sisalto', {
        perusteProjektiId: $stateParams.perusteProjektiId,
        suoritustapa: suoritustapakoodi
      });
    };

    $scope.navigoi = function (state, params) {
      $state.go(state, params);
    };
  });

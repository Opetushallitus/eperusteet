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
  .controller('LukiokoulutussisaltoController',
  function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto, LukioKurssiService,
      LukiokoulutusService, TekstikappaleOperations, Editointikontrollit, $stateParams, Notifikaatiot, Utils, $log) {

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    TekstikappaleOperations.setPeruste($scope.peruste);
    $scope.rajaus = '';

    $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();
    $scope.$esitysurl = $state.href('root.selaus.lukiokoulutus', {
      perusteId: $scope.peruste.id
    });

    $scope.$watch('peruste.sisalto', function () {
      if( !_.isEmpty($scope.peruste.sisalto) ) {
        Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function () {
          $state.href('root.perusteprojekti.suoritustapa.lukioosat', {
            osanTyyppi: 'osaaminen'
          });
        });
      }
    }, true);

    $scope.datat = {
      opetus: {lapset: []},
      sisalto: perusteprojektiTiedot.getYlTiedot().sisalto
    };

    $scope.$watch('datat.opetus.lapset', function () {
      _.each($scope.datat.opetus.lapset, function (area) {
        area.$type = 'ep-parts';
        area.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: area.tyyppi});

        area.$orderFn = Utils.nameSort;

        Algoritmit.kaikilleLapsisolmuille(area, 'lapset', function (lapsi) {
          lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {osanTyyppi: area.tyyppi, osanId: lapsi.id, tabId: 0});
          if (lapsi.koosteinen) {
            lapsi.lapset = _.sortBy(lapsi.oppimaarat, Utils.nameSort);
          }
        });
      });
    }, true);

    // TODO käytä samaa APIa kuin sivunavissa, koko sisältöpuu kerralla
    _.each(LukiokoulutusService.sisallot, function (item) {
      var data = {
        nimi: item.label,
        tyyppi: item.tyyppi
      };
      LukiokoulutusService.getOsat(item.tyyppi, true).then(function (res) {
        data.lapset = res;
      });
      $scope.datat.opetus.lapset.push(data);
    });
    $scope.peruste.sisalto = $scope.datat.sisalto;

    $scope.rajaaSisaltoa = function(value) {
      if (_.isUndefined(value)) { return; }
      var sisaltoFilterer = function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'perusteenOsa', 'nimi');
        return osa.$filtered;
      };
      var filterer = function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'nimi');
        return osa.$filtered;
      };
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.opetus, filterer);
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.sisalto, sisaltoFilterer);
    };

    $scope.avaaSuljeKaikki = function(value) {
      var open = _.isUndefined(value) ? false : !value;
      if (_.isUndefined(value)) {
        Algoritmit.kaikilleLapsisolmuille($scope.datat.opetus, 'lapset', function(lapsi) {
          open = open || lapsi.$opened;
        });
      }
      Algoritmit.kaikilleLapsisolmuille($scope.datat.sisalto, 'lapset', function(lapsi) {
        lapsi.$opened = !open;
      });
      Algoritmit.kaikilleLapsisolmuille($scope.datat.opetus, 'lapset', function(lapsi) {
        lapsi.$opened = !open;
      });
    };

    $scope.addTekstikappale = function () {
      TekstikappaleOperations.add();
    };

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.rajaus = '';
        $scope.avaaSuljeKaikki(true);
      },
      save: function() {
        TekstikappaleOperations.updateViitteet($scope.peruste.sisalto, function () {
          Notifikaatiot.onnistui('osien-rakenteen-päivitys-onnistui');
        });
      },
      cancel: function() {
        $state.go($state.current.name, $stateParams, {
          reload: true
        });
      },
      validate: function() { return true; },
      notify: function (value) {
        $scope.editing = value;
      }
    });

  }
)
.controller('LukioOsalistausController', function ($scope, $state, $stateParams, LukiokoulutusService,
                                                virheService, LukioKurssiService, $log) {
    $scope.sisaltoState = _.find(
      LukiokoulutusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      $log.error('LukioOsalistausController osaTyyppi: '+ $stateParams.osanTyyppi);
      virheService.virhe('virhe-sivua-ei-löytynyt');
      return;
    }

    $scope.kurssit = [];
    $scope.aihekokonaisuudet = [];
    $scope.osaAlueet = [];
    LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function (res) {
      $scope.osaAlueet = res;
      if ($scope.isOppiaineet()) {
        LukioKurssiService.listByPeruste($scope.peruste.id).then(function(kurssit) {
          $scope.kurssit = kurssit;
        });
      }
    });
    $scope.isOppiaineet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    };

    $scope.isAihekokonaisuus = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.AIHEKOKONAISUUDET;
    };

    $scope.isOpetuksenTavoitteet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPETUKSEN_YLEISET_TAVOITTEET;
    };

    $scope.options = {};

    $scope.createUrl = function (value) {
      return $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: value.id,
        tabId: 0
      });
    };

    $scope.add = function () {
      if ($stateParams.osanTyyppi === LukiokoulutusService.KURSSIT) {
        $state.go('root.perusteprojekti.suoritustapa.lisaaLukioKurssi');
        return;
      }
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: 'uusi',
        tabId: 0
      });
    };
  })
  .controller('LukioOsaAlueController', function ($scope, $q, $stateParams, LukiokoulutusService,
                                                  ProjektinMurupolkuService) {
    $scope.isOppiaine = $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    $scope.isAihekokonaisuus = $stateParams.osanTyyppi === LukiokoulutusService.AIHEKOKONAISUUDET;
    $scope.versiot = {latest: true};
    $scope.dataObject = LukiokoulutusService.getOsa($stateParams);
    var labels = _.invert(LukiokoulutusService.LABELS);
    ProjektinMurupolkuService.set('osanTyyppi', $stateParams.osanTyyppi, labels[$stateParams.osanTyyppi]);
    $scope.dataObject.then(function (res) {
      ProjektinMurupolkuService.set('osanId', $stateParams.osanId, res.nimi);
    });
  })


  // --------------------------------------------------------------------------------------------------------
  // Kurssit
  // --------------------------------------------------------------------------------------------------------

  .controller('LisaaLukioKurssiController', function($scope,
                                                     $state,
                                                     $q,
                                                     $stateParams,
                                                     LukiokoulutusService,
                                                     LukioKurssiService,
                                                     YleinenData,
                                                     MuokkausUtils,
                                                     Koodisto) {
    $scope.kurssityypit = [];
    function init() {
      $scope.kurssi = {
        nimi: {fi: ''},
        tyyppi: 'PAKOLLINEN',
        koodiUri: null,
        koodiArvo: null
      };
      YleinenData.lukioKurssityypit().then(function(tyypit) {
        $scope.kurssityypit = tyypit;
      });
    }
    init();

    $scope.openKoodisto = Koodisto.modaali(function(koodisto) {
      MuokkausUtils.nestedSet($scope.kurssi, 'koodiUri', ',', koodisto.koodiUri);
      MuokkausUtils.nestedSet($scope.kurssi, 'koodiArvo', ',', koodisto.koodiArvo);
    }, {
      tyyppi: function() { return 'lukionkurssit'; },
      ylarelaatioTyyppi: function() { return ''; },
      tarkista: _.constant(true)
    });

    $scope.save = function() {
      LukioKurssiService.save($scope.kurssi).then(function() {
        $scope.back();
      });
    };

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: LukiokoulutusService.KURSSIT});
    };
  });

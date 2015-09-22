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
  function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto,
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
      Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function (lapsi) {
        $state.href('root.perusteprojekti.suoritustapa.lukioosat', {
          osanTyyppi: 'osaaminen'
        });
      });
    }, true);

    $scope.datat = {
      opetus: {lapset: []},
      sisalto: perusteprojektiTiedot.getYlTiedot().sisalto
    };

    $scope.$watch('datat.opetus.lapset', function () {
      _.each($scope.datat.opetus.lapset, function (area) {
        area.$type = 'ep-parts';
        area.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: area.tyyppi});
        $log.info("URL: " + area.$url + ", " + area.tyyppi);

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
).controller('LukioOsalistausController', function ($scope, $state, $stateParams, LukiokoulutusService,
                                                virheService) {
    $scope.sisaltoState = _.find(LukiokoulutusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      $log.error("LukioOsalistausController osaTyyppi: "+ $stateParams.osanTyyppi);
      virheService.virhe({
        muu:'virhe-sivua-ei-löytynyt',
        state: $stateParams.osanTyyppi
      });
      return;
    }
    var vuosiluokkakokonaisuudet = [];
    $scope.osaAlueet = [];
    LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function (res) {
      $scope.osaAlueet = res;
    });
    //if ($stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT) {
    //  LukiokoulutusService.getOsat(PerusopetusService.OPPIAINEET_OPPIMAARAT, true).then(function (res) {
    //    vuosiluokkakokonaisuudet = res;
    //  });
    //}

    $scope.options = {};

    $scope.createUrl = function (value) {
      return $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {
        suoritustapa: $stateParams.suoritustapa,
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: value.id,
        tabId: 0
      });
    };

    $scope.add = function () {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        suoritustapa: $stateParams.suoritustapa,
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
;

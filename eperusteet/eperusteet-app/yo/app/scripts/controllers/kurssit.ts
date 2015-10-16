/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

'use strict';

angular.module('eperusteApp')
  .service('LukiokurssiModifyHelpers', function(Koodisto, MuokkausUtils, $translate, $filter, Kieli) {
    var openKoodisto = function(kurssi) {
      return Koodisto.modaali(function(koodisto) {
        MuokkausUtils.nestedSet(kurssi, 'koodiUri', ',', koodisto.koodiUri);
        MuokkausUtils.nestedSet(kurssi, 'koodiArvo', ',', koodisto.koodiArvo);
      }, {
        tyyppi: function() { return 'lukionkurssit'; },
        ylarelaatioTyyppi: function() { return ''; },
        tarkista: _.constant(true)
      });
    };
    var getDefaultTavoitteet = function() {
      // TODO: ei tosiaan taida olla mitään tapaa saada $translatelta tietyn id:n käännöksiä kaikille kielille?
      // --> siispä joudutaan tyytymään yhteen. Jos nyt luontivaiheessa tai puuttuvan kohdalla vaihdetaan
      // localea, niin toimii väärin, mutta minkäs teet :/
      var oletusTavoiteOtsikko = {};
      oletusTavoiteOtsikko[Kieli.getSisaltokieli()] = $filter('kaanna')('kurssi-tavoitteet-header');
      return oletusTavoiteOtsikko;
    };
    return {
      openKoodisto: openKoodisto,
      getDefaultTavoitteet: getDefaultTavoitteet
    };
  })

  .controller('NaytaLukiokurssiController', function($scope, $state, LukioKurssiService,
                                                     $stateParams, YleinenData, LukiokoulutusService) {
    $scope.kurssi = LukioKurssiService.get($stateParams.kurssiId);
    $scope.kurssityypit = [];
    YleinenData.lukioKurssityypit().then(function(tyypit) {
      _.each(tyypit, function(t) {
        $scope.kurssityypit[t.tyyppi] = t.nimi;
      });
    });

    $scope.oppiaineMurupolkuItems = function(oppiaine) {
      var ls = [];
      while (oppiaine.vanhempi) {
        oppiaine = oppiaine.vanhempi;
        ls.push(oppiaine);
      }
      return _(ls).reverse().value();
    };

    $scope.goto = function(oppiaine) {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanId: oppiaine.oppiaineId,
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT,
        tabId: 0
      });
    };
    $scope.gotoKurssit = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT,
        tabId: 'kurssit'
      });
    };
    $scope.gotoMuokkaa = function() {
      LukioKurssiService.lukitse($stateParams.kurssiId).then(function() {
        $state.go('root.perusteprojekti.suoritustapa.muokkaakurssia', {
          kurssiId: $stateParams.kurssiId
        });
      });
    };
  })

  .controller('LisaaLukioKurssiController', function($scope, $state, $q, $stateParams,
                                                     LukiokoulutusService, LukioKurssiService,
                                                     YleinenData, LukiokurssiModifyHelpers,
                                                     Editointikontrollit, $filter, $rootScope) {
    Editointikontrollit.registerCallback({
      edit: function() {
      },
      save: function() {
        $rootScope.$broadcast('notifyCKEditor');
        LukioKurssiService.save($scope.kurssi).then(function() {
          $scope.back();
        });
      },
      cancel: function() {
        $scope.back();
      },
      validate: function() { return $filter('kaanna')($scope.kurssi.nimi) != ''; },
      notify: function () {
      }
    });
    Editointikontrollit.startEditing();

    $scope.kurssityypit = {};
    $scope.kurssi = {
      nimi: {},
      tyyppi: 'PAKOLLINEN',
      koodiUri: null,
      koodiArvo: null,
      tavoitteetOtsikko: LukiokurssiModifyHelpers.getDefaultTavoitteet()
    };
    YleinenData.lukioKurssityypit().then(function(tyypit) {
      $scope.kurssityypit = tyypit;
    });

    $scope.openKoodisto = LukiokurssiModifyHelpers.openKoodisto($scope.kurssi);

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT
      });
    };
  })
  .controller('MuokkaaLukiokurssiaController', function($scope, $state, LukioKurssiService, $stateParams,
              YleinenData, $log, $rootScope, LukiokurssiModifyHelpers, Editointikontrollit,
              Varmistusdialogi, $filter, Kaanna, LukiokoulutusService, Lukitus) {
    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.kurssi = LukioKurssiService.get($stateParams.kurssiId, function(kurssi) {
          if (!kurssi.tavoitteetOtsikko) {
            kurssi.tavoitteetOtsikko = LukiokurssiModifyHelpers.getDefaultTavoitteet();
          }
        });
      },
      save: function() {
        $rootScope.$broadcast('notifyCKEditor');
        LukioKurssiService.update($scope.kurssi).then(function() {
          $scope.back();
        });
      },
      cancel: function() {
        Lukitus.vapauta().then(function() {
          $scope.back();
        });
      },
      validate: function() { return $filter('kaanna')($scope.kurssi.nimi) != ''; },
      notify: function () {
      }
    });
    $scope.kurssi = null;
    Editointikontrollit.startEditing();

    $scope.kurssityypit = [];
    YleinenData.lukioKurssityypit().then(function(tyypit) {
      $scope.kurssityypit = tyypit;
    });

    $scope.openKoodisto = LukiokurssiModifyHelpers.openKoodisto($scope.kurssi);

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.kurssi', {
        kurssiId: $stateParams.kurssiId
      });
    };

    $scope.deleteKurssi = function() {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        teksti: Kaanna.kaanna('poistetaanko-kurssi'),
        primaryBtn: 'poista',
        successCb: function() {
          LukioKurssiService.deleteKurssi($stateParams.kurssiId).then(function() {
            Editointikontrollit.unregisterCallback();
            $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
              osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT
            });
          })
        }
      })();
    };

  });

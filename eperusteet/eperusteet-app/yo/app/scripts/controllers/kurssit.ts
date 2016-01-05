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
  .service('LukiokurssiModifyHelpers', function (Koodisto, MuokkausUtils, $translate, $filter,
                                                 Kieli, Kaanna, $log, $rootScope) {
    var openKoodisto = function (kurssi) {
      return Koodisto.modaali(function (koodisto) {
        MuokkausUtils.nestedSet(kurssi, 'koodiUri', ',', koodisto.koodiUri);
        MuokkausUtils.nestedSet(kurssi, 'koodiArvo', ',', koodisto.koodiArvo);
        MuokkausUtils.nestedSet(kurssi, 'nimi', ',', koodisto.nimi);
        kurssi.lokalisoituKoodi = {};
        kurssi.lokalisoituKoodi[Kieli.getSisaltokieli()] = koodisto.koodiArvo;
        $rootScope.$broadcast('notifyCKEditor');
      }, {
        tyyppi: function () {
          return 'lukionkurssit';
        },
        ylarelaatioTyyppi: function () {
          return '';
        },
        tarkista: _.constant(true)
      });
    };
    var getDefaultTavoitteet = function () {
      // TODO: ei tosiaan taida olla mitään tapaa saada $translatelta tietyn id:n käännöksiä kaikille kielille?
      // --> siispä joudutaan tyytymään yhteen. Jos nyt luontivaiheessa tai puuttuvan kohdalla vaihdetaan
      // localea, niin toimii väärin, mutta minkäs teet :/
      var oletusTavoiteOtsikko = {};
      oletusTavoiteOtsikko[Kieli.getSisaltokieli()] = $filter('kaanna')('kurssi-tavoitteet-header');
      return oletusTavoiteOtsikko;
    };

    function getOsa(kurssi, id) {
      return {
        id: id,
        obj: kurssi[id]
      };
    }

    function emptyI18n(defultValue) {
      var i18n = {};
      i18n[Kieli.getSisaltokieli()] = defultValue;
      return i18n;
    }

    function emptyOsa(id) {
      return {
        otsikko: emptyI18n(Kaanna.kaanna('kurssi-osa-' + id)),
        teksti: emptyI18n('')
      };
    }

    function isAddAvailable(osat) {
      var len = 0;
      _.each(osat, function (osa) {
        if (!osa.obj) {
          len++;
        }
      });
      return len > 0;
    }

    return {
      openKoodisto: openKoodisto,
      getDefaultTavoitteet: getDefaultTavoitteet,
      getOsa: getOsa,
      emptyOsa: emptyOsa,
      isAddAvailable: isAddAvailable
    };
  })

  .controller('NaytaLukiokurssiController', function ($scope, $state, LukioKurssiService,
                                                      $stateParams, YleinenData, LukiokoulutusService,
                                                      Kommentit, KommentitByPerusteenOsa, VersionHelper,
                                                      Notifikaatiot, Lukitus, $log) {
    Kommentit.haeKommentit(KommentitByPerusteenOsa,
      {id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.kurssiId});
    $scope.kurssi = LukioKurssiService.get($stateParams.kurssiId);
    $scope.kurssityypit = [];
    YleinenData.lukioKurssityypit().then(function (tyypit) {
      _.each(tyypit, function (t) {
        $scope.kurssityypit[t.tyyppi] = t.nimi;
      });
    });
    $scope.dontFixVersiot = true;
    $scope.versiot = {latest: true};

    $scope.haeVersiot = function (force, cb) {
      VersionHelper.getLukiokurssiVersions($scope.versiot, {id: $stateParams.kurssiId}, force, cb);
    };
    $scope.haeVersiot(true);

    $scope.vaihdaVersio = function (v) {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
      if ($scope.versiot.chosen) {
        $scope.kurssi = LukioKurssiService.getVersion($stateParams.kurssiId, $scope.versiot.chosen.numero);
      }
    };

    $scope.revertCb = function (response) {
      Lukitus.vapauta();
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      Notifikaatiot.onnistui('lukiokurssi-palautettu');
    };

    $scope.oppiaineMurupolkuItems = function (oppiaine) {
      var ls = [];
      while (oppiaine.vanhempi) {
        oppiaine = oppiaine.vanhempi;
        ls.push(oppiaine);
      }
      return _(ls).reverse().value();
    };

    $scope.goto = function (oppiaine) {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanId: oppiaine.oppiaineId,
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT,
        tabId: 0
      });
    };
    $scope.gotoKurssit = function () {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT,
        tabId: 'kurssit'
      });
    };
    $scope.gotoMuokkaa = function () {
      LukioKurssiService.lukitse($stateParams.kurssiId).then(function () {
        $state.go('root.perusteprojekti.suoritustapa.muokkaakurssia', {
          kurssiId: $stateParams.kurssiId
        });
      });
    };
  })

  .controller('LisaaLukioKurssiController', function ($scope, $state, $q, $stateParams,
                                                      LukiokoulutusService, LukioKurssiService,
                                                      YleinenData, LukiokurssiModifyHelpers,
                                                      Editointikontrollit, $filter, $rootScope) {
    Editointikontrollit.registerCallback({
      edit: function () {
      },
      save: function (kommentti) {
        $rootScope.$broadcast('notifyCKEditor');
        $scope.kurssi.kommentti = kommentti;
        LukioKurssiService.save($scope.kurssi).then(function () {
          $scope.back();
        });
      },
      cancel: function () {
        $scope.back();
      },
      validate: function () {
        return $filter('kaanna')($scope.kurssi.nimi) != '';
      },
      notify: function () {
      }
    });
    $scope.kurssityypit = {};
    $scope.kurssi = {
      nimi: {},
      tyyppi: 'PAKOLLINEN',
      koodiUri: null,
      koodiArvo: null
    };
    YleinenData.lukioKurssityypit().then(function (tyypit) {
      $scope.kurssityypit = tyypit;
    });
    $scope.osat = ['tavoitteet', 'keskeinenSisalto', 'tavoitteetJaKeskeinenSisalto'];
    $scope.muokattavatOsat = [];
    $scope.osatById = {};
    _.each($scope.osat, function (osaId) {
      var osa = LukiokurssiModifyHelpers.getOsa($scope.kurssi, osaId);
      $scope.osatById[osaId] = osa;
      $scope.muokattavatOsat.push(osa);
    });
    Editointikontrollit.startEditing();

    $scope.addOsa = function (id) {
      if (!$scope.kurssi[id]) {
        $scope.kurssi[id] = LukiokurssiModifyHelpers.emptyOsa(id);
        $scope.osatById[id].obj = $scope.kurssi[id];
      }
    };
    $scope.removeOsa = function (id) {
      $scope.kurssi[id] = null;
      $scope.osatById[id].obj = null;
    };
    $scope.isAddAvailable = function () {
      return LukiokurssiModifyHelpers.isAddAvailable($scope.muokattavatOsat);
    };

    $scope.openKoodisto = LukiokurssiModifyHelpers.openKoodisto($scope.kurssi);
    $scope.back = function () {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
        osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT
      });
    };
  })
  .controller('MuokkaaLukiokurssiaController', function ($scope, $state, LukioKurssiService, $stateParams, $timeout, $q,
                                       YleinenData, $log, $rootScope, LukiokurssiModifyHelpers, Editointikontrollit,
                                       Varmistusdialogi, $filter, Kaanna, LukiokoulutusService, Lukitus, Kieli) {
    Editointikontrollit.registerCallback({
      edit: function () {
        $log.info('edit called');
        $scope.kurssi = LukioKurssiService.get($stateParams.kurssiId, function (kurssi) {
          _.each($scope.osat, function (osaId) {
            var osa = LukiokurssiModifyHelpers.getOsa(kurssi, osaId);
            $scope.osatById[osaId] = osa;
            $scope.muokattavatOsat.push(osa);
          });
        });
      },
      save: function (kommentti) {
        $rootScope.$broadcast('notifyCKEditor', true);
        $scope.kurssi.kommentti = kommentti;
        LukioKurssiService.update(_.cloneDeep($scope.kurssi)).then(function () {
          $scope.back();
        });
      },
      cancel: function () {
        Lukitus.vapauta().then(function () {
          $scope.back();
        });
      },
      validate: function () {
        return $filter('kaanna')($scope.kurssi.nimi) != '';
      },
      notify: function () {
      }
    });
    $scope.kurssi = {kuvaus: {}};
    $scope.osat = ['tavoitteet', 'keskeinenSisalto', 'tavoitteetJaKeskeinenSisalto'];
    $scope.muokattavatOsat = [];
    $scope.osatById = {};
    Editointikontrollit.startEditing();

    $scope.kurssityypit = [];
    YleinenData.lukioKurssityypit().then(function (tyypit) {
      $scope.kurssityypit = tyypit;
    });

    $scope.addOsa = function (id) {
      if (!$scope.kurssi[id]) {
        $scope.kurssi[id] = LukiokurssiModifyHelpers.emptyOsa(id);
        $scope.osatById[id].obj = $scope.kurssi[id];
      }
      $rootScope.$broadcast('enableEditing');
    };
    $scope.removeOsa = function (id) {
      $scope.kurssi[id] = null;
      $scope.osatById[id].obj = null;
    };
    $scope.isAddAvailable = function () {
      return LukiokurssiModifyHelpers.isAddAvailable($scope.muokattavatOsat);
    };

    $scope.openKoodisto = LukiokurssiModifyHelpers.openKoodisto($scope.kurssi);
    $scope.back = function () {
      $state.go('root.perusteprojekti.suoritustapa.kurssi', {
        kurssiId: $stateParams.kurssiId
      });
    };

    $scope.deleteKurssi = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        teksti: Kaanna.kaanna('poistetaanko-kurssi'),
        primaryBtn: 'poista',
        successCb: function () {
          LukioKurssiService.deleteKurssi($stateParams.kurssiId).then(function () {
            Editointikontrollit.unregisterCallback();
            $state.go('root.perusteprojekti.suoritustapa.lukioosat', {
              osanTyyppi: LukiokoulutusService.OPPIAINEET_OPPIMAARAT
            });
          })
        }
      })();
    };
  });

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
  .service('TekstikappaleOperations', function (YleinenData, PerusteenOsaViitteet,
    Editointikontrollit, Notifikaatiot, $state, SuoritustapaSisalto, TutkinnonOsaEditMode,
    PerusopetusService, $stateParams) {
    var peruste = null;
    var deleteDone = false;

    this.setPeruste = function (value) {
      peruste = value;
    };

    function goToView(response, id) {
      var params = {
        perusteenOsaViiteId: id || response.id,
        versio: ''
      };
      $state.go('root.perusteprojekti.suoritustapa.tekstikappale', params);
    }

    this.add = function () {
      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.saveOsa({}, {osanTyyppi: 'tekstikappale'}, function (response) {
          TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
          goToView(response);
        });
      }
    };

    this.wasDeleted = function () {
      var ret = deleteDone;
      deleteDone = false;
      return ret;
    };

    this.delete = function (viiteId, isNew) {
      function commonCb(tyyppi) {
        deleteDone = true;
        if (isNew !== true) {
          Editointikontrollit.cancelEditing();
          Notifikaatiot.onnistui('poisto-onnistui');
        }
        $state.go('root.perusteprojekti.suoritustapa.' + tyyppi, {}, {reload: true});
      }

      var successCb = _.partial(commonCb, YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi].sisaltoTunniste);
      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.deleteOsa({$url: 'dummy', id: viiteId}, successCb, Notifikaatiot.serverCb);
      } else {
        PerusteenOsaViitteet.delete({viiteId: viiteId}, {}, successCb, Notifikaatiot.serverCb);
      }
    };

    this.addChild = function (viiteId, suoritustapa) {
        SuoritustapaSisalto.addChild({
          perusteId: peruste.id,
          suoritustapa: suoritustapa,
          perusteenosaViiteId: viiteId
        }, {}, function (response) {
          TutkinnonOsaEditMode.setMode(true);
          goToView(response);
        }, Notifikaatiot.varoitus);
    };

    this.clone = function (viiteId) {
      if (YleinenData.isPerusopetus(peruste)) {

      } else {
        PerusteenOsaViitteet.kloonaaTekstikappale({
          perusteId: peruste.id,
          suoritustapa: $stateParams.suoritustapa,
          viiteId: viiteId
        }, function (tk) {
          TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
          Notifikaatiot.onnistui('tekstikappale-kopioitu-onnistuneesti');
          goToView(tk, tk.id);
        });
      }
    };

    function mapSisalto(root) {
      return {
        id: root.id,
        perusteenOsa: null,
        lapset: _.map(root.lapset, mapSisalto)
      };
    }

    this.updateViitteet = function (sisalto, successCb) {
      var success = successCb || angular.noop;
      var mapped = mapSisalto(sisalto);

      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.updateSisaltoViitteet(sisalto, mapped, successCb);
      } else {
        PerusteenOsaViitteet.update({
          viiteId: sisalto.id
        }, mapped, success, Notifikaatiot.serverCb);
      }
    };
  })
  .controller('muokkausTekstikappaleCtrl', function ($scope, $q, Editointikontrollit, PerusteenOsat,
    Notifikaatiot, VersionHelper, Lukitus, TutkinnonOsaEditMode,
    Varmistusdialogi, Kaanna, PerusteprojektiTiedotService, $stateParams,
    Utils, PerusteProjektiSivunavi, YleinenData, $rootScope, Kommentit,
    KommentitByPerusteenOsa, PerusteenOsanTyoryhmat, Tyoryhmat, PerusteprojektiTyoryhmat,
    TEXT_HIERARCHY_MAX_DEPTH, TekstikappaleOperations, virheService, ProjektinMurupolkuService,
    $state) {

    $scope.tekstikappale = {};
    $scope.versiot = {};
    PerusteprojektiTiedotService.then(function(pts) { $scope.peruste = pts.getPeruste(); });

    var tekstikappaleDefer = $q.defer();
    $scope.tekstikappalePromise = tekstikappaleDefer.promise;

    $scope.valitseOsaamisala = function(oa) {
      $scope.editableTekstikappale.osaamisala = oa;
    };

    function successCb (re) {
      $scope.tekstikappale = re;
      setupTekstikappale($scope.tekstikappale);
      tekstikappaleDefer.resolve($scope.tekstikappale);
      if (TutkinnonOsaEditMode.getMode()) {
        $scope.isNew = true;
        $scope.muokkaa();
      }
    }

    function errorCb() {
      virheService.virhe('virhe-tekstikappaletta-ei-löytynyt');
    }

    var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
    if (versio) {
      VersionHelper.getPerusteenOsaVersionsByViite($scope.versiot, {id: $stateParams.perusteenOsaViiteId}, true, function () {
        var revNumber = VersionHelper.select($scope.versiot, versio);
        if (!revNumber) {
          errorCb();
        } else {
          PerusteenOsat.getVersioByViite({
            viiteId: $stateParams.perusteenOsaViiteId,
            versioId: revNumber
          }, successCb, errorCb);
        }
      });
    } else {
      PerusteenOsat.getByViite({viiteId: $stateParams.perusteenOsaViiteId}, successCb, errorCb);
    }



    TekstikappaleOperations.setPeruste($scope.$parent.peruste);
    $scope.kaikkiTyoryhmat = [];

    function paivitaRyhmat(uudet, cb) {
      PerusteenOsanTyoryhmat.save({
        projektiId: $stateParams.perusteProjektiId,
        osaId: $scope.tekstikappale.id
      }, uudet, cb, Notifikaatiot.serverCb);
    }

    $scope.poistaTyoryhma = function (tr) {
      Varmistusdialogi.dialogi({
        successCb: function () {
          var uusi = _.remove(_.clone($scope.tyoryhmat), function (vanha) {
            return vanha !== tr;
          });
          paivitaRyhmat(uusi, function () {
            $scope.tyoryhmat = uusi;
          });
        },
        otsikko: 'poista-tyoryhma-perusteenosasta',
        teksti: Kaanna.kaanna('poista-tyoryhma-teksti', {nimi: tr})
      })();
    };

    $scope.lisaaTyoryhma = function () {
      Tyoryhmat.valitse(_.clone($scope.kaikkiTyoryhmat), _.clone($scope.tyoryhmat), function (uudet) {
        var uusi = _.clone($scope.tyoryhmat).concat(uudet);
        paivitaRyhmat(uusi, function () {
          $scope.tyoryhmat = uusi;
        });
      });
    };

    Utils.scrollTo('#ylasivuankkuri');
    Kommentit.haeKommentit(KommentitByPerusteenOsa, {id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.perusteenOsaViiteId});

    $scope.sisalto = {};
    $scope.viitteet = {};
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

    function haeSisalto(cb) {
      if ($scope.tiedotService) {
        $scope.tiedotService.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function (res) {
          $scope.sisalto = res[0];
          $scope.setNavigation();
          (cb || angular.noop)();
        });
      }
    }

    if ($stateParams.suoritustapa || YleinenData.isPerusopetus($scope.$parent.peruste)) {
      PerusteprojektiTiedotService.then(function (instance) {
        $scope.tiedotService = instance;
        haeSisalto();
      });
    }

    $scope.setNavigation = function () {
      $scope.tree.init();
      ProjektinMurupolkuService.setCustom($scope.tree.get());
      VersionHelper.setUrl($scope.versiot);
    };

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, cb);
    }

    function fetch(cb) {
      PerusteenOsat.get({osanId: $scope.tekstikappale.id}, _.setWithCallback($scope, 'tekstikappale', cb));
    }

    function storeTree(sisalto, level) {
      level = level || 0;
      _.each(sisalto.lapset, function (lapsi) {
        if (lapsi.perusteenOsa) {
          if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
            $scope.viitteet[lapsi.perusteenOsa.id] = {};
          }
          $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
          $scope.viitteet[lapsi.perusteenOsa.id].level = level;
          $scope.viitteet[lapsi.perusteenOsa.id].nimi = lapsi.perusteenOsa.nimi;
          if (sisalto.perusteenOsa) {
            $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
          }
          storeTree(lapsi, level + 1);
        }
      });
    }

    function updateViitteet() {
      $scope.viitteet = {};
      storeTree($scope.sisalto);
    }

    $scope.tree = {
      init: function () {
        updateViitteet();
      },
      get: function () {
        var items = [];
        var id = $scope.tekstikappale.id;
        if ($scope.viitteet[id]) {
          do {
            items.push({
              label : $scope.viitteet[id].nimi,
              url: $scope.tekstikappale.id === id ? null : $state.href('root.perusteprojekti.suoritustapa.tekstikappale', {
                perusteenOsaViiteId: $scope.viitteet[id].viite,
                versio: ''
              })
            });
            id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
          } while (id);
        }
        items.reverse();
        return items.length > 1 ? items : [];
      }
    };

    $scope.viiteId = function () {
      return $scope.viitteet[$scope.tekstikappale.id] ? $scope.viitteet[$scope.tekstikappale.id].viite : null;
    };

    $scope.fields = [{
        path: 'nimi',
        hideHeader: false,
        localeKey: 'teksikappaleen-nimi',
        type: 'editor-header',
        localized: true,
        mandatory: true,
        mandatoryMessage: 'mandatory-otsikkoa-ei-asetettu',
        order: 1
      }, {
        path: 'teksti',
        hideHeader: false,
        localeKey: 'tekstikappaleen-teksti',
        type: 'editor-area',
        localized: true,
        mandatory: false,
        order: 2
      }];


    function refreshPromise() {
      $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
      tekstikappaleDefer = $q.defer();
      $scope.tekstikappalePromise = tekstikappaleDefer.promise;
      tekstikappaleDefer.resolve($scope.editableTekstikappale);
    }

    function saveCb(res, done) {
      // Päivitä versiot
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      PerusteProjektiSivunavi.refresh();
      Lukitus.vapautaPerusteenosa(res.id);
      Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
      haeSisalto(function() {
        $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
        $scope.isNew = false;
        done();
      });
    }

    function doDelete(isNew) {
      TekstikappaleOperations.delete($scope.viiteId(), isNew);
    }

    function setupTekstikappale(kappale) {

      $q.all([PerusteenOsanTyoryhmat.get({projektiId: $stateParams.perusteProjektiId, osaId: $scope.tekstikappale.id}).$promise,
        PerusteprojektiTyoryhmat.get({id: $stateParams.perusteProjektiId}).$promise]).then(function (data) {
        $scope.tyoryhmat = data[0];
        $scope.kaikkiTyoryhmat = _.unique(_.map(data[1], 'nimi'));
      }, Notifikaatiot.serverCb);

      $scope.editableTekstikappale = angular.copy(kappale);

      Editointikontrollit.registerCallback({
        edit: function(done) {
          fetch(function() {
            refreshPromise();
            done();
          });
        },
        asyncValidate: function (cb) {
          lukitse(cb);
        },
        save: function(kommentti, done) {
          $scope.editableTekstikappale.metadata = {kommentti: kommentti};
          PerusteenOsat.saveTekstikappale({
            osanId: $scope.editableTekstikappale.id
          }, $scope.editableTekstikappale, _.partial(saveCb, _, done), Notifikaatiot.serverCb);
        },
        cancel: function(done) {
          if (!TekstikappaleOperations.wasDeleted()) {
            Lukitus.vapautaPerusteenosa($scope.tekstikappale.id, function () {
              if ($scope.isNew) {
                doDelete(true);
              }
              else {
                fetch(function () {
                  refreshPromise();
                });
              }
              $scope.isNew = false;
              done();
            });
          }
          else {
            done();
          }
        },
        notify: function (mode) {
          $scope.editEnabled = mode;
        },
        validate: function (mandatoryValidator) {
          return mandatoryValidator($scope.fields, $scope.editableTekstikappale);
        }
      });

      $scope.haeVersiot();
      $scope.setNavigation();
      Lukitus.tarkista($scope.tekstikappale.id, $scope);
    }

    $scope.kopioiMuokattavaksi = function () {
      TekstikappaleOperations.clone($scope.viitteet[$scope.tekstikappale.id].viite);
    };

    $scope.muokkaa = function () {
      lukitse(function () {
        Editointikontrollit.startEditing();
      });
    };

    $scope.canAddLapsi = function () {
      return $scope.tekstikappale.id &&
        $scope.viitteet[$scope.tekstikappale.id] &&
        $scope.viitteet[$scope.tekstikappale.id].level < (TEXT_HIERARCHY_MAX_DEPTH - 1);
    };

    $scope.addLapsi = function () {
      TekstikappaleOperations.addChild($scope.viiteId(), $stateParams.suoritustapa);
    };

    $scope.$watch('editEnabled', function (editEnabled) {
      PerusteProjektiSivunavi.setVisible(!editEnabled);
    });

    $scope.haeVersiot = function (force) {
      VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
    };

    function responseFn(response) {
      $scope.tekstikappale = response;
      setupTekstikappale(response);
      tekstikappaleDefer = $q.defer();
      $scope.tekstikappalePromise = tekstikappaleDefer.promise;
      tekstikappaleDefer.resolve($scope.editableTekstikappale);
      VersionHelper.setUrl($scope.versiot);
    }

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
      //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
    };

    $scope.revertCb = function (response) {
      responseFn(response);
      saveCb(response);
    };

    $scope.poista = function () {
      var nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

      Varmistusdialogi.dialogi({
        successCb: doDelete,
        otsikko: 'poista-tekstikappale-otsikko',
        teksti: Kaanna.kaanna('poista-tekstikappale-teksti', {nimi: nimi})
      })();
    };


    // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
    var received = 0;
    $scope.$on('ckEditorInstanceReady', function () {
      if (++received === $scope.fields.length) {
        $rootScope.$broadcast('editointikontrollitRefresh');
      }
    });
  });

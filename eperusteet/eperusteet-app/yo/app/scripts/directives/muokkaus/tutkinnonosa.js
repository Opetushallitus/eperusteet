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
  .directive('muokkausTutkinnonosa', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tutkinnonosa.html',
      restrict: 'E',
      scope: {
        tutkinnonOsa: '=',
        versiot: '='
      },
      controller: 'muokkausTutkinnonosaController'
    };
  })

  .controller('muokkausTutkinnonosaController', function($scope, $state, $stateParams,
    $q, Editointikontrollit, PerusteenOsat, PerusteenRakenne,
    PerusteTutkinnonosa, TutkinnonOsaEditMode, $timeout, Varmistusdialogi,
    VersionHelper, Lukitus, MuokkausUtils, PerusteenOsaViitteet,
    Utils, ArviointiHelper, PerusteProjektiSivunavi, Notifikaatiot, Koodisto,
    Tutke2OsaData, Kommentit, KommentitByPerusteenOsa, FieldSplitter) {

    Utils.scrollTo('#ylasivuankkuri');

    Kommentit.haeKommentit(KommentitByPerusteenOsa, { id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.perusteenOsaId });

    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {};
    $scope.test = angular.noop;
    $scope.menuItems = [];
    $scope.editableTutkinnonOsa = {};
    $scope.editEnabled = false;
    $scope.editointikontrollit = Editointikontrollit;

    function getRakenne() {
      // FIXME: Vaihda käyttämään parempaa endpointtia
      PerusteenRakenne.haeByPerusteprojekti($stateParams.perusteProjektiId, $stateParams.suoritustapa, function(res) {
        $scope.rakenne = res;
        $scope.viiteosa = _.find($scope.rakenne.tutkinnonOsat, {'_tutkinnonOsa': $scope.editableTutkinnonOsa.id.toString()}) || {};
        $scope.viiteosa.laajuus = $scope.viiteosa.laajuus || 0;
        $scope.yksikko = _.zipObject(_.map(res.$peruste.suoritustavat, 'suoritustapakoodi'),
                                     _.map(res.$peruste.suoritustavat, 'laajuusYksikko'))[$scope.suoritustapa];
        if (TutkinnonOsaEditMode.getMode()) {
          $scope.isNew = true;
          $timeout(function () {
            $scope.muokkaa();
          }, 50);
        }
      });
    }
    getRakenne();

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosa($scope.tutkinnonOsa.id, cb);
    }

    function fetch(cb) {
      cb = cb || angular.noop;
      PerusteenOsat.get({ osanId: $stateParams.perusteenOsaId }, function(res) {
        $scope.tutkinnonOsa = res;
        cb(res);
      });
    }

    $scope.fields =
      new Array({
        path: 'tavoitteet',
        localeKey: 'tutkinnon-osan-tavoitteet',
        type: 'editor-area',
        localized: true,
        collapsible: true,
        order: 3
      },{
        path: 'ammattitaitovaatimukset',
        localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
        type: 'editor-area',
        localized: true,
        collapsible: true,
        order: 4
      },{
        path: 'ammattitaidonOsoittamistavat',
        localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
        type: 'editor-area',
        localized: true,
        collapsible: true,
        order: 7
      },{
        path: 'arviointi.lisatiedot',
        localeKey: 'tutkinnon-osan-arviointi-teksti',
        type: 'editor-text',
        localized: true,
        collapsible: true,
        order: 5
      },{
        path: 'arviointi.arvioinninKohdealueet',
        localeKey: 'tutkinnon-osan-arviointi-taulukko',
        type: 'arviointi',
        collapsible: true,
        order: 6
      });

    $scope.koodistoClick = Koodisto.modaali(function(koodisto) {
      MuokkausUtils.nestedSet($scope.editableTutkinnonOsa, 'koodiUri', ',', koodisto.koodiUri);
      MuokkausUtils.nestedSet($scope.editableTutkinnonOsa, 'koodiArvo', ',', koodisto.koodiArvo);
    }, {
      tyyppi: function() { return 'tutkinnonosat'; },
      ylarelaatioTyyppi: function() { return ''; }
    });

    $scope.kopioiMuokattavaksi = function() {
      PerusteenOsaViitteet.kloonaaTutkinnonOsa({
        viiteId: $scope.viiteosa.id
      }, function(tk) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        Notifikaatiot.onnistui('tutkinnonosa-kopioitu-onnistuneesti');
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tutkinnonosa',
          perusteenOsaId: tk._tutkinnonOsa,
          versio: ''
        });
      });
    };

    function refreshPromise() {
      $scope.tutkinnonOsa.kuvaus = $scope.tutkinnonOsa.kuvaus || {};
      $scope.editableTutkinnonOsa = angular.copy($scope.tutkinnonOsa);
      $scope.editableViiteosa = angular.copy($scope.viiteosa);
      var tutkinnonOsaDefer = $q.defer();
      $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
      tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
    }

    function saveCb(res) {
      Lukitus.vapautaPerusteenosa(res.id);
      Notifikaatiot.onnistui('muokkaus-tutkinnon-osa-tallennettu');
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      tutke2.fetch();
    }

    function doDelete(osaId) {
      PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.rakenne.$peruste.id, $stateParams.suoritustapa, function() {
        Notifikaatiot.onnistui('tutkinnon-osa-rakenteesta-poistettu');
        $state.go('root.perusteprojekti.suoritustapa.tutkinnonosat');
      });
    }

    var tutke2 = {
      fetch: function () {
        if ($scope.editableTutkinnonOsa.tyyppi === 'tutke2') {
          Tutke2OsaData.get().fetch();
        }
      },
      mergeOsaAlueet: function (tutkinnonOsa) {
        if ($scope.editableTutkinnonOsa.tyyppi === 'tutke2') {
          tutkinnonOsa.osaAlueet = _.map(Tutke2OsaData.get().$editing, function (osaAlue) {
            var item = {nimi: osaAlue.nimi};
            if (osaAlue.id) {
              item.id = osaAlue.id;
            }
            return item;
          });
        }
      },
      validate: function() {
        if ($scope.editableTutkinnonOsa.tyyppi === 'tutke2') {
          return _.all(_.map(Tutke2OsaData.get().$editing, function (item) {
            return Utils.hasLocalizedText(item.nimi);
          }));
        } else {
          return true;
        }
      }
    };

    var normalCallbacks = {
      edit: function() {
        tutke2.fetch();
      },
      asyncValidate: function(cb) {
        lukitse(function() { cb(); });
      },
      save: function(kommentti) {
        tutke2.mergeOsaAlueet($scope.editableTutkinnonOsa);
        $scope.editableTutkinnonOsa.metadata = { kommentti: kommentti };
        if ($scope.editableTutkinnonOsa.id) {
          $scope.editableTutkinnonOsa.$saveTutkinnonOsa(function(response) {
            $scope.editableTutkinnonOsa = angular.copy(response);
            $scope.tutkinnonOsa = angular.copy(response);
            Editointikontrollit.lastModified = response;
            saveCb(response);
            getRakenne();

            var tutkinnonOsaDefer = $q.defer();
            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsa);
          }, Notifikaatiot.serverCb);

          // Viiteosa (laajuus) tallennetaan erikseen
          PerusteTutkinnonosa.save({
            perusteId: $scope.rakenne.$peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            osanId: $scope.editableViiteosa.id
          }, $scope.editableViiteosa, angular.noop, Notifikaatiot.serverCb);
        }
        else {
          PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsa, function(response) {
            Editointikontrollit.lastModified = response;
            saveCb(response);
            getRakenne();
          }, Notifikaatiot.serverCb);
        }
        $scope.isNew = false;
      },
      cancel: function() {
        if ($scope.isNew) {
          doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsa.id].id);
          $scope.isNew = false;
        }
        else {
          tutke2.fetch();
          fetch(function() {
            refreshPromise();
            Lukitus.vapautaPerusteenosa($scope.tutkinnonOsa.id);
          });
        }
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      },
      validate: function () {
        if (!Utils.hasLocalizedText($scope.editableTutkinnonOsa.nimi)) {
          $scope.nimiValidationError = true;
        }
        return $scope.tutkinnonOsaHeaderForm.$valid && tutke2.validate();
      }
    };

    function setupTutkinnonOsa(osa) {
      $scope.editableTutkinnonOsa = angular.copy(osa);
      $scope.isNew = !$scope.editableTutkinnonOsa.id;

      Editointikontrollit.registerCallback(normalCallbacks);
      $scope.haeVersiot();
      Lukitus.tarkista($scope.tutkinnonOsa.id, $scope);
    }

    if($scope.tutkinnonOsa) {
      $scope.tutkinnonOsaPromise = $scope.tutkinnonOsa.$promise.then(function(response) {
        setupTutkinnonOsa(response);
        return $scope.editableTutkinnonOsa;
      });
    } else {
      var objectReadyDefer = $q.defer();
      $scope.tutkinnonOsaPromise = objectReadyDefer.promise;
      $scope.tutkinnonOsa = {};
      setupTutkinnonOsa($scope.tutkinnonOsa);
      objectReadyDefer.resolve($scope.editableTutkinnonOsa);
      VersionHelper.setUrl($scope.versiot);
    }

    $scope.poistaTutkinnonOsa = function(osaId) {
      var onRakenteessa = PerusteenRakenne.validoiRakennetta($scope.rakenne.rakenne, function(osa) {
        return osa._tutkinnonOsaViite && $scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].id === osaId;
      });
      if (onRakenteessa) {
        Notifikaatiot.varoitus('tutkinnon-osa-rakenteessa-ei-voi-poistaa');
      }
      else {
        Varmistusdialogi.dialogi({
          otsikko: 'poistetaanko-tutkinnonosa',
          primaryBtn: 'poista',
          successCb: function () {
            $scope.isNew = false;
            Editointikontrollit.cancelEditing();
            doDelete(osaId);
          }
        })();
      }
    };

    $scope.muokkaa = function () {
      Editointikontrollit.registerCallback(normalCallbacks);
      lukitse(function() {
        fetch(function() {
          Editointikontrollit.startEditing();
          refreshPromise();
        });
      });
    };
    $scope.$watch('editEnabled', function (editEnabled) {
      PerusteProjektiSivunavi.setVisible(!editEnabled);
    });

    $scope.haeVersiot = function (force, cb) {
      VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tutkinnonOsa.id}, force, cb);
    };

    function responseFn(response) {
      $scope.tutkinnonOsa = response;
      setupTutkinnonOsa(response);
      var objDefer = $q.defer();
      $scope.tutkinnonOsaPromise = objDefer.promise;
      objDefer.resolve($scope.editableTutkinnonOsa);
      VersionHelper.setUrl($scope.versiot);
    }

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
      //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tutkinnonOsa.id}, responseFn);
    };

    $scope.revertCb = function (response) {
      responseFn(response);
      saveCb(response);
    };

    $scope.addFieldToVisible = function(field) {
      field.visible = true;
      // Varmista että menu sulkeutuu klikin jälkeen
      $timeout(function () {
        angular.element('h1').click();
        // TODO ei toimi koska localeKey voi olla muu kuin string,
        //     joku muu tapa yksilöidä/löytää juuri lisätty kenttä?
        Utils.scrollTo('li.' + FieldSplitter.getClass(field));
      });
    };

    /**
     * Palauttaa true jos kaikki mahdolliset osiot on jo lisätty
     */
    $scope.allVisible = function() {
      var lisatty = _.all($scope.fields, function (field) {
        return (_.contains(field.path, 'arviointi.') ||
                !field.inMenu ||
                (field.inMenu && field.visible));
      });
      return lisatty && $scope.arviointiHelper.exists();
    };

    $scope.updateMenu = function () {
      if (!$scope.arviointiHelper) {
        $scope.arviointiHelper = ArviointiHelper.create();
      }
      $scope.arviointiFields = $scope.arviointiHelper.initFromFields($scope.fields);
      $scope.menuItems = _.reject($scope.fields, 'mandatory');
      if ($scope.arviointiHelper) {
        $scope.arviointiHelper.setMenu($scope.menuItems);
      }
    };

    $scope.$watch('arviointiFields.teksti.visible', $scope.updateMenu);
    $scope.$watch('arviointiFields.taulukko.visible', $scope.updateMenu);
  });

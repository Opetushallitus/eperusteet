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
        tutkinnonOsaViite: '=',
        versiot: '=',
        peruste: '='
      },
      controller: 'muokkausTutkinnonosaController'
    };
  })
  .service('TutkinnonosanTiedotService', function(PerusteenOsat, $q, TutkinnonOsanOsaAlue, Osaamistavoite) {
    var tutkinnonOsa;

    function noudaTutkinnonOsa(stateParams) {

      var deferred = $q.defer();

      PerusteenOsat.get({osanId: stateParams.perusteenOsaId}, function (vastaus) {
        tutkinnonOsa = vastaus;
        if (vastaus.tyyppi === 'tutke2') {
          TutkinnonOsanOsaAlue.list({osanId: stateParams.perusteenOsaId}, function (osaAlueet) {
            tutkinnonOsa.osaAlueet = osaAlueet;

            if (osaAlueet && osaAlueet.length > 0) {
              var promisesList = [];
              _.each(osaAlueet, function (osaAlue) {
                var valmis = Osaamistavoite.list({osanId: stateParams.perusteenOsaId, osaalueenId: osaAlue.id}, function (osaamistavoitteet) {
                  osaAlue.osaamistavoitteet = osaamistavoitteet;
                });
                promisesList.push(valmis.promise);
              });
              $q.all(promisesList).then( function() {
                deferred.resolve();
              }, function () {
                deferred.reject();
              });

            } else {
              deferred.resolve();
            }
          });
        } else {
          deferred.resolve();
        }

      });

      return deferred.promise;
    }

    function getTutkinnonOsa() {
      return _.clone(tutkinnonOsa);
    }

    return {
      noudaTutkinnonOsa: noudaTutkinnonOsa,
      getTutkinnonOsa: getTutkinnonOsa
    };

  })
  .controller('muokkausTutkinnonosaController', function($scope, $state, $stateParams, $rootScope,
    $q, Editointikontrollit, PerusteenOsat, PerusteenRakenne, PerusteTutkinnonosa,
    TutkinnonOsaEditMode, $timeout, Varmistusdialogi, VersionHelper, Lukitus,
    MuokkausUtils, PerusteenOsaViitteet, Utils, ArviointiHelper, PerusteProjektiSivunavi,
    Notifikaatiot, Koodisto, Tutke2OsaData, Kommentit, KommentitByPerusteenOsa, FieldSplitter) {

    Utils.scrollTo('#ylasivuankkuri');

    Kommentit.haeKommentit(KommentitByPerusteenOsa, { id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.perusteenOsaViiteId });

    $scope.osaAlueAlitila = $state.current.name === 'root.perusteprojekti.suoritustapa.perusteenosa.osaalue' ? true : false;
    $rootScope.$on('$stateChangeStart', function(event, toState){
      if (toState.name === 'root.perusteprojekti.suoritustapa.perusteenosa.osaalue') {
        $scope.osaAlueAlitila = true;
      } else {
        $scope.osaAlueAlitila = false;
      }
    });

    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {};
    $scope.test = angular.noop;
    $scope.menuItems = [];
    $scope.editableTutkinnonOsaViite = {};
    $scope.editEnabled = false;
    $scope.editointikontrollit = Editointikontrollit;
    $scope.nimiValidationError = false;

    $scope.$watch('editableTutkinnonOsa.nimi', function () {
      $scope.nimiValidationError = false;
    }, true);

    // FIXME
    $scope.yksikko = 'osp';


    function getRakenne() {
      // FIXME: Vaihda käyttämään parempaa endpointtia
      PerusteenRakenne.haeByPerusteprojekti($stateParams.perusteProjektiId, $stateParams.suoritustapa, function(res) {
        $scope.rakenne = res;
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
      Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, cb);
    }

    function fetch(cb) {
      cb = cb || angular.noop;
      PerusteenOsat.get({ osanId: $scope.tutkinnonOsaViite.tutkinnonOsa.id }, function(res) {
        $scope.tutkinnonOsaViite.tutkinnonOsa = res;
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
      MuokkausUtils.nestedSet($scope.editableTutkinnonOsaViite.tutkinnonOsa, 'koodiUri', ',', koodisto.koodiUri);
      MuokkausUtils.nestedSet($scope.editableTutkinnonOsaViite.tutkinnonOsa, 'koodiArvo', ',', koodisto.koodiArvo);
    }, {
      tyyppi: function() { return 'tutkinnonosat'; },
      ylarelaatioTyyppi: function() { return ''; }
    });


    $scope.kopioiMuokattavaksi = function() {
      PerusteenOsaViitteet.kloonaaTutkinnonOsa({
        viiteId: $scope.tutkinnonOsaViite.id
      }, function(tk) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        Notifikaatiot.onnistui('tutkinnonosa-kopioitu-onnistuneesti');
        $state.go('root.perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tutkinnonosa',
          perusteenOsaViiteId: tk.id,
          versio: ''
        },{reload: true});
      });
    };

    function refreshPromise() {
      $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus = $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus || {};
      $scope.editableTutkinnonOsaViite = angular.copy($scope.tutkinnonOsaViite);
      var tutkinnonOsaDefer = $q.defer();
      $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
      tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
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
      PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.peruste.id, $stateParams.suoritustapa, function() {
        Notifikaatiot.onnistui('tutkinnon-osa-rakenteesta-poistettu');
        $state.go('root.perusteprojekti.suoritustapa.tutkinnonosat');
      });
    }

    var tutke2 = {
      fetch: function () {
        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === 'tutke2') {
          if (Tutke2OsaData.get()) {
            Tutke2OsaData.get().fetch();
        }
        }
      },
      mergeOsaAlueet: function (tutkinnonOsa) {
        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === 'tutke2') {
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
        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === 'tutke2') {
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
        tutke2.mergeOsaAlueet($scope.editableTutkinnonOsaViite.tutkinnonOsa);
        $scope.editableTutkinnonOsaViite.metadata = { kommentti: kommentti };
        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.id) {
          PerusteTutkinnonosa.save({perusteId: $scope.peruste.id, suoritustapa: $stateParams.suoritustapa, osanId: $scope.editableTutkinnonOsaViite.tutkinnonOsa.id},
          $scope.editableTutkinnonOsaViite, function(response){
            $scope.editableTutkinnonOsaViite = angular.copy(response);
            $scope.tutkinnonOsaViite = angular.copy(response);
            Editointikontrollit.lastModified = response;
            saveCb(response.tutkinnonOsa);
            getRakenne();

            var tutkinnonOsaDefer = $q.defer();
            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
          }, Notifikaatiot.serverCb);
        }
        else {
          PerusteenOsat.saveTutkinnonOsa($scope.editableTutkinnonOsaViite.tutkinnonOsa, function(response) {
            Editointikontrollit.lastModified = response;
            saveCb(response);
            getRakenne();
          }, Notifikaatiot.serverCb);
        }
        $scope.isNew = false;
      },
      cancel: function() {
        if ($scope.isNew) {
          doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id].id);
          $scope.isNew = false;
        }
        else {
          tutke2.fetch();
          fetch(function() {
            refreshPromise();
            Lukitus.vapautaPerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id);
          });
        }
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      },
      validate: function () {
        if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
          $scope.nimiValidationError = true;
        }
        return $scope.tutkinnonOsaHeaderForm.$valid && tutke2.validate();
      }
    };

    function setupTutkinnonOsa(osa) {
      $scope.tutkinnonOsaViite.tutkinnonOsa = osa;
      $scope.editableTutkinnonOsaViite.tutkinnonOsa = angular.copy(osa);
      $scope.isNew = !$scope.editableTutkinnonOsaViite.tutkinnonOsa.id;
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.perusteenosa') {
        Editointikontrollit.registerCallback(normalCallbacks);
      }
      $scope.haeVersiot();
      Lukitus.tarkista($scope.tutkinnonOsaViite.tutkinnonOsa.id, $scope);
    }

    if($scope.tutkinnonOsaViite) {
      $scope.tutkinnonOsaPromise = $scope.tutkinnonOsaViite.$promise.then(function(response) {
        setupTutkinnonOsa(response.tutkinnonOsa);
        return $scope.editableTutkinnonOsaViite;
      });
    } else {
      var objectReadyDefer = $q.defer();
      $scope.tutkinnonOsaPromise = objectReadyDefer.promise;
      $scope.tutkinnonOsaViite.tutkinnonOsa = {};
      setupTutkinnonOsa($scope.tutkinnonOsaViite.tutkinnonOsa);
      objectReadyDefer.resolve($scope.editableTutkinnonOsaViite);
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
      VersionHelper.getTutkinnonOsaViiteVersions($scope.versiot, {id: $scope.tutkinnonOsaViite.id}, force, cb);
    };

    function responseFn(response) {
      $scope.tutkinnonOsaViite.tutkinnonOsa = response;
      setupTutkinnonOsa(response);
      var objDefer = $q.defer();
      $scope.tutkinnonOsaPromise = objDefer.promise;
      objDefer.resolve($scope.editableTutkinnonOsa);
      VersionHelper.setUrl($scope.versiot);
    }

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
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

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
  .factory('TutkinnonOsanKoodiUniqueResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/tutkinnonosat/koodi/uniikki/:tutkinnonosakoodi');
  })
  .service('TutkinnonosanTiedotService', function(PerusteenOsat, $q, TutkinnonOsanOsaAlue, Osaamistavoite) {
    var FIELD_ORDER = {
      tavoitteet: 3,
      ammattitaitovaatimukset: 4,
      ammattitaidonOsoittamistavat: 7,
      arviointi: 5,
      lisatiedot: 5,
      arvioinninKohdealueet: 6
    };

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
      getTutkinnonOsa: getTutkinnonOsa,
      order: function (key) {
        return FIELD_ORDER[key] || -1;
      },
      keys: function () {
        return _.keys(FIELD_ORDER);
      }
    };
  })
  .service('muokkausCtrlCommons', function($rootScope, $state, ProjektinMurupolkuService, Editointikontrollit, Lukitus,
        PerusteProjektiSivunavi, PerusteenOsat, MuokkausUtils, Koodisto, PerusteenRakenne, Notifikaatiot, Varmistusdialogi,
        Utils, $timeout, FieldSplitter, ArviointiHelper, VersionHelper, $q, Algoritmit) {
    var cOsaaluePattern = new RegExp('root.perusteprojekti.suoritustapa.\\w+.osaalue');
    var currentScope = null;

    // Quick fix that creates only one state change listener. Ugly, should refactor.
    $rootScope.$on('$stateChangeStart', function(event, toState) {
      if (currentScope) {
        currentScope.osaAlueAlitila = cOsaaluePattern.test(toState.name);
      }
    });

    function setupScope(scope, doDelete, saveCb) {
      scope.addFieldToVisible = function(field) {
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
      scope.allVisible = function() {
        var lisatty = _.all(scope.fields, function (field) {
          return (_.contains(field.path, 'arviointi.') ||
                  !field.inMenu ||
                  (field.inMenu && field.visible));
        });
        return lisatty && scope.arviointiHelper.exists();
      };

      scope.updateMenu = function() {
        if (!scope.arviointiHelper) {
          scope.arviointiHelper = ArviointiHelper.create();
        }
        scope.arviointiFields = scope.arviointiHelper.initFromFields(scope.fields);
        scope.menuItems = _.reject(scope.fields, 'mandatory');
        if (scope.arviointiHelper) {
          scope.arviointiHelper.setMenu(scope.menuItems);
        }
      };

      scope.sortableOptions = {
        handle: '.handle',
        connectWith: '.container-items',
        cursor: 'move',
        cursorAt: {top : 2, left: 2},
        tolerance: 'pointer',
      };

      scope.koodistoClick = Koodisto.modaali(function(koodisto) {
        MuokkausUtils.nestedSet(scope.editableTutkinnonOsaViite.tutkinnonOsa, 'koodiUri', ',', koodisto.koodiUri);
        MuokkausUtils.nestedSet(scope.editableTutkinnonOsaViite.tutkinnonOsa, 'koodiArvo', ',', koodisto.koodiArvo);
      }, {
        tyyppi: function() { return 'tutkinnonosat'; },
        ylarelaatioTyyppi: function() { return ''; },
        tarkista: _.constant(true)
      });

      scope.poistaTutkinnonOsa = function(osaId) {
        var onRakenteessa = PerusteenRakenne.validoiRakennetta(scope.rakenne.rakenne, function(osa) {
          return osa._tutkinnonOsaViite && scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].id === osaId;
        });
        if (onRakenteessa) {
          Notifikaatiot.varoitus('tutkinnon-osa-rakenteessa-ei-voi-poistaa');
        }
        else {
          Varmistusdialogi.dialogi({
            otsikko: 'poistetaanko-tutkinnonosa',
            primaryBtn: 'poista',
            successCb: function() {
              scope.isNew = false;
              Editointikontrollit.cancelEditing();
              doDelete(osaId);
            }
          })();
        }

        scope.osaAlueAlitila = $state.is('root.perusteprojekti.suoritustapa.*.osaalue');
        scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko(scope.peruste, scope.suoritustapa);

      };

      scope.addVapaaTeksti = function(vapaatTekstit) {
        vapaatTekstit.push({
          nimi: {},
          teksti: {}
        });
        Utils.scrollTo('#vapaatTekstitAnchor');
      };

      scope.haeVersiot = function(force, cb) {
        VersionHelper.getTutkinnonOsaViiteVersions(scope.versiot, {
          id: scope.tutkinnonOsaViite.id
        }, force, cb);
      };

      scope.vaihdaVersio = function () {
        scope.versiot.hasChanged = true;
        VersionHelper.setUrl(scope.versiot);
      };

      function responseFn(response) {
        scope.tutkinnonOsaViite.tutkinnonOsa = response;
        setupTutkinnonOsaViite(response);
        var objDefer = $q.defer();
        scope.tutkinnonOsaPromise = objDefer.promise;
        objDefer.resolve(scope.editableTutkinnonOsaViite);
        VersionHelper.setUrl(scope.versiot);
      }

      scope.revertCb = function(response) {
        responseFn(response);
        saveCb(response);
      };

    }

    function setupTutkinnonOsaViite(scope, editointiCallbacks) {
      currentScope = scope;
      scope.$watch('arviointiFields.teksti.visible', scope.updateMenu);
      scope.$watch('arviointiFields.taulukko.visible', scope.updateMenu);
      scope.$watch('editEnabled', function(editEnabled) {
        PerusteProjektiSivunavi.setVisible(!editEnabled);
      });
      scope.$watch('editableTutkinnonOsa.nimi', function() {
        scope.nimiValidationError = false;
      }, true);

      return function(viite) {
        scope.tutkinnonOsaViite = viite;
        ProjektinMurupolkuService.set('tutkinnonOsaViiteId', scope.tutkinnonOsaViite.id, scope.tutkinnonOsaViite.tutkinnonOsa.nimi);
        scope.editableTutkinnonOsaViite = angular.copy(viite);
        scope.isNew = !scope.editableTutkinnonOsaViite.tutkinnonOsa.id;

        // FIXME: Onko tarpeellista?
        if ($state.current.name === 'root.perusteprojekti.suoritustapa.koulutuksenosa' ||
            $state.current.name === 'root.perusteprojekti.suoritustapa.tutkinnonosa') {
          Editointikontrollit.registerCallback(editointiCallbacks);
        }

        scope.haeVersiot();
        Lukitus.tarkista(scope.tutkinnonOsaViite.tutkinnonOsa.id, scope);
      };
    }

    return {
      setupScope: setupScope,
      setupTutkinnonOsaViite: setupTutkinnonOsaViite,
      scoped: function(scope) {
        return {
          fetch: function(cb) {
            cb = cb || angular.noop;
            PerusteenOsat.get({
              osanId: scope.tutkinnonOsaViite.tutkinnonOsa.id
            }, function(res) {
              scope.tutkinnonOsaViite.tutkinnonOsa = res;
              cb(res);
            });
          }
        };
      }
    };
  })
  .controller('muokkausTutkinnonosaCtrl', function($scope, $state, $stateParams, $rootScope,
    $q, Editointikontrollit, PerusteenOsat, PerusteenRakenne, PerusteTutkinnonosa,
    TutkinnonOsaEditMode, $timeout, Varmistusdialogi, VersionHelper, Lukitus,
    MuokkausUtils, PerusteenOsaViitteet, Utils, ArviointiHelper, PerusteProjektiSivunavi,
    Notifikaatiot, Koodisto, Tutke2OsaData, Kommentit, KommentitByPerusteenOsa, FieldSplitter,
    Algoritmit, TutkinnonosanTiedotService, TutkinnonOsaViitteet, PerusteenOsaViite, virheService,
    ProjektinMurupolkuService, muokkausCtrlCommons, Tutke2Service) {

    function doDelete(osaId) {
      PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.peruste.id, $stateParams.suoritustapa, function() {
        Notifikaatiot.onnistui('tutkinnon-osa-rakenteesta-poistettu');
        $state.go('root.perusteprojekti.suoritustapa.tutkinnonosat');
      });
    }

    function successCb(re) {
      setupTutkinnonOsaViite(re);
      tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
      $scope.$laajuusRangena = $scope.editableTutkinnonOsaViite.laajuusMaksimi > 0;
    }

    function errorCb() {
      virheService.virhe('virhe-tutkinnonosaa-ei-löytynyt');
    }

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, cb);
    }

    function saveCb(res) {
      Lukitus.vapautaPerusteenosa(res.id);
      ProjektinMurupolkuService.set('tutkinnonOsaViiteId', $scope.tutkinnonOsaViite.id, $scope.tutkinnonOsaViite.tutkinnonOsa.nimi);
      Notifikaatiot.onnistui('muokkaus-tutkinnon-osa-tallennettu');
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
    }

    Utils.scrollTo('#ylasivuankkuri');

    Kommentit.haeKommentit(KommentitByPerusteenOsa, { id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.tutkinnonOsaViiteId });

    $scope.$laajuusRangena = false;
    $scope.toggleLaajuusRange = function() {
      $scope.$laajuusRangena = !$scope.$laajuusRangena;
    };

    $scope.tutkinnonOsaViite = {};
    $scope.versiot = {};

    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {};
    $scope.test = angular.noop;
    $scope.menuItems = [];
    $scope.editableTutkinnonOsaViite = {};
    $scope.editEnabled = false;
    $scope.editointikontrollit = Editointikontrollit;
    $scope.nimiValidationError = false;
    muokkausCtrlCommons.setupScope($scope, doDelete, saveCb);

    $scope.getTosa = function() {
      return !$scope.rakenne.tutkinnonOsat ? undefined : $scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id];
    };

    var fetch = muokkausCtrlCommons.scoped($scope).fetch;

    var tutkinnonOsaDefer = $q.defer();
    $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

    var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
    if (versio) {
      VersionHelper.getTutkinnonOsaViiteVersions($scope.versiot, {id: $stateParams.tutkinnonOsaViiteId}, true, function () {
        var revNumber = VersionHelper.select($scope.versiot, versio);
        if (!revNumber) {
          errorCb();
        } else {
          TutkinnonOsaViitteet.getVersio({
            viiteId: $stateParams.tutkinnonOsaViiteId,
            versioId: revNumber
          }, successCb, errorCb);
        }
      });
    }
    else {
      PerusteenOsaViite.get({
        perusteId: $scope.peruste.id,
        suoritustapa: $stateParams.suoritustapa,
        viiteId: $stateParams.tutkinnonOsaViiteId
      }, successCb, errorCb);
    }

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

    $scope.removeVapaaTeksti = function(vapaatTekstit, sisalto) {
      _.remove(vapaatTekstit, sisalto);
    };

    $scope.fields = [{
      path: 'tutkinnonOsa.tavoitteet',
      localeKey: 'tutkinnon-osan-tavoitteet',
      type: 'editor-area',
      localized: true,
      collapsible: true
    }, {
      path: 'tutkinnonOsa.ammattitaitovaatimukset',
      localeKey: 'tutkinnon-osan-ammattitaitovaatimukset',
      type: 'editor-area',
      localized: true,
      collapsible: true
    }, {
      path: 'tutkinnonOsa.ammattitaidonOsoittamistavat',
      localeKey: 'tutkinnon-osan-ammattitaidon-osoittamistavat',
      type: 'editor-area',
      localized: true,
      collapsible: true
    }, {
      path: 'tutkinnonOsa.arviointi.lisatiedot',
      localeKey: 'tutkinnon-osan-arviointi-teksti',
      type: 'editor-area',
      localized: true,
      collapsible: true
    }, {
      path: 'tutkinnonOsa.arviointi.arvioinninKohdealueet',
      localeKey: 'tutkinnon-osan-arviointi-taulukko',
      type: 'arviointi',
      collapsible: true
    }];

    _.each($scope.fields, function (field) {
      field.order = TutkinnonosanTiedotService.order(_.last(field.path.split('.')));
    });

    $scope.kopioiMuokattavaksi = function() {
      PerusteenOsaViitteet.kloonaaTutkinnonOsa({
        viiteId: $scope.tutkinnonOsaViite.id
      }, function(tk) {
        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
        Notifikaatiot.onnistui('tutkinnonosa-kopioitu-onnistuneesti');
        $state.go('root.perusteprojekti.suoritustapa.tutkinnonosa', {
          perusteenOsaViiteId: tk.id,
          versio: ''
        },{reload: true});
      });
    };

    function refreshPromise() {
      $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus = $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus || {};
      $scope.editableTutkinnonOsaViite = angular.copy($scope.tutkinnonOsaViite);
      tutkinnonOsaDefer = $q.defer();
      $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
      tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
    }

    var normalCallbacks = {
      edit: function() {
        Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
      },
      asyncValidate: function(done) {
        done();
      },
      save: function(kommentti) {
        if (!$scope.$laajuusRangena || !$scope.editableTutkinnonOsaViite.laajuusMaksimi) {
          $scope.editableTutkinnonOsaViite.laajuusMaksimi = undefined;
          $scope.$laajuusRangena = false;
        }

        Tutke2Service.mergeOsaAlueet($scope.editableTutkinnonOsaViite.tutkinnonOsa);
        $scope.editableTutkinnonOsaViite.metadata = { kommentti: kommentti };

        if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.id) {
          PerusteTutkinnonosa.save({
            perusteId: $scope.peruste.id,
            suoritustapa: $stateParams.suoritustapa,
            osanId: $scope.editableTutkinnonOsaViite.tutkinnonOsa.id
          },
          $scope.editableTutkinnonOsaViite, function(response){
            $scope.editableTutkinnonOsaViite = angular.copy(response);
            $scope.tutkinnonOsaViite = angular.copy(response);
            Editointikontrollit.lastModified = response;
            saveCb(response.tutkinnonOsa);
            getRakenne();

            tutkinnonOsaDefer = $q.defer();
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
          Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
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
        return $scope.tutkinnonOsaHeaderForm.$valid && Tutke2Service.validate($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
      }
    };

    var setupTutkinnonOsaViite = muokkausCtrlCommons.setupTutkinnonOsaViite($scope, normalCallbacks);

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
  });

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

    this.setPeruste = function (value) {
      peruste = value;
    };

    function goToView(response, id) {
      var params = {
        perusteenOsanTyyppi: 'tekstikappale',
        perusteenOsaViiteId: id || response.id,
        versio: ''
      };
      if (YleinenData.isPerusopetus(peruste)) {
        _.extend(params, {suoritustapa: 'ops'});
      }
      $state.go('root.perusteprojekti.suoritustapa.perusteenosa', params, {reload: true});
    }

    this.add = function () {
      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.saveOsa({}, {osanTyyppi: 'tekstikappale'}, function(response) {
          TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
          goToView(response);
        });
      }
    };

    this.delete = function (viiteId) {
      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.deleteOsa({$url: 'dummy', id: viiteId}, function () {
          Editointikontrollit.cancelEditing();
          Notifikaatiot.onnistui('poisto-onnistui');
          $state.go('root.perusteprojekti.perusopetus', {}, {reload: true});
        });
      } else {
        PerusteenOsaViitteet.delete({viiteId: viiteId}, {}, function() {
          Editointikontrollit.cancelEditing();
          Notifikaatiot.onnistui('poisto-onnistui');
          $state.go('root.perusteprojekti.suoritustapa.sisalto', {}, {reload: true});
        }, Notifikaatiot.serverCb);
      }
    };

    this.addChild = function (viiteId, suoritustapa) {
      if (YleinenData.isPerusopetus(peruste)) {
        PerusopetusService.addSisaltoChild(viiteId, function (response) {
          TutkinnonOsaEditMode.setMode(true);
          goToView(response);
        });
      } else {
        SuoritustapaSisalto.addChild({
          perusteId: peruste.id,
          suoritustapa: suoritustapa,
          perusteenosaViiteId: viiteId
        }, {}, function(response) {
          TutkinnonOsaEditMode.setMode(true);
          goToView(response);
        }, Notifikaatiot.varoitus);
      }
    };

    this.clone = function (viiteId) {
      if (YleinenData.isPerusopetus(peruste)) {

      } else {
        PerusteenOsaViitteet.kloonaaTekstikappale({
          perusteId: peruste.id,
          suoritustapa: $stateParams.suoritustapa,
          viiteId: viiteId
        }, function(tk) {
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

  .directive('muokkausTekstikappale', function() {
    return {
      templateUrl: 'views/partials/muokkaus/tekstikappale.html',
      restrict: 'E',
      scope: {
        tekstikappale: '=',
        versiot: '='
      },
      controller: function($scope, $q, Editointikontrollit, PerusteenOsat,
        Notifikaatiot, VersionHelper, Lukitus, $state,
        TutkinnonOsaEditMode, Varmistusdialogi, $timeout,
        Kaanna, PerusteprojektiTiedotService, $stateParams,
        Utils, PerusteProjektiSivunavi, YleinenData, $rootScope, Kommentit,
        KommentitByPerusteenOsa, PerusteenOsanTyoryhmat, Tyoryhmat, PerusteprojektiTyoryhmat,
        TEXT_HIERARCHY_MAX_DEPTH, TekstikappaleOperations) {

        TekstikappaleOperations.setPeruste($scope.$parent.peruste);
        $scope.kaikkiTyoryhmat = [];

        function paivitaRyhmat(uudet, cb) {
          PerusteenOsanTyoryhmat.save({
            projektiId: $stateParams.perusteProjektiId,
            osaId: $scope.tekstikappale.id
          }, uudet, cb, Notifikaatiot.serverCb);
        }

        $scope.poistaTyoryhma = function(tr) {
          Varmistusdialogi.dialogi({
            successCb: function() {
              var uusi = _.remove(_.clone($scope.tyoryhmat), function(vanha) { return vanha !== tr; });
              paivitaRyhmat(uusi, function() { $scope.tyoryhmat = uusi; });
            },
            otsikko: 'poista-tyoryhma-perusteenosasta',
            teksti: Kaanna.kaanna('poista-tyoryhma-teksti', { nimi: tr })
          })();
        };

        $scope.lisaaTyoryhma = function() {
          Tyoryhmat.valitse(_.clone($scope.kaikkiTyoryhmat), _.clone($scope.tyoryhmat), function(uudet) {
            var uusi = _.clone($scope.tyoryhmat).concat(uudet);
            paivitaRyhmat(uusi, function() { $scope.tyoryhmat = uusi; });
          });
        };

        Utils.scrollTo('#ylasivuankkuri');
        Kommentit.haeKommentit(KommentitByPerusteenOsa, { id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.perusteenOsaViiteId });

        $scope.sisalto = {};
        $scope.viitteet = {};
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

        if ($stateParams.suoritustapa || YleinenData.isPerusopetus($scope.$parent.peruste)) {
          PerusteprojektiTiedotService.then(function(instance) {
            instance.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function(res) {
              $scope.sisalto = res[0];
              $scope.setNavigation();
            });
          });
        }

        $scope.setNavigation = function() {
          $scope.tree.init();
          PerusteProjektiSivunavi.setCrumb($scope.tree.get());
          VersionHelper.setUrl($scope.versiot);
        };

        function lukitse(cb) {
          Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, cb);
        }

        function fetch(cb) {
          PerusteenOsat.get({ osanId: $scope.tekstikappale.id }, _.setWithCallback($scope, 'tekstikappale', cb));
        }

        function storeTree (sisalto, level) {
          level = level || 0;
          _.each(sisalto.lapset, function(lapsi) {
            if (lapsi.perusteenOsa) {
              if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
                $scope.viitteet[lapsi.perusteenOsa.id] = {};
              }
              $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
              $scope.viitteet[lapsi.perusteenOsa.id].level = level;
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
          init: function() {
            updateViitteet();
          },
          get: function() {
            var ids = [];
            var id = $scope.tekstikappale.id;
            if ($scope.viitteet[id]) {
              do {
                ids.push($scope.viitteet[id].viite);
                id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
              } while (id);
            }
            return ids;
          }
        };

        $scope.viiteId = function() {
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
           },{
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
          var tekstikappaleDefer = $q.defer();
          $scope.tekstikappalePromise = tekstikappaleDefer.promise;
          tekstikappaleDefer.resolve($scope.editableTekstikappale);
        }

        function saveCb(res) {
          // Päivitä versiot
          $scope.haeVersiot(true, function () {
            VersionHelper.setUrl($scope.versiot);
          });
          PerusteProjektiSivunavi.refresh();
          Lukitus.vapautaPerusteenosa(res.id);
          Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
          $scope.setNavigation();
        }

        function doDelete() {
          TekstikappaleOperations.delete($scope.viiteId());
        }

        function setupTekstikappale(kappale) {

          $q.all([PerusteenOsanTyoryhmat.get({projektiId: $stateParams.perusteProjektiId, osaId: $scope.tekstikappale.id}).$promise,
            PerusteprojektiTyoryhmat.get({id: $stateParams.perusteProjektiId}).$promise]).then(function (data) {
            $scope.tyoryhmat = data[0];
            $scope.kaikkiTyoryhmat = _.unique(_.map(data[1], 'nimi'));
          }, Notifikaatiot.serverCb);

          $scope.editableTekstikappale = angular.copy(kappale);

          Editointikontrollit.registerCallback({
            edit: function() {
              fetch(function() {
                refreshPromise();
              });
            },
            asyncValidate: function(cb) {
              lukitse(function() { cb(); });
            },
            save: function(kommentti) {
              $scope.editableTekstikappale.metadata = { kommentti: kommentti };
              PerusteenOsat.saveTekstikappale({
                osanId: $scope.editableTekstikappale.id
              }, $scope.editableTekstikappale, saveCb, Notifikaatiot.serverCb);
              $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
              $scope.isNew = false;
            },
            cancel: function() {
              Lukitus.vapautaPerusteenosa($scope.tekstikappale.id, function () {
                if ($scope.isNew) {
                  doDelete();
                }
                else {
                  fetch(function() {
                    refreshPromise();
                  });
                }
                $scope.isNew = false;
              });
            },
            notify: function(mode) {
              $scope.editEnabled = mode;
            },
            validate: function(mandatoryValidator) {
              return mandatoryValidator($scope.fields, $scope.editableTekstikappale);
            }
          });

          $scope.haeVersiot();
          $scope.setNavigation();
          Lukitus.tarkista($scope.tekstikappale.id, $scope);
        }

        if ($scope.tekstikappale) {
          var promiseVar = $scope.tekstikappale.promise ? 'promise' : '$promise';
          $scope.tekstikappalePromise = $scope.tekstikappale[promiseVar].then(function(response) {
            setupTekstikappale(response);
            return $scope.editableTekstikappale;
          });
        }
        else {
          var objectReadyDefer = $q.defer();
          $scope.tekstikappalePromise = objectReadyDefer.promise;
          $scope.tekstikappale = {};
          setupTekstikappale($scope.tekstikappale);
          objectReadyDefer.resolve($scope.editableTekstikappale);
        }


        $scope.kopioiMuokattavaksi = function() {
          TekstikappaleOperations.clone($scope.viitteet[$scope.tekstikappale.id].viite);
        };

        $scope.muokkaa = function() {
          lukitse(function() {
            Editointikontrollit.startEditing();
          });
        };

        $scope.canAddLapsi = function() {
          return $scope.tekstikappale.id &&
            $scope.viitteet[$scope.tekstikappale.id] &&
            $scope.viitteet[$scope.tekstikappale.id].level < (TEXT_HIERARCHY_MAX_DEPTH - 1);
        };

        $scope.addLapsi = function() {
          TekstikappaleOperations.addChild($scope.viiteId(), $stateParams.suoritustapa);
        };

        $scope.setCrumbs = function(crumbs) {
          $scope.crumbs = crumbs;
        };

        $scope.$watch('editEnabled', function(editEnabled) {
          PerusteProjektiSivunavi.setVisible(!editEnabled);
        });

        $scope.haeVersiot = function(force) {
          VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
        };

        function responseFn(response) {
          $scope.tekstikappale = response;
          setupTekstikappale(response);
          var tekstikappaleDefer = $q.defer();
          $scope.tekstikappalePromise = tekstikappaleDefer.promise;
          tekstikappaleDefer.resolve($scope.editableTekstikappale);
          VersionHelper.setUrl($scope.versiot);
        }

        $scope.vaihdaVersio = function () {
          $scope.versiot.hasChanged = true;
          VersionHelper.setUrl($scope.versiot);
          //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
        };

        $scope.revertCb = function(response) {
          responseFn(response);
          saveCb(response);
        };

        $scope.poista = function() {
          var nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

          Varmistusdialogi.dialogi({
          successCb: doDelete,
          otsikko: 'poista-tekstikappale-otsikko',
            teksti: Kaanna.kaanna('poista-tekstikappale-teksti', {nimi: nimi})
          })();
        };

        if (TutkinnonOsaEditMode.getMode()) {
          $scope.isNew = true;
          $timeout(function() {
            $scope.muokkaa();
          }, 50);
        }

        // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
        var received = 0;
        $scope.$on('ckEditorInstanceReady', function() {
          if (++received === $scope.fields.length) {
            $rootScope.$broadcast('editointikontrollitRefresh');
          }
        });
      }
    };
  })

  .directive('termistoViitteet', function (Kaanna) {
    var TERMI_MATCHER = 'abbr[data-viite]';
    return {
      restrict: 'A',
      scope: {
        model: '=termistoViitteet'
      },
      link: function (scope, element) {
        function destroy() {
          element.find(TERMI_MATCHER).each(function () {
            var jqEl = angular.element(this);
            if (jqEl.popover) {
              jqEl.popover('destroy');
            }
          });
        }
        function setup() {
          element.find(TERMI_MATCHER).each(function () {
            var jqEl = angular.element(this);
            var viiteId = jqEl.attr('data-viite');
            var popover = jqEl.popover({
              placement: 'bottom',
              html: true,
              title: Kaanna.kaanna('termin-selitys')
            }).on('show.bs.popover', function () {
              // TODO integrate with backend
              popover.attr('data-content', 'Tähän termin selitys viiteId:llä ' + viiteId);
            });
          });
        }
        scope.$watch('model', function () {
          destroy();
          setup();
        });
        scope.$on('$destroy', destroy);
      },
    };
  });

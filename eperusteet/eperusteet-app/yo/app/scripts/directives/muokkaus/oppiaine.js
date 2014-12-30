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
  .service('tabHelper', function () {
    // Helper to deal with ui-router weirdness
    this.changeInited = false;
  })

  .service('VlkUtils', function () {
    this.orderFn = function (vlk) {
      return _.first(vlk.vuosiluokat.slice().sort());
    };
  })

  .service('OppimaaraHelper', function (PerusopetusService) {
    var instance = null;
    var params = {};
    function OppimaaraHelperImpl(oppiaine) {
      this.oppiaine = oppiaine;
    }

    this.init = function (oppiaine, stateParams) {
      instance = new OppimaaraHelperImpl(oppiaine);
      params = _.clone(stateParams);
      return instance;
    };

    this.reset = function () {
      instance = null;
      params = {};
    };

    this.instance = function() {
      return instance;
    };

    this.getBackState = function () {
      return instance ? ['root.perusteprojekti.suoritustapa.osaalue', params] :
      ['root.perusteprojekti.suoritustapa.osalistaus', {suoritustapa: 'perusopetus', osanTyyppi: PerusopetusService.OPPIAINEET}];
    };

    this.presave = function (model) {
      if (!instance) {
        return false;
      }
      model.$isNew = false;
      model.oppiaine = instance.oppiaine.id;
      return model;
    };
  })

  .directive('muokkausOppiaine', function() {
    return {
      templateUrl: 'views/directives/perusopetus/oppiaine.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'OppiaineController',
      link: function (scope, element) {
        scope.$watch('editEnabled', function (value) {
          if (!value) {
            element.find('.info-placeholder').hide();
          }
        });
      }
    };
  })

  .controller('OppiaineController', function ($scope, PerusopetusService, Kaanna, Notifikaatiot,
      PerusteProjektiSivunavi, Oppiaineet, $timeout, $state, $stateParams, $q, YleinenData, tabHelper,
      CloneHelper, OppimaaraHelper, Utils, $rootScope, Lukitus, VlkUtils, ProjektinMurupolkuService, Varmistusdialogi) {
    $scope.editableModel = {};
    $scope.editEnabled = false;
    $scope.mappedVuosiluokat = [];
    $scope.nameSort = Utils.nameSort;
    $scope.yleisetosat = ['tehtava', 'osaalue'];
    $scope.activeTab =  parseInt($stateParams.tabId, 10);
    var creatingNewOppimaara = !!OppimaaraHelper.instance();
    $scope.oppimaaraRequested = false;
    $scope.oppiaineet = PerusopetusService.getOsat(PerusopetusService.OPPIAINEET, true);
    $scope.oppiaineet.$promise.then(function (data) {
      $scope.oppiaineMap = _.zipObject(_.map(data, function (oppiaine) {
        oppiaine.$url = $scope.generateLink(oppiaine);
        return [''+oppiaine.id, oppiaine];
      }));
    });

    if (_.isNumber(tabHelper.changeInited)) {
      $scope.activeTab = tabHelper.changeInited;
      tabHelper.changeInited = false;
    }

    var cloner = CloneHelper.init(['koosteinen', 'nimi', 'tehtava', 'vuosiluokkakokonaisuudet']);

    $scope.generateLink = function(model) {
      return $state.href('root.perusteprojekti.suoritustapa.osaalue', _.extend(_.clone($stateParams), {
        osanId: _.isObject(model) ? model.id : model,
        tabId: 0
      }));
    };

    $scope.lisaaVlkSisalto = function(osio) {
      $scope.vuosiluokka.$sisalto[osio] = {otsikko: getTitle(osio), teksti: {}};
      saveVanhaOppiaine();
    };

    // TODO: mergeä saveOppiaineen kanssa
    function saveVanhaOppiaine() {
      Lukitus.lukitse(function() {
        Oppiaineet.save({
          perusteId: PerusopetusService.getPerusteId()
        }, $scope.editableModel, function() {
          Lukitus.vapauta();
          Notifikaatiot.onnistui('tallennus-onnistui');
        }, Notifikaatiot.serverCb);
      });
    }

    $scope.vklOsaPoisto = function(path) {
      Varmistusdialogi.dialogi({
        otsikko: 'haluatko-varmasti-poistaa-osion'
      })(function() {
        $scope.vuosiluokka.$sisalto[path] = null;
        saveVanhaOppiaine();
      });
    };

    function saveOppiaine() {
      $scope.editableModel.$save({
        perusteId: PerusopetusService.getPerusteId()
      }, function(res) {
        $scope.editableModel = res;
        Lukitus.vapauta();
        Notifikaatiot.onnistui('tallennus-onnistui');
        $state.go($state.current, _.extend(_.clone($stateParams), {tabId: 0}), {reload: true});
      });
    }

    function processVuosiluokkakokonaisuudet() {
      // Jos oppiaineen vuosiluokkakokonaisuuksia on poistettu, poistetaan ne eksplisiittisesti
      var promises = [];
      var original = _.cloneDeep(cloner.get());
      function getVlkSet(model) {
        return _(model.vuosiluokkakokonaisuudet).pluck('vuosiluokkaKokonaisuus').map(String).value();
      }
      var originalVlkSet = getVlkSet(original),
          newVlkSet = getVlkSet($scope.editableModel),
          removedVlkSet = _.difference(originalVlkSet, newVlkSet),
          originalIds = _.zipObject(originalVlkSet, _.pluck(original.vuosiluokkakokonaisuudet, 'id'));
      if (_.isEmpty(removedVlkSet)) {
        var deferred = $q.defer();
        deferred.resolve();
        promises.push(deferred.promise);
      } else {
        _.each(original.vuosiluokkakokonaisuudet, function (vlk) {
          if (_.indexOf(removedVlkSet, '' + vlk.vuosiluokkaKokonaisuus) > -1) {
            promises.push(PerusopetusService.deleteOppiaineenVuosiluokkakokonaisuus(vlk, $scope.editableModel.id).$promise);
          }
        });
      }
      // Jos vlk on poistettu ja lisätty takaisin, lisätään uudestaan vanha id, jotta ei synny duplikaatteja.
      _.each($scope.editableModel.vuosiluokkakokonaisuudet, function (vlk) {
        vlk.tehtava = {};
        var originalId = originalIds['' + vlk.vuosiluokkaKokonaisuus];
        if (originalId && !vlk.id) {
          vlk.id = originalId;
        }
      });
      return $q.all(promises);
    }

    var callbacks = {
      edit: function() {
        if ($scope.editableModel.id) {
          Lukitus.lukitse(function() {
            cloner.clone($scope.editableModel);
          });
        } else {
          if (!$scope.editableModel.koosteinen) {
            $scope.editableModel.koosteinen = false;
          }
          cloner.clone($scope.editableModel);
        }
      },
      save: function() {
        var oppimaara = OppimaaraHelper.presave($scope.editableModel);
        if (oppimaara) {
          $scope.editableModel = oppimaara;
        }
        if ($scope.editableModel.id) {
          processVuosiluokkakokonaisuudet().then(function () {
            saveOppiaine();
          });
        } else {
          Oppiaineet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, function(res) {
            $scope.editableModel = res;
            $state.go($state.current, _.extend(_.clone($stateParams), {osanId: res.id}), {reload: true});
          });
        }
      },
      cancel: function () {
        function cancelActions() {
          cloner.restore($scope.editableModel);
          if ($scope.editableModel.$isNew) {
            $scope.editableModel.$isNew = false;
            $timeout(function () {
              $state.go.apply($state, $scope.data.options.backState);
            });
          }
        }

        if ($scope.editableModel.$isNew) {
          cancelActions();
        } else {
          Lukitus.vapauta(cancelActions);
        }
      },
      notify: function (value) {
        $scope.editEnabled = value;
        PerusteProjektiSivunavi.setVisible(!value);
      },
      validate: function () { return true; }
    };

    $scope.$on('$destroy', function () {
      if (!$scope.oppimaaraRequested) {
        OppimaaraHelper.reset();
      }
    });

    function updateTypeInfo () {
      var isOppimaara = (creatingNewOppimaara || $scope.editableModel.oppiaine);
      _.extend($scope.data.options, {
        editTitle: isOppimaara ? 'muokkaa-oppimaaraa' : 'muokkaa-oppiainetta',
        newTitle: isOppimaara ? 'uusi-oppimaara' : 'uusi-oppiaine',
        removeWholeLabel: isOppimaara ? 'poista-oppimaara' : 'poista-oppiaine',
        removeWholeConfirmationText: isOppimaara ? 'poistetaanko-oppimaara' : 'poistetaanko-oppiaine',
      });
      var oppiaineLink = [];
      if ($scope.editableModel.oppiaine && $scope.oppiaineMap) {
        var oppiaine = $scope.oppiaineMap[$scope.editableModel.oppiaine];
        oppiaineLink =  [{
          url: $state.href('root.perusteprojekti.suoritustapa.osaalue', {
            osanTyyppi: PerusopetusService.OPPIAINEET,
            osanId: oppiaine.id,
            tabId: 0
          }),
          label: oppiaine.nimi
        }];
      }
      ProjektinMurupolkuService.setCustom(oppiaineLink);
    }

    $scope.data = {
      options: {
        title: function () { return $scope.editableModel.nimi; },
        backLabel: 'oppiaineet',
        backState: OppimaaraHelper.getBackState(),
        removeWholeFn: function () {
          PerusopetusService.deleteOsa($scope.editableModel);
        },
        actionButtons: [
          {
            label: 'lisaa-oppimaara',
            role: 'add',
            callback: function () {
              OppimaaraHelper.init($scope.editableModel, $stateParams);
              $scope.oppimaaraRequested = true;
              $state.go('root.perusteprojekti.suoritustapa.osaalue', {
                suoritustapa: $stateParams.suoritustapa,
                osanTyyppi: PerusopetusService.OPPIAINEET,
                osanId: 'uusi',
                tabId: 0
              });
            },
            hide: '!editableModel.koosteinen'
          }
        ],
        addFieldCb: function (field) {
          if (field.path === 'tehtava' || field.path === 'osaalue') {
            $scope.editableModel[field.path] = {
              otsikko: field.path === 'tehtava' ? {fi: 'Oppiaineen tehtävä'} : {fi: 'Osa-alue'},
              teksti: {fi: ''}
            };
          } else if (field.path === 'vuosiluokkakokonaisuudet') {
            if (!$scope.editableModel.vuosiluokkakokonaisuudet) {
              $scope.editableModel.vuosiluokkakokonaisuudet = [];
            }
            $scope.editableModel.vuosiluokkakokonaisuudet.push(field.empty());
          }
        },
        fieldRenderer: '<div ng-show="editEnabled" oppiaineen-osiot="editableModel" fields="config.fields" ' +
          'vuosiluokkakokonaisuudet="$parent.vuosiluokkakokonaisuudet"></div>',
        fields: [
          /*{
            path: 'tekstikappaleet[].teksti',
            menuLabel: 'tekstikappale',
            localeKey: 'nimi',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            titleplaceholder: 'muokkaus-teksikappaleen-nimi-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 300
          },*/
          {
            path: 'tehtava',
            localeKey: 'oppiaine-osio-tehtava',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 100
          },
          /*{
            path: 'osaalue',
            localeKey: 'oppiaine-osio-osaalue',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 200
          },*/
        ],
        editingCallbacks: callbacks
      }
    };
    updateTypeInfo();

    $scope.chooseTab = function (chosenIndex, noStateChange) {
      _.each($scope.mappedVuosiluokat, function (item, index) {
        item.$tabActive = chosenIndex === index;
      });
      $scope.vuosiluokka = $scope.mappedVuosiluokat[chosenIndex];
      if (!noStateChange) {
        tabHelper.changeInited = chosenIndex;
        var params = _.extend(_.clone($stateParams), {tabId: chosenIndex});
        $state.transitionTo($state.$current.name, params, {
          location: true,
          reload: false,
          notify: false
        });
        $timeout(function () {
          $rootScope.$broadcast('oppiaine:tabChanged');
        });
      }
    };

    $scope.getVuosiluokkakokonaisuus = function (oppiaineenVuosiluokkakokonaisuus) {
      return _.find($scope.vuosiluokkakokonaisuudet, function (item) {
        return item.id === parseInt(oppiaineenVuosiluokkakokonaisuus.vuosiluokkaKokonaisuus, 10);
      });
    };

    $scope.$watch('editableModel.oppimaarat', function () {
      PerusopetusService.getOppimaarat($scope.editableModel).then(function (data) {
        $scope.editableModel.$oppimaarat = data;
        _.each($scope.editableModel.$oppimaarat, function (oppimaara) {
          oppimaara.$url = $scope.generateLink(oppimaara);
        });
      });
    });

    var modelPromise = $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
      if (creatingNewOppimaara && !$scope.editableModel.oppiaine) {
        $scope.editableModel.oppiaine = 'placeholder';
      }
      updateTypeInfo();
    });
    var vuosiluokatPromise = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT).$promise;

    function getTitle(key) {
      var obj = {};
      _.each(YleinenData.kielet, function (kieli) {
        obj[kieli] = Kaanna.kaanna('oppiaine-osio-' + key);
      });
      return obj;
    }
    $scope.getTitle = getTitle;

    function updateChosen() {
      $scope.chosenVuosiluokat = _.map($scope.editableModel.vuosiluokkakokonaisuudet, function (item) {
        return parseInt(item.vuosiluokkaKokonaisuus, 10);
      });
    }

    function mapVuosiluokat() {
      $scope.mappedVuosiluokat = _($scope.editableModel.vuosiluokkakokonaisuudet).map(function (item) {
        var thisItem = $scope.getVuosiluokkakokonaisuus(item);
        thisItem.$sisalto = item;
        return thisItem;
      }).sortBy(VlkUtils.orderFn).value();
    }

    $q.all([modelPromise, vuosiluokatPromise]).then(function (data) {
      // Add addable items to menu
      $scope.vuosiluokkakokonaisuudet = data[1];
      $scope.vuosiluokkakokonaisuudet = _.sortBy($scope.vuosiluokkakokonaisuudet, VlkUtils.orderFn);
      if (_.size($scope.vuosiluokkakokonaisuudet) > 0) {
        $scope.data.options.fields.push({divider: true, order: 99});
      }
      var menuItems = [];
      _.each($scope.vuosiluokkakokonaisuudet, function (item) {
        menuItems.push({
          path: 'vuosiluokkakokonaisuudet',
          localeKey: item.nimi,
          id: item.id,
          empty: function () {
            var vlk = {
              vuosiluokkaKokonaisuus: item.id,
              sisaltoAlueet: [],
              tavoitteet: []
            };
            _.each(['tehtava', 'tyotavat', 'ohjaus', 'arviointi'], function (osio) {
              vlk[osio] = {otsikko: getTitle(osio), teksti: {}};
            });
            return vlk;
          },
          order: 10,
          visibleFn: function () {
            updateChosen();
            return _.indexOf($scope.chosenVuosiluokat, item.id) > -1;
          },
          remove: function () {
            var index = _.findIndex($scope.editableModel.vuosiluokkakokonaisuudet, function (vlk) {
              return parseInt(vlk.vuosiluokkaKokonaisuus, 10) === item.id;
            });
            $scope.editableModel.vuosiluokkakokonaisuudet.splice(index, 1);
          }
        });
      });
      _(menuItems).each(function (item, index) {
        item.order += index;
      });
      $scope.data.options.fields = menuItems.concat($scope.data.options.fields);

      // Jos tätä ei ole tabit vaihtelee satunnaisesti poistoilla ja lisäyksillä
      var valitseTabi = _.once(_.bind($scope.chooseTab, {}, $scope.activeTab, true));
      $scope.$watch('editableModel.vuosiluokkakokonaisuudet', function () {
        mapVuosiluokat();
        valitseTabi();
      }, true);
    });
  })

  .directive('oppiaineenOsiot', function () {
    return {
      restrict: 'AE',
      scope: {
        model: '=oppiaineenOsiot',
        fields: '=',
        vuosiluokkakokonaisuudet: '='
      },
      controller: 'OppiaineenOsiotController',
      templateUrl: 'views/directives/perusopetus/oppiaineenosiot.html'
    };
  })

  .controller('OppiaineenOsiotController', function ($scope, MuokkausUtils, Varmistusdialogi, VlkUtils) {
    $scope.activeVuosiluokat = [];
    $scope.activeOsiot = [];

    function verifyRemove(cb) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-oppiaineen-osio',
        primaryBtn: 'poista',
        successCb: cb
      })();
    }

    function getVlkField(vlk) {
      return _.find($scope.fields, function (field) {
        return field.id === vlk.id;
      });
    }

    $scope.removeVlk = function (vlk) {
      verifyRemove(function () {
        var field = getVlkField(vlk);
        if (field) {
          field.remove();
          field.visible = false;
          field.$added = false;
        }
      });
    };

    $scope.removeOsio = function (osio) {
      verifyRemove(function () {
        osio.field.visible = false;
        osio.field.$added = false;
        MuokkausUtils.nestedSet($scope.model, osio.field.path, '.', null);
      });
    };

    $scope.vlkOrderFn = VlkUtils.orderFn;

    function getField(value) {
      return _.find($scope.fields, function (field) {
        return field.path === value;
      });
    }

    function setOsio(key) {
      if (MuokkausUtils.hasValue($scope.model, key)) {
        var field = getField(key);
        if (field) {
          $scope.activeOsiot.push({model: $scope.model[key], field: field});
          field.visible = true;
        }
      }
    }

    function mapModel() {
      $scope.activeVuosiluokat = [];
      $scope.activeOsiot = [];
      var current = _($scope.model.vuosiluokkakokonaisuudet).pluck('vuosiluokkaKokonaisuus').map(String).value();
      _.each($scope.vuosiluokkakokonaisuudet, function (vlk) {
        if (_.indexOf(current, '' + vlk.id) > -1) {
          $scope.activeVuosiluokat.push(vlk);
          var field = getVlkField(vlk);
          if (field) {
            field.visible = true;
          }
        }
      });
      setOsio('tehtava');
      setOsio('osaalue');
    }

    $scope.$watch('model', function () {
      mapModel();
    }, true);
    $scope.$watch('vuosiluokkakokonaisuudet', function () {
      mapModel();
    }, true);
  });

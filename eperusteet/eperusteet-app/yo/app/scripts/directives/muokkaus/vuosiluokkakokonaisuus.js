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
  .directive('muokkausVuosiluokka', function () {
    return {
      templateUrl: 'views/directives/perusopetus/vuosiluokkakokonaisuus.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'VuosiluokkakokonaisuusController'
    };
  })

  .controller('VuosiluokkakokonaisuusController', function ($scope, PerusopetusService,
    Editointikontrollit, Kaanna, PerusteProjektiSivunavi, Vuosiluokkakokonaisuudet,
    CloneHelper, Lukitus, $timeout, $state, $stateParams, Varmistusdialogi, Utils,
    Notifikaatiot, Kieli, $rootScope) {
    $scope.editableModel = {};
    $scope.isLocked = false;
    $scope.isNew = $stateParams.osanId === 'uusi';
    $scope.editEnabled = false;
    $scope.loaded = false;

    if ($scope.isNew) {
      $timeout(function () {
        $scope.muokkaa();
      }, 200);
    }
    else {
      Lukitus.genericTarkista(function() {
        $scope.isLocked = false;
      }, function(lukitsija) {
        $scope.isLocked = true;
        $scope.lockNotification = lukitsija;
      });
    }

    $scope.vuosiluokkaOptions = {};

    $scope.isPublished = function () {
      return $scope.model.tila === 'julkaistu';
    };

    $scope.canAdd = function () {
      return true;
    };

    var successCb = function (res) {
      PerusopetusService.clearCache();
      $scope.editableModel = res;
      Lukitus.vapauta();
      mapModel();
      Notifikaatiot.onnistui('tallennus-onnistui');
    };

    var cloner = CloneHelper.init([
      'nimi',
      'vuosiluokat',
      'siirtymaEdellisesta',
      'tehtava',
      'siirtymaSeuraavaan',
      'laajaalainenOsaaminen',
      'laajaalaisetOsaamiset',
      'paikallisestiPaatettavatAsiat'
    ]);

    var editingCallbacks = {
      edit: function() {
        mapModel();
        cloner.clone($scope.editableModel);
      },
      asyncValidate: function (cb) {
        if ($scope.editableModel.id) {
          Lukitus.lukitse(cb);
        } else {
          cb();
        }
      },
      save: function () {
        if ($scope.editableModel.id) {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
          }, successCb, Notifikaatiot.serverCb);
        } else {
          Vuosiluokkakokonaisuudet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, function (res) {
            successCb(res);
            $state.go($state.current, _.extend(_.clone($stateParams), {osanId: res.id}), {reload: true});
          }, Notifikaatiot.serverCb);
        }
      },
      cancel: function () {
        cloner.restore($scope.editableModel);
        if ($scope.isNew) {
          $timeout(function () {
            $scope.goToListView();
          });
        } else {
          Lukitus.vapauta();
          $state.go($state.current.name, {}, {reload: true});
        }
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      },
      validate: function () {
        return true;
      }
    };

    Editointikontrollit.registerCallback(editingCallbacks);

    $scope.goToListView = function () {
      $state.go('root.perusteprojekti.suoritustapa.osalistaus', {
        suoritustapa: $stateParams.suoritustapa,
        osanTyyppi: PerusopetusService.VUOSILUOKAT
      }, { reload: true });
    };

    $scope.filterFn = function (item) {
      return item.visible || _.isUndefined(item.visible);
    };

    $scope.config = {
      editTitle: 'muokkaa-vuosiluokkakokonaisuutta',
      newTitle: 'uusi-vuosiluokkakokonaisuus',
      removeWholeLabel: 'poista-vuosiluokkakokonaisuus',
      removeWholeFn: function () {
        Varmistusdialogi.dialogi({
          otsikko: 'varmista-poisto',
          teksti: 'poistetaanko-vuosiluokkakokonaisuus',
          primaryBtn: 'poista',
          successCb: function () {
            Editointikontrollit.cancelEditing();
            PerusopetusService.clearCache();
            PerusopetusService.deleteOsa($scope.editableModel);
            $scope.goToListView();
          }
        })();
      },
      fields: [
        {
          path: 'siirtymaEdellisesta',
          localeKey: 'siirtyma-edellisesta',
          order: 1
        },
        {
          path: 'tehtava',
          localeKey: 'vuosiluokkakokonaisuuden-tehtava',
          order: 2
        },
        {
          path: 'siirtymaSeuraavaan',
          localeKey: 'siirtyma-seuraavaan',
          order: 3
        },
        {
          path: 'laajaalainenOsaaminen',
          localeKey: 'laaja-alainen-osaaminen-kuvaus',
          order: 4
        },
        {
          path: 'laajaalaisetOsaamiset',
          localeKey: 'laaja-alaiset-osaamiset',
          type: 'vuosiluokkakokonaisuuden-osaaminen',
          order: 5
        },
        {
          path: 'paikallisestiPaatettavatAsiat',
          localeKey: 'paikallisesti-paatettavat-asiat',
          order: 6
        }
      ]
    };

    $scope.updateVuosiluokatModel = function () {
      $scope.editableModel.vuosiluokat = _($scope.vuosiluokkaOptions)
        .filter('selected').map('value').value();
    };

    $scope.$watch('editEnabled', function (editEnabled) {
      PerusteProjektiSivunavi.setVisible(!editEnabled);
    });

    $scope.formatVuosiluokka = function (vlEnumValue) {
      return parseInt(_.last(vlEnumValue.split('_')), 10);
    };

    function mapModel() {
      _.each($scope.config.fields, function (field) {
        field.visible = $scope.fieldOps.hasContent(field);
      });

      $scope.vuosiluokkaOptions = _.map(_.range(1, 11), function (item) {
        var vlEnum = 'vuosiluokka_' + item;
        return {
          value: vlEnum,
          label: Kaanna.kaanna('vuosiluokka') + ' ' + item,
          selected: $scope.editableModel.vuosiluokat.indexOf(vlEnum) > -1
        };
      });
    }

    $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
      if (!$scope.editableModel.vuosiluokat) {
        $scope.editableModel.vuosiluokat = [];
      }
      mapModel();
      $scope.loaded = true;
    });

    $scope.muokkaa = function () {
      Lukitus.lukitse(function() {
        Editointikontrollit.startEditing();
      });
    };

    var fieldBackups = {};
    function mapLaajaAlaiset(arr) {
      _.each(arr, function (yleinen) {
        $scope.editableModel.laajaalaisetOsaamiset.push({
          laajaalainenOsaaminen: yleinen.id, kuvaus: {}
        });
      });
    }

    $scope.fieldOps = {
      hasContent: function (field) {
        var model = $scope.editableModel[field.path];
        if (_.isEmpty(model)) {
          return false;
        }
        if (field.type) {
          return !_.isEmpty(model);
        } else {
          var otsikko = model.otsikko;
          var teksti = model.teksti;
          return Utils.hasLocalizedText(otsikko) || Utils.hasLocalizedText(teksti);
        }
      },
      remove: function (field) {
        function doRemove() {
          field.visible = false;
          $scope.editableModel[field.path] = field.type ? [] : null;
        }

        if ($scope.fieldOps.hasContent(field)) {
          Varmistusdialogi.dialogi({
            otsikko: 'varmista-poisto',
            teksti: 'poistetaanko-osio',
            primaryBtn: 'poista',
            successCb: function () {
              doRemove();
            }
          })();
        } else {
          doRemove();
        }
      },
      edit: function (field) {
        field.$editing = true;
        field.$isCollapsed = false;
        fieldBackups[field.path] = _.cloneDeep($scope.editableModel[field.path]);
      },
      cancel: function (field) {
        field.$editing = false;
        $scope.editableModel[field.path] = _.cloneDeep(fieldBackups[field.path]);
        fieldBackups[field.path] = null;
      },
      ok: function (field) {
        field.$editing = false;
        fieldBackups[field.path] = null;
        $rootScope.$broadcast('notifyCKEditor');
      },
      add: function (field) {
        field.visible = true;
        if (field.type) {
          if (!$scope.editableModel.laajaalaisetOsaamiset) {
            $scope.editableModel.laajaalaisetOsaamiset = [];
          }
          PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true).then(function (res) {
            mapLaajaAlaiset(res);
          });
       } else {
          if (!$scope.editableModel[field.path]) {
            $scope.editableModel[field.path] = {
              otsikko: {},
              teksti: {}
            };
            // TODO: Kaanna/translate can't be used to translate other than current ui language
            // with strings (not localized objects).
            // We should have kaannaSisalto for strings but $translate doesn't support it without
            // changing the current ui language.
            $scope.editableModel[field.path].otsikko[Kieli.getSisaltokieli()] = Kaanna.kaanna(field.localeKey);
          }
        }
        field.$editing = true;
        $timeout(function () {
          Utils.scrollTo('.osio-' + field.path, -100);
        }, 200);
      }
    };

  })

  .directive('vuosiluokkakokonaisuudenOsaaminen', function () {
    return {
      templateUrl: 'views/directives/perusopetus/vuosiluokkakokonaisuudenosaaminen.html',
      restrict: 'A',
      scope: {
        object: '=vuosiluokkakokonaisuudenOsaaminen',
        editEnabled: '='
      },
      controller: 'LaajaAlainenOsaaminenController'
    };
  })

  .controller('LaajaAlainenOsaaminenController', function ($scope, PerusopetusService, Utils) {
    $scope.oneAtATime = false;
    $scope.yleiset = [];
    PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true).then(function (res) {
      $scope.yleiset = res;
    });
    $scope.orderFn = Utils.nameSort;

    function getModel(object, item) {
      var model = _.find(object, function (obj) {
        return parseInt(obj.laajaalainenOsaaminen, 10) === item.id;
      });
      if (!model) {
        model = {
          laajaalainenOsaaminen: item.id,
          kuvaus: {}
        };
      }
      return model;
    }

    function refresh(initial) {
      _.each($scope.yleiset, function (item) {
        if (initial) {
          item.$isOpen = true;
        }
        item.$model = getModel($scope.object, item);
      });
    }
    $scope.$watch('object', refresh);
    $scope.$watch('yleiset', _.partial(refresh, true), true);
    refresh(true);
  });

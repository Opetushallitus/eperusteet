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
    CloneHelper, Lukitus, $timeout, $state) {
    $scope.editableModel = {};
    $scope.editEnabled = false;
    $scope.vuosiluokkaOptions = {};

    $scope.updateVuosiluokatModel = function () {
      $scope.editableModel.vuosiluokat = _($scope.vuosiluokkaOptions)
        .filter('selected').map('value').value();
    };

    $scope.$watch('editEnabled', function (editEnabled) {
      PerusteProjektiSivunavi.setVisible(!editEnabled);
    });

    var successCb = function (res) {
      $scope.editableModel = res;
      Lukitus.vapautaVuosiluokkakokonaisuus($scope.editableModel.id);
      mapModel();
    };

    var cloner = CloneHelper.init(['nimi', 'tehtava', 'tekstikappaleet', 'laajaalaisetOsaamiset']);

    var editingCallbacks = {
      edit: function () {
        mapModel();
        if ($scope.editableModel.id) {
          Lukitus.lukitseVuosiluokkakokonaisuus($scope.editableModel.id, function() {
            cloner.clone($scope.editableModel);
          });
        } else {
          cloner.clone($scope.editableModel);
        }
      },
      asyncValidate: function (cb) {
        lukitse(function () {
          cb();
        });
      },
      save: function () {
        // hax until backend support
        if ($scope.editableModel.tekstikappaleet && $scope.editableModel.tekstikappaleet.length > 0) {
          $scope.editableModel.tehtava = _.cloneDeep($scope.editableModel.tekstikappaleet[0]);
        }
        if ($scope.editableModel.id) {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
          }, successCb);
        } else {
          Vuosiluokkakokonaisuudet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, successCb);
        }
      },
      cancel: function () {
        cloner.restore($scope.editableModel);
        if ($scope.editableModel.$isNew) {
          $timeout(function () {
            $state.go.apply($state, $scope.data.options.backState);
          });
        } else {
          Lukitus.vapautaVuosiluokkakokonaisuus($scope.editableModel.id);
        }
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      },
      validate: function () {
        return true;
      }
    };

    $scope.data = {
      options: {
        title: function () {
          return $scope.editableModel.nimi;
        },
        editTitle: 'muokkaa-vuosiluokkakokonaisuutta',
        newTitle: 'uusi-vuosiluokkakokonaisuus',
        backLabel: 'vuosiluokkakokonaisuudet',
        backState: ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.VUOSILUOKAT}],
        removeWholeLabel: 'poista-vuosiluokkakokonaisuus',
        removeWholeConfirmationText: 'poistetaanko-vuosiluokkakokonaisuus',
        removeWholeFn: function () {
          PerusopetusService.deleteOsa($scope.editableModel);
        },
        addFieldCb: function (field) {
          if (field.path === 'laajaalaisetOsaamiset') {
            if (!$scope.editableModel.laajaalaisetOsaamiset) {
              $scope.editableModel.laajaalaisetOsaamiset = [];
            }
            var yleiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true);
            _.each(yleiset, function (yleinen) {
              $scope.editableModel.laajaalaisetOsaamiset.push({
                laajaalainenOsaaminen: yleinen.id, kuvaus: {}
              });
            });
            /* } else if (field.path === 'tehtava') {
             $scope.editableModel.tehtava = {};
             } */
          } else {
            /*if (!$scope.editableModel.tekstikappaleet) {
             $scope.editableModel.tekstikappaleet = [];
             $scope.editableModel.tekstikappaleet.push({otsikko: {}, teksti: {}});
             }*/
          }
        },
        fieldRenderer: '<kenttalistaus edit-enabled="editEnabled" object-promise="modelPromise" fields="config.fields"></kenttalistaus>',
        fields: [
          {
            path: 'laajaalaisetOsaamiset',
            localeKey: 'laaja-alainen-osaaminen',
            type: 'vuosiluokkakokonaisuuden-osaaminen',
            collapsible: true,
            order: 1
          },
          {
            path: 'tekstikappaleet[].teksti',
            menuLabel: 'tekstikappale',
            localeKey: 'otsikko',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            titleplaceholder: 'muokkaus-teksikappaleen-nimi-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 2
          }/*,
           {
           path: 'tehtava.teksti',
           localeKey: 'tehtava',
           originalLocaleKey: 'otsikko',
           type: 'editor-area',
           collapsible: true,
           isolateEdit: true,
           localized: true,
           order: 3,
           }*/
        ],
        editingCallbacks: editingCallbacks
      }
    };

    function lukitse(cb) {
      cb();
    }

    $scope.formatVuosiluokka = function (vlEnumValue) {
      return parseInt(_.last(vlEnumValue.split('_')), 10);
    };

    function mapModel() {
      // hax until backend support
      if (!$scope.editableModel.tekstikappaleet) {
        $scope.editableModel.tekstikappaleet = [{}];
      }
      $scope.editableModel.tekstikappaleet[0] = _.cloneDeep($scope.editableModel.tehtava);

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
    });
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

  .controller('LaajaAlainenOsaaminenController', function ($scope, PerusopetusService, YleinenData) {
    $scope.oneAtATime = false;
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.yleiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true);

    function getModel(object, item) {
      var model = _.find(object, function (obj) {
        return parseInt(obj.laajaalainenOsaaminen, 10) === item.id;
      });
      if (!model) {
        model = {
          laajaalainenOsaaminen: item.id,
          kuvaus: {}
        };
        $scope.$parent.$parent.object.laajaalaisetOsaamiset.push(model);
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
    refresh(true);
  });

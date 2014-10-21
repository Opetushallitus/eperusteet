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

  .controller('OppiaineController', function ($scope, PerusopetusService, Kaanna,
      PerusteProjektiSivunavi, Oppiaineet, $timeout, $state, $stateParams, $q, YleinenData) {
    $scope.editableModel = {};
    $scope.editEnabled = false;
    $scope.mappedVuosiluokat = [];
    $scope.yleisetosat = ['tehtava', 'osaalue'];

    var callbacks = {
      edit: function () {},
      save: function () {
        if (!$scope.editableModel.id) {
          Oppiaineet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, function (res) {
            $scope.editableModel = res;
            $state.transitionTo($state.current, _.extend(_.clone($stateParams), {osanId: res.id}), {
              reload: true, inherit: false, notify: false
            });
          });
        } else {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
          }, function (res) {
            $scope.editableModel = res;
          });
        }
      },
      cancel: function () {
        if ($scope.editableModel.$isNew) {
          $timeout(function () {
            $state.go.apply($state, $scope.data.options.backState);
          });
        }
      },
      notify: function (value) {
        $scope.editEnabled = value;
        PerusteProjektiSivunavi.setVisible(!value);
      },
      validate: function () { return true; }
    };

    $scope.data = {
      options: {
        title: function () { return $scope.editableModel.nimi; },
        editTitle: 'muokkaa-oppiainetta',
        newTitle: 'uusi-oppiaine',
        backLabel: 'oppiaineet',
        backState: ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.OPPIAINEET}],
        removeWholeLabel: 'poista-oppiaine',
        removeWholeConfirmationText: 'poistetaanko-oppiaine',
        removeWholeFn: function () {
          // TODO delete
        },
        actionButtons: [
          {
            label: 'lisaa-oppimaara',
            role: 'add',
            callback: function () {
            },
            hide: '!model.koosteinen'
          }
        ],
        addFieldCb: function (field) {
          console.log("addFieldCb", field);
          if (field.path === 'tehtava') {
            $scope.editableModel.tehtava = {
              otsikko: {fi: 'Oppiaineen tehtävä'},
              teksti: {fi: ''}
            };
          } else if (field.path === 'vuosiluokkakokonaisuudet') {
            if (!$scope.editableModel.vuosiluokkakokonaisuudet) {
              $scope.editableModel.vuosiluokkakokonaisuudet = [];
            }
            $scope.editableModel.vuosiluokkakokonaisuudet.push(field.empty());
          }
        },
        fieldRenderer: '<kenttalistaus mode="sortable" edit-enabled="editEnabled" ' +
          'object-promise="modelPromise" fields="config.fields" hide-empty-placeholder="true"></kenttalistaus>',
        fields: [
          {
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
          },
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
          {
            path: 'osaalue',
            localeKey: 'oppiaine-osio-osaalue',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 200
          },

        ],
        editingCallbacks: callbacks
      }
    };

    $scope.getVuosiluokkakokonaisuus = function (oppiaineenVuosiluokkakokonaisuus) {
      return _.find($scope.vuosiluokkakokonaisuudet, function (item) {
        return item.id === parseInt(oppiaineenVuosiluokkakokonaisuus.vuosiluokkaKokonaisuus, 10);
      });
    };

    var modelPromise = $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
    });
    var vuosiluokatPromise = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT).$promise;

    function getTitle(key) {
      var obj = {};
      _.each(YleinenData.kielet, function (kieli) {
        obj[kieli] = Kaanna.kaanna('oppiaine-osio-' + key);
      });
      return obj;
    }

    function updateChosen() {
      $scope.chosenVuosiluokat = _.map($scope.editableModel.vuosiluokkakokonaisuudet, function (item) {
        return parseInt(item.vuosiluokkaKokonaisuus, 10);
      });
    }

    $q.all([modelPromise, vuosiluokatPromise]).then(function (data) {
      //updateChosen();
      // Add addable items to menu
      $scope.vuosiluokkakokonaisuudet = data[1];
      if (_.size($scope.vuosiluokkakokonaisuudet) > 0) {
        $scope.data.options.fields.push({divider: true, order: 99});
      }
      var menuItems = [];
      _.each($scope.vuosiluokkakokonaisuudet, function (item) {
        menuItems.push({
          path: 'vuosiluokkakokonaisuudet',
          localeKey: item.nimi,
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
      _(menuItems).sortBy(function (item) {
        return Kaanna.kaanna(item.localeKey);
      }).each(function (item, index) {
        item.order += index;
      });
      $scope.data.options.fields = menuItems.concat($scope.data.options.fields);

      $scope.$watch('editableModel.vuosiluokkakokonaisuudet', function () {
        //updateChosen();
        $scope.mappedVuosiluokat = _($scope.editableModel.vuosiluokkakokonaisuudet).map(function (item) {
          var thisItem = $scope.getVuosiluokkakokonaisuus(item);
          thisItem.$sisalto = item;
          return thisItem;
        }).sortBy(function (item) {
          return Kaanna.kaanna(item.nimi);
        }).value();
      }, true);
    });
  });

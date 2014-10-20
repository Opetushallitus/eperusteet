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
      PerusteProjektiSivunavi, Oppiaineet, $timeout, $state) {
    $scope.editableModel = {};
    $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
    });
    $scope.editEnabled = false;
    $scope.mappedVuosiluokat = [];
    $scope.yleisetosat = ['tehtava', 'osaalue'];

    var callbacks = {
      edit: function () {},
      save: function () {
        if (!$scope.editableModel.id) {
          // TODO
          Oppiaineet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, {
            nimi: $scope.editableModel.nimi
          });
        } else {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
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
          if (field.path === 'tehtava') {
            $scope.editableModel.tehtava = {
              otsikko: {fi: 'Oppiaineen tehtävä'},
              teksti: {fi: ''}
            };
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
            localeKey: 'oppiaineen-tehtava',
            type: 'editor-area',
            placeholder: 'muokkaus-tekstikappaleen-teksti-placeholder',
            localized: true,
            collapsible: true,
            isolateEdit: true,
            order: 100
          },
          {
            path: 'osaalue',
            localeKey: 'osaalue',
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

    var vuosiluokatPromise = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT).$promise.then(function (data) {
      $scope.vuosiluokkakokonaisuudet = data;
      if (_.size($scope.vuosiluokkakokonaisuudet) > 0) {
        $scope.data.options.fields.push({divider: true, order: 99});
      }
      _.each($scope.vuosiluokkakokonaisuudet, function (item, index) {
        $scope.data.options.fields.unshift({
          path: 'vuosiluokkakokonaisuudet['+index+']',
          localeKey: item.nimi,
          empty: function () {
            return {
              vuosiluokkaKokonaisuus: item.id,
              sisaltoAlueet: [],
              tavoitteet: []
            };
          },
          order: 10
        });
      });
    });

    vuosiluokatPromise.then(function () {
      $scope.$watch('model.vuosiluokkakokonaisuudet', function () {

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

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
  .directive('muokkausVuosiluokka', function() {
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
      Editointikontrollit, Kaanna, PerusteProjektiSivunavi, Vuosiluokkakokonaisuudet) {
    $scope.editableModel = {};
    $scope.editEnabled = false;
    $scope.vuosiluokkaOptions = _.map(_.range(1, 10), function (item) {
      return {
        value: item,
        label: Kaanna.kaanna('vuosiluokka') + ' ' + item,
        selected: /*$scope.editableModel.vuosiluokat.indexOf(item) > -1*/ false
      };
    });

    $scope.updateVuosiluokatModel = function () {
      $scope.editableModel.vuosiluokat = _($scope.vuosiluokkaOptions)
        .filter('selected').map('value').value();
    };

    $scope.$watch('editEnabled', function(editEnabled) {
      PerusteProjektiSivunavi.setVisible(!editEnabled);
    });

    var editingCallbacks = {
      edit: function() {
        refetch();
      },
      asyncValidate: function(cb) {
        lukitse(function() { cb(); });
      },
      save: function(/*kommentti*/) {
        if ($scope.editableModel.id) {
          $scope.editableModel.$save({
            perusteId: PerusopetusService.getPerusteId()
          }, function (res) {
            $scope.editableModel = res;
          });
        } else {
          Vuosiluokkakokonaisuudet.save({
            perusteId: PerusopetusService.getPerusteId()
          }, $scope.editableModel, function (res) {
            $scope.editableModel = res;
          });
        }
      },
      cancel: function() {
        // TODO delete lock
        refetch();
      },
      notify: function(mode) {
        $scope.editEnabled = mode;
      },
      validate: function() {
        return true;
      }
    };

    $scope.data = {
      options: {
        title: function () { return $scope.editableModel.nimi; },
        editTitle: 'muokkaa-vuosiluokkakokonaisuutta',
        newTitle: 'uusi-vuosiluokkakokonaisuus',
        backLabel: 'vuosiluokkakokonaisuudet',
        backState: ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.VUOSILUOKAT}],
        removeWholeLabel: 'poista-vuosiluokkakokonaisuus',
        removeWholeConfirmationText: 'poistetaanko-vuosiluokkakokonaisuus',
        removeWholeFn: function () {
          // TODO delete
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
          } else if (field.path === 'tehtava') {
            $scope.editableModel.tehtava = {};
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
            order: 2
          },*/
          {
            path: 'tehtava.teksti',
            localeKey: 'tehtava',
            originalLocaleKey: 'otsikko',
            type: 'editor-area',
            collapsible: true,
            isolateEdit: true,
            localized: true,
            order: 3,
          }
        ],
        editingCallbacks: editingCallbacks
      }
    };

    function lukitse(cb) {
      cb();
    }

    function refetch() {
      //$scope.editableModel = angular.copy($scope.model);
    }

    $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
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
      return _.find(object, function (obj) {
        return parseInt(obj.laajaalainenOsaaminen, 10) === item.id;
      });
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

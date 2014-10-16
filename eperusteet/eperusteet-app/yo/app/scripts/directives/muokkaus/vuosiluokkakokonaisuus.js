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
    $scope.editEnabled = false;
    $scope.editableModel = angular.copy($scope.model);
    $scope.vuosiluokkaOptions = _.map(_.range(1, 10), function (item) {
      return {
        value: item,
        label: Kaanna.kaanna('vuosiluokka') + ' ' + item,
        selected: $scope.editableModel.vuosiluokat.indexOf(item) > -1
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
        // TODO set metadata, save
        Vuosiluokkakokonaisuudet.save({
          perusteId: PerusopetusService.getPerusteId()
        }, {
          nimi: $scope.editableModel.nimi,
          tehtava: {fi: 'joku teksti'}
        });
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
        title: $scope.model.nimi,
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
          if (field.path === 'osaamisenkuvaukset') {
            if (!$scope.editableModel.osaamisenkuvaukset) {
              $scope.editableModel.osaamisenkuvaukset = [];
            }
            // TODO don't fetch the real data here
            var yleiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN);
            _.each(yleiset, function (yleinen) {
              $scope.editableModel.osaamisenkuvaukset.push({
                osaaminen: yleinen.perusteenOsa.id, teksti: {}
              });
            });
          }
        },
        fieldRenderer: '<kenttalistaus edit-enabled="editEnabled" object-promise="modelPromise" fields="config.fields"></kenttalistaus>',
        fields: [
          {
            path: 'osaamisenkuvaukset',
            localeKey: 'laaja-alainen-osaaminen',
            type: 'osaaminen',
            collapsible: true,
            order: 1
          },
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
            order: 2
          }
        ],
        editingCallbacks: editingCallbacks
      }
    };

    function lukitse(cb) {
      cb();
    }

    function refetch() {
      $scope.editableModel = angular.copy($scope.model);
    }

  })

  .directive('osaaminen', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osaaminen.html',
      restrict: 'A',
      scope: {
        object: '=osaaminen',
        editEnabled: '='
      },
      controller: 'LaajaAlainenOsaaminenController'
    };
  })

  .controller('LaajaAlainenOsaaminenController', function ($scope, PerusopetusService, YleinenData) {
    $scope.oneAtATime = false;
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.yleiset = PerusopetusService.getOsat(PerusopetusService.OSAAMINEN);

    function getModel(object, item) {
      return _.find(object, function (obj) {
        return obj.osaaminen === item.perusteenOsa.id;
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

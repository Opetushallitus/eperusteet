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
  .directive('muokkausOsaaminen', function() {
    return {
      templateUrl: 'views/directives/perusopetus/osaaminen.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'MuokkausOsaaminenController'
    };
  })

  .controller('MuokkausOsaaminenController', function ($scope, PerusopetusService,
      PerusteProjektiSivunavi, YleinenData, $stateParams, CloneHelper, $timeout, $state) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.editableModel = {};
    $scope.editEnabled = false;

    var cloner = CloneHelper.init(['nimi', 'kuvaus']);

    var callbacks = {
      edit: function () {
        cloner.clone($scope.editableModel);
      },
      save: function () {
        var isNew = !$scope.editableModel.id;
        PerusopetusService.saveOsa($scope.editableModel, $stateParams, function(tallennettu) {
          $scope.editableModel = tallennettu;
          if (isNew) {
            $state.go($state.current, _.extend(_.clone($stateParams), {osanId: tallennettu.id}), {reload: true});
          }
        });
      },
      cancel: function () {
        cloner.restore($scope.editableModel);
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
        editTitle: 'muokkaa-osaaminen',
        newTitle: 'uusi-osaaminen',
        backLabel: 'laaja-alainen-osaaminen',
        backState: ['root.perusteprojekti.osalistaus', {osanTyyppi: PerusopetusService.OSAAMINEN}],
        removeWholeLabel: 'poista-osaamiskokonaisuus',
        removeWholeConfirmationText: 'poistetaanko-osaamiskokonaisuus',
        removeWholeFn: function () {
          PerusopetusService.deleteOsa($scope.editableModel);
        },
        fields: [],
        editingCallbacks: callbacks
      }
    };


    $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
    });

  });

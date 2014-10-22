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
      PerusteProjektiSivunavi, YleinenData, $stateParams) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.editableModel = {};
    $scope.editEnabled = false;

    var callbacks = {
      edit: function () {},
      save: function () {
        // TODO
        PerusopetusService.saveOsa({}, $stateParams);
      },
      cancel: function () {

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
          // TODO delete
        },
        fields: [],
        editingCallbacks: callbacks
      }
    };


    var modelPromise = $scope.model.then(function (data) {
      $scope.editableModel = angular.copy(data);
    });

    modelPromise.then(function (data) {

    });
  });

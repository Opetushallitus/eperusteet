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
  .directive('osanmuokkausSisaltoalueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaussisaltoalueet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      },
      controller: function ($scope, YleinenData, $rootScope, Utils) {
        $scope.editables = $scope.model;
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.isEditing = false;

        var originals = null;
        $scope.edit = function (alue) {
          alue.$editing = true;
          $scope.isEditing = true;
          originals = _.cloneDeep(_.pick(alue, ['nimi', 'kuvaus']));
        };
        $scope.remove = function (alue) {
          var index = _.findIndex($scope.editables, function (item) {
            return item === alue;
          });
          if (index > -1) {
            $scope.editables.splice(index, 1);
          }
        };
        $scope.cancel = function (alue) {
          alue.$editing = false;
          $scope.isEditing = false;
          if (alue.$new) {
            $scope.remove(alue);
          } else {
            alue.nimi = _.cloneDeep(originals.nimi);
            alue.kuvaus = _.cloneDeep(originals.kuvaus);
            originals = null;
          }
        };
        $scope.ok = function (alue) {
          $rootScope.$broadcast('notifyCKEditor');
          if (!$scope.hasTitle(alue)) {
            return;
          }
          alue.$editing = false;
          $scope.isEditing = false;
        };
        $scope.add = function () {
          $scope.isEditing = true;
          $scope.editables.push({
            $editing: true,
            $new: true,
            nimi: {},
            kuvaus: {}
          });
        };
        $scope.hasTitle = function (alue) {
          return Utils.hasLocalizedText(alue.nimi);
        };
      }
    };
  })

  .directive('sisaltoalueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/sisaltoalueet.html',
      restrict: 'A',
      scope: {
        model: '=sisaltoalueet'
      },
      controller: 'SisaltoalueetController'
    };
  })
  .controller('SisaltoalueetController', function ($scope, YleinenData) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
  });

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
  .service('PerusteprojektinTilanvaihto', function ($modal) {
    var that = this;
    this.start = function (currentStatus, setFn) {
      var dummyDescription = 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.';
      if (_.isFunction(setFn)) {
        that.setFn = setFn;
      }
      $modal.open({
        templateUrl: 'views/modals/perusteprojektinTila.html',
        controller: 'PerusteprojektinTilaModal',
        resolve: {
          data: function () {
            return {
              oldStatus: currentStatus,
              // TODO: mikä on tilojen ja kuvausten oikea asuinpaikka?
              statuses: _.map([
                'luonnos',
                'kommentointi',
                'viimeistely',
                'kaannos',
                'hyvaksytty'
              ], function (item) {
                return {'key': item, 'description': {'fi': dummyDescription}};
              })
            };
          }
        }
      });
    };
    this.set = function (status) {
      that.setFn(status);
    };
  })

  .controller('PerusteprojektinTilaModal', function ($scope, $modal, $modalInstance, data) {
    $scope.data = data;
    $scope.data.selected = null;
    $scope.data.editable = false;
    $scope.valitse = function () {
      $modalInstance.close();
      $modal.open({
        templateUrl: 'views/modals/perusteprojektinTilaVarmistus.html',
        controller: 'PerusteprojektinTilaVarmistusModal',
        resolve: {
          data: function () { return $scope.data; }
        }
      });
    };
    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };
  })

  .controller('PerusteprojektinTilaVarmistusModal', function ($scope,
      $modalInstance, data, PerusteprojektinTilanvaihto) {
    $scope.data = data;
    $scope.edellinen = function () {
      $modalInstance.dismiss();
      PerusteprojektinTilanvaihto.start(data.oldStatus);
    };
    $scope.ok = function () {
      PerusteprojektinTilanvaihto.set(data.selected);
      $modalInstance.close();
    };
    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };
  });

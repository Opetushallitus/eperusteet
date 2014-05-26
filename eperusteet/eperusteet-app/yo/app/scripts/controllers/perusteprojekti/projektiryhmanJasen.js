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

  .controller('projektiryhmanJasenCtrl', function ($scope, $modal, $modalInstance, data, kayttajaToiminnot) {
    $scope.jasen = angular.copy(data.jasen);
    $scope.kayttajaToiminnot = kayttajaToiminnot;

    $scope.tallenna = function () {
      _.each(['nimi', 'puhelin', 'email', 'rooli'], function (kentta) {
        data.jasen[kentta] = $scope.jasen[kentta];
      });
      $modalInstance.close();
    };

    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };

    $scope.poista = function () {
      $modal.open({
        template: '<div class="modal-header"><p>Poistetaanko {{jasen.nimi}}?</p></div>' +
            '<div class="modal-footer"><button ng-click="poista()" class="btn btn-primary" translate>poista</button>' +
            '<button ng-click="peruuta()" class="btn btn-default" translate>peru</button></div>',
        controller: 'poistaJasenModalCtrl',
        resolve: {
          data: function () { return data; }
        }
      }).result.then(function () {
        $modalInstance.close();
      });
    };
  })

  .controller('poistaJasenModalCtrl', function ($scope, $modalInstance, data) {
    $scope.jasen = data.jasen;
    $scope.peruuta = function () {
      $modalInstance.dismiss();
    };
    $scope.poista = function () {
      $modalInstance.close();
      _.remove(data.ryhma, function (alkio) {
        return alkio === data.jasen;
      });
    };
  })

  .controller('jasenkorttiCtrl', function ($scope, $modal) {
    $scope.muokkaaJasenta = function (jasen, ryhma) {
      $modal.open({
        templateUrl: 'views/modals/projektiryhmanJasen.html',
        controller: 'projektiryhmanJasenCtrl',
        resolve: {
          data: function () { return {jasen: jasen, ryhma: ryhma}; }
        }
      });
    };
  });

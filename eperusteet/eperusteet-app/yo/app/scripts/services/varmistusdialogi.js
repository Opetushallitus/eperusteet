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

angular.module('eperusteApp')
  .service('Varmistusdialogi', function($modal) {

    function dialogi(options) {

      return function() {
        var resolve = {};
        resolve.otsikko = function() {
          return options.otsikko || '';
        };
        resolve.teksti = function() {
          return options.teksti || '';
        };
        resolve.lisaTeksti = function() {
          return options.lisaTeksti || '';
        };
        resolve.cbData = function() {
          return options.data || null;
        };
        resolve.primaryBtn = function() {
          return options.primaryBtn || 'ok';
        };
        resolve.secondaryBtn = function() {
          return options.secondaryBtn || 'peruuta';
        };
        var failureCb = options.failureCb || angular.noop;
        var successCb = options.successCb || angular.noop;

        $modal.open({
          templateUrl: 'views/modals/varmistusdialogi.html',
          controller: 'VarmistusDialogiCtrl',
          resolve: resolve
        }).result.then(successCb, failureCb);
      };
    }

    return {
      dialogi: dialogi
    };

  })
  .controller('VarmistusDialogiCtrl', function($scope, $modalInstance, otsikko, teksti, lisaTeksti, cbData, primaryBtn, secondaryBtn) {

    $scope.otsikko = otsikko;
    $scope.teksti = teksti;
    $scope.lisaTeksti = lisaTeksti;
    $scope.primaryBtn = primaryBtn;
    $scope.secondaryBtn = secondaryBtn;

    $scope.ok = function() {
      if (cbData !== null) {
        $modalInstance.close(cbData);
      } else {
        $modalInstance.close();
      }
    };

    $scope.peruuta = function() {
      $modalInstance.dismiss();
    };
  });

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

angular.module('eperusteApp')
  .service('Varmistusdialogi', function($uibModal) {

    function dialogi(options) {
      return function(success, failure) {
        var resolve = {
          opts: function () {
            return {
              primaryBtn: options.primaryBtn || 'ok',
              secondaryBtn: options.secondaryBtn || 'peruuta'
            };
          },
          data: function() { return options.data || null; },
          otsikko: function() { return options.otsikko || ''; },
          teksti: function() { return options.teksti || ''; },
          lisaTeksti: function() { return options.lisaTeksti || ''; },
          comment: function() { return options.comment || {}; }
        };
        var successCb = success || options.successCb || angular.noop;
        var failureCb = failure || options.failureCb || angular.noop;

        $uibModal.open({
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
  .controller('VarmistusDialogiCtrl', function($scope, $uibModalInstance, opts, data, otsikko, teksti, lisaTeksti, comment) {
    $scope.opts = opts;
    $scope.otsikko = otsikko;
    $scope.teksti = teksti;
    $scope.lisaTeksti = lisaTeksti;
    $scope.comment = comment;

    $scope.ok = function() {
      if (data !== null) {
        $uibModalInstance.close(data);
      } else {
        $uibModalInstance.close();
      }
    };

    $scope.peruuta = function() {
      $uibModalInstance.dismiss();
    };
  });

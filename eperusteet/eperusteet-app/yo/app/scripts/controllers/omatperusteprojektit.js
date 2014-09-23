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
  .controller('OmatperusteprojektitCtrl', function ($scope, $state, OmatPerusteprojektit, PerusteProjektiService) {
    $scope.projektit = {};
    $scope.naytto = {limit: 5, shown: 5};

    var paivitaOmatProjektit = function() {
      OmatPerusteprojektit.query({}, function(vastaus) {
        $scope.projektit = _(vastaus)
          .forEach(function(pp) {
            pp.url = PerusteProjektiService.getUrl(pp);
          })
          .reverse()
          .value();
      });
    };

    paivitaOmatProjektit();

    $scope.$on('update:perusteprojekti', function() {
      paivitaOmatProjektit();
    });
  })
.directive('limitToggler', function () {
  return {
    restrict: 'AE',
    template: '<div class="show-toggler" ng-show="isVisible">' +
          '<a class="action-link" ng-click="toggle()">{{linktext| kaanna}}</a>' +
          '</div>',
    scope: {
      'model': '=',
      'limit': '=',
      'limiter': '='
    },
    controller: function ($scope) {
      $scope.isVisible = false;
      $scope.linktext = 'sivupalkki-näytä-kaikki';
      $scope.$watch('model', function () {
        $scope.isVisible = $scope.model.length > $scope.limit;
      });
      $scope.toggle = function () {
        if ($scope.limiter === $scope.limit) {
          $scope.limiter = $scope.model.length;
          $scope.linktext = 'sivupalkki-piilota';
        } else {
          $scope.limiter = $scope.limit;
          $scope.linktext = 'sivupalkki-näytä-kaikki';
        }
      };
    }
  };
});

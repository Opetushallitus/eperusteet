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
  .directive('muokattavaOsio', function() {
    return {
      templateUrl: 'views/directives/muokattavaosio.html',
      restrict: 'A',
      scope: {
        model: '=muokattavaOsio',
        type: '@'
      },
      controller: 'MuokattavaOsioController'
    };
  })
  .controller('MuokattavaOsioController', function($scope, YleinenData, Utils, $state, OsanMuokkausHelper) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

    $scope.hasContent = false;
    $scope.$watch('model', function () {
      $scope.hasContent = $scope.type !== 'tekstikappale' || Utils.hasLocalizedText($scope.model.nimi);
    }, true);

    $scope.edit = function () {
      OsanMuokkausHelper.setBackState();
      // TODO osan id
      $state.go('root.perusteprojekti.muokkaus', {osanTyyppi: $scope.type, osanId: ''});
    };
  })

  .directive('tagCloud', function () {
    return {
      templateUrl: 'views/directives/tagcloud.html',
      restrict: 'A',
      scope: {
        model: '=tagCloud',
        openable: '@',
        tagCloser: '=?'
      },
      controller: 'TagCloudController'
    };
  })
  .controller('TagCloudController', function ($scope) {
    $scope.notHidden = function (item) {
      return !item.$hidden;
    };
    $scope.remove = function (tag) {
      tag.$hidden = true;
    };
  });

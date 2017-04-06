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
  .directive('muokattavaOsio', function() {
    return {
      templateUrl: 'views/directives/muokattavaosio.html',
      restrict: 'A',
      scope: {
        model: '=muokattavaOsio',
        type: '@',
        path: '@?',
        oppiaine: '=?',
        vuosiluokka: '=?',
        poistoCb: '=?'
      },
      controller: 'MuokattavaOsioController',
      link: function (scope: any, element: any, attrs: any) {
        scope.cantremove = !_.isEmpty(attrs.static);
      }
    };
  })
  .controller('MuokattavaOsioController', function($scope, YleinenData, Utils, $state, OsanMuokkausHelper, $stateParams, $log) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.hasContent = false;
    $scope.poistoCb = $scope.poistoCb || angular.noop;

    function update() {
      $scope.realModel = $scope.path ? $scope.model[$scope.path] : $scope.model;
      $scope.hasContent = _.isArray($scope.realModel) || ($scope.realModel && _.has($scope.realModel, 'otsikko')) ||
        $scope.type === 'tavoitteet';
      if (_.isArray($scope.model[$scope.path]) && _.isEmpty($scope.model[$scope.path])) {
        $scope.realModel.$isCollapsed = true;
      }
    }
    update();
    $scope.$watch('model', update, true);

    $scope.edit = function() {
      OsanMuokkausHelper.setup($scope.model, $scope.path, $scope.oppiaine, function() {
        $state.go('root.perusteprojekti.suoritustapa.muokkaus', {
          suoritustapa: $stateParams.suoritustapa,
          osanTyyppi: $scope.type,
          osanId: $scope.realModel.id
        });
      });
    };

    $scope.poista = function() {
      $scope.poistoCb($scope.path);
    };
  });

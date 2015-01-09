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
  .directive('tagCloud', function () {
    return {
      templateUrl: 'views/directives/tagcloud.html',
      restrict: 'A',
      scope: {
        model: '=tagCloud',
        openable: '@',
        editMode: '=',
        addLabel: '@'
      },
      controller: 'TagCloudController'
    };
  })
  .controller('TagCloudController', function ($scope, $modal, Utils) {
    $scope.notHidden = function (item) {
      return !item.$hidden;
    };

    $scope.remove = function (tag) {
      tag.$hidden = true;
    };

    $scope.orderFn = Utils.nameSort;

    $scope.openDialog = function () {
      var modal = $modal.open({
        templateUrl: 'views/modals/tagcloudmodal.html',
        controller: 'TagCloudModalController',
        resolve: {
          model: function() { return $scope.model; },
          addLabel: function () { return $scope.addLabel; }
        }
      });

      modal.result.then(function() {
      });
    };

    $scope.showEmptyPlaceholder = function () {
      return !$scope.editMode && (!$scope.model || $scope.model.length === 0  ||
        !_.some($scope.model, function (item) { return !item.$hidden; }));
    };
  })

  .controller('TagCloudModalController', function ($scope, model, addLabel, Utils) {
    $scope.model = model;
    $scope.addLabel = addLabel;
    $scope.orderFn = Utils.nameSort;

    $scope.toggle = function (tag, $event) {
      $event.preventDefault();
      $event.stopPropagation();
      tag.$hidden = !tag.$hidden;
    };
  });

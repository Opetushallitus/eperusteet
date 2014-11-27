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
  .controller('TiedotteetController', function ($scope, Algoritmit, $modal, Varmistusdialogi) {
    // TODO tiedotteet bäkkäriltä
    $scope.tiedotteet = [];
    $scope.naytto = {limit: 5, shown: 5};

    $scope.paginate = {
      perPage: 10,
      current: 1
    };

    $scope.search = {
      term: '',
      changed: function () {
        $scope.paginate.current = 1;
      },
      filterFn: function (item) {
        return $scope.search.term ? Algoritmit.match($scope.search.term, item.otsikko) : true;
      }
    };

    $scope.orderFn = function (item) {
      return -1 * item.muokattu;
    };

    function doDelete(/*item*/) {
      // TODO delete
    }

    function doSave(item) {
      // TODO save
      $scope.tiedotteet.push(item);
    }

    $scope.delete = function (model) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-tiedote',
      })(function() {
        doDelete(model);
      });
    };

    $scope.edit = function (tiedote) {
      $modal.open({
        templateUrl: 'views/modals/tiedotteenmuokkaus.html',
        controller: 'TiedotteenMuokkausController',
        size: 'lg',
        resolve: {
          model: function () { return _.cloneDeep(tiedote); }
        }
      }).result.then(function (data) {
        if (data.$delete) {
          doDelete(data);
        } else {
          doSave(data);
        }
      });
    };
  })

  .controller('TiedotteenMuokkausController', function ($scope, model, Varmistusdialogi,
      $modalInstance, $rootScope) {
    $scope.model = model;
    $scope.creating = !model;
    if ($scope.creating) {
      $scope.model = {
        otsikko: {},
        teksti: {},
        id: null
      };
    }

    $scope.ok = function () {
      $rootScope.$broadcast('notifyCKEditor');
      $modalInstance.close($scope.model);
    };

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-tiedote',
      })(function() {
        $modalInstance.close(_.extend($scope.model, {$delete: true}));
      });
    };
  });

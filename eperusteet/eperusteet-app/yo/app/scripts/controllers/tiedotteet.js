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
  .factory('TiedotteetCRUD', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/tiedotteet/:tiedoteId', {
      tiedoteId: '@id'
    });
  })

  .controller('TiedotteetController', function ($scope, Algoritmit, $modal, Varmistusdialogi, TiedotteetCRUD,
    Notifikaatiot) {
    $scope.tiedotteet = [];
    $scope.naytto = {limit: 5, shown: 5};

    $scope.paginate = {
      perPage: 10,
      current: 1
    };

    function fetch() {
      TiedotteetCRUD.query({}, function (res) {
        $scope.tiedotteet = res;
      }, Notifikaatiot.serverCb);
    }
    fetch();

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

    function doDelete(item) {
      TiedotteetCRUD.delete({}, item, function () {
        Notifikaatiot.onnistui('poisto-onnistui');
        fetch();
      }, Notifikaatiot.serverCb);
    }

    function doSave(item) {
      TiedotteetCRUD.save({}, item, function () {
        Notifikaatiot.onnistui('tallennus-onnistui');
        fetch();
      }, Notifikaatiot.serverCb);
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
        if (data.$dodelete) {
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
        sisalto: {},
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
        $modalInstance.close(_.extend($scope.model, {$dodelete: true}));
      });
    };
  })

  .config(function($stateProvider) {
    $stateProvider
      .state('root.tiedote', {
        url: '/tiedote/:tiedoteId',
        templateUrl: 'views/tiedote.html',
        controller: 'TiedoteViewController'
      });
  })

  .controller('TiedoteViewController', function ($scope, $stateParams, TiedotteetCRUD, Notifikaatiot) {
    $scope.tiedote = null;
    TiedotteetCRUD.get({tiedoteId: $stateParams.tiedoteId}, function (res) {
      $scope.tiedote = res;
    }, Notifikaatiot.serverCb);
  });

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
  .service('TermistoService', function ($q) {
    // TODO backend
    var dummydata = [];
    var peruste = null;
    this.getAll = function () {
      var deferred = $q.defer();
      deferred.resolve(dummydata);
      return deferred.promise;
    };
    this.setPeruste = function (value) {
      peruste = value;
    };
  })

  .controller('TermistoController', function($scope, TermistoService, YleinenData, Algoritmit, Kaanna,
      $modal, Varmistusdialogi) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    TermistoService.setPeruste($scope.peruste);
    $scope.termisto = [];
    $scope.filtered = [];

    function sorter(item) {
      return Kaanna.kaanna(item.termi).toLowerCase();
    }

    $scope.search = {
      phrase: '',
      changed: function (value) {
        $scope.filtered = _($scope.termisto).filter(function (termi) {
          return !value ? true :
            Algoritmit.match(value, termi.termi) || Algoritmit.match(value, termi.selitys);
        }).sortBy(sorter).value();
        $scope.paginate.current = 1;
      }
    };

    $scope.paginate = {
      perPage: 7,
      current: 1
    };

    $scope.filterer = function (item) {
      return !item.$hidden;
    };

    function doDelete(item) {
      var index = _.findIndex($scope.termisto, {avain: item.avain});
      $scope.termisto.splice(index, 1);
    }

    $scope.delete = function (item) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-termi',
      })(function() {
        doDelete(item);
        $scope.search.changed($scope.search.phrase);
      });
    };

    $scope.edit = function (item) {
      $modal.open({
        templateUrl: 'views/modals/termisto.html',
        controller: 'TermistoMuokkausController',
        size: 'lg',
        resolve: {
          termimodel: function () {
            return _.cloneDeep(item);
          }
        }
      }).result.then(function (data) {
        // TODO save to backend / delete from backend
        if (data.$delete) {
          doDelete(data);
        } else if (data.$new) {
          delete data.$new;
          _.extend(data, {avain: (new Date()).getTime()});
          $scope.termisto.push(data);
        } else {
          var index = _.findIndex($scope.termisto, {avain: data.avain});
          _.each(['termi', 'selitys'], function (key) {
            $scope.termisto[index][key] = _.clone(data[key]);
          });
        }
        $scope.search.changed($scope.search.phrase);
      });
    };

    TermistoService.getAll().then(function (data) {
      $scope.termisto = data;
      $scope.filtered = _(data).sortBy(sorter).value();
    });
  })

  .controller('TermistoMuokkausController', function ($scope, termimodel, Varmistusdialogi, $modalInstance) {
    $scope.termimodel = termimodel;
    $scope.creating = !termimodel;
    if ($scope.creating) {
      $scope.termimodel = {
        termi: {},
        selitys: {},
        $new: true
      };
    }

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-termi',
      })(function() {
        $modalInstance.close(_.extend($scope.termimodel, {$delete: true}));
      });
    };
  });

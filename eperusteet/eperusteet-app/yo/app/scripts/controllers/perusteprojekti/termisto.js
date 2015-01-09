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
  .factory('TermistoCRUD', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/termisto/:id', {
      id: '@id',
      perusteId: '@perusteId'
    });
  })

  .service('TermistoService', function (TermistoCRUD, Notifikaatiot, $q, $timeout, YleinenData, $rootScope) {
    var peruste = null;
    var cached = {};
    var loading = false;
    this.newTermi = function (termi) {
      var newtermi = {
        termi: {},
        selitys: {},
        id: null
      };
      if (termi) {
        _.each(_.values(YleinenData.kielet), function (lang) {
          newtermi.termi[lang] = termi;
        });
      }
      return newtermi;
    };
    this.preload = function () {
      if (!cached[peruste.id] && !loading) {
        loading = true;
        var self = this;
        $timeout(function () {
          self.getAll().then(function () {
            loading = false;
          });
        });
      }
    };
    this.getAll = function () {
      return TermistoCRUD.query({perusteId: peruste.id}, function (res) {
        cached[peruste.id] = res;
      }).$promise;
    };
    this.delete = function (item) {
      return TermistoCRUD.delete({perusteId: peruste.id, id: item.id}, {}, function () {
        cached[peruste.id] = null;
        $rootScope.$broadcast('termisto:update');
      }, Notifikaatiot.serverCb).$promise;
    };
    function makeKey(item) {
      var termi = _.first(_.compact(_.values(item.termi))) || '';
      return termi.replace(/[^a-zA-Z0-9]/g, '') + (new Date()).getTime();
    }
    this.save = function (item) {
      if (!item.avain) {
        item.avain = makeKey(item);
      }
      return TermistoCRUD.save({perusteId: peruste.id}, item, function () {
        cached[peruste.id] = null;
        $rootScope.$broadcast('termisto:update');
      }, Notifikaatiot.serverCb).$promise;
    };
    this.setPeruste = function (value) {
      peruste = value;
    };
    function findTermi(avain) {
      return _.find(cached[peruste.id], function (item) {
        return item.avain === avain;
      });
    }
    this.getWithAvain = function (avain, cached) {
      if (cached) {
        return findTermi(avain);
      }
      var deferred = $q.defer();
      if (cached[peruste.id]) {
        deferred.resolve(findTermi(avain));
      } else {
        this.getAll().then(function () {
          deferred.resolve(findTermi(avain));
        });
      }
      return deferred.promise;
    };
  })

  .controller('TermistoController', function($scope, TermistoService, YleinenData, Algoritmit, Kaanna,
      $modal, Varmistusdialogi) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
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

    function refresh() {
      TermistoService.getAll().then(function (data) {
        $scope.termisto = data;
        $scope.filtered = _(data).sortBy(sorter).value();
      });
    }

    function doDelete(item) {
      TermistoService.delete(item).then(function () {
        $scope.search.phrase = '';
        refresh();
      });
    }

    function doSave(item) {
      TermistoService.save(item).then(function (res) {
        var index = _.findIndex($scope.termisto, {avain: res.avain});
        if (index >= 0) {
          $scope.termisto[index] = res;
        } else {
          $scope.termisto.push(res);
        }
        $scope.search.changed($scope.search.phrase);
      });
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
        if (data.$delete) {
          doDelete(data);
        } else {
          doSave(data);
        }
      });
    };

    refresh();
  })

  .controller('TermistoMuokkausController', function ($scope, termimodel, Varmistusdialogi,
      $modalInstance, $rootScope, TermistoService) {
    $scope.termimodel = termimodel;
    $scope.creating = !termimodel;
    if ($scope.creating) {
      $scope.termimodel = TermistoService.newTermi();
    }

    $scope.ok = function () {
      $rootScope.$broadcast('notifyCKEditor');
      $modalInstance.close($scope.termimodel);
    };

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-termi',
      })(function() {
        $modalInstance.close(_.extend($scope.termimodel, {$delete: true}));
      });
    };
  });

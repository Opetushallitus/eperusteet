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

/* global _ */
'use strict';

angular.module('eperusteApp')
  .service('VersionHelper', function(PerusteenOsat, $modal, RakenneVersiot, RakenneVersio, Notifikaatiot,
    PerusteenRakenne) {
    function latest(data) {
      return _.first(data) || {};
    }

    this.lastModified = function (data) {
      if (data && data.chosen) {
        var found = _.find(data.list, {number: data.chosen.number});
        if (found) {
          return found.date;
        }
      }
    };

    this.getPerusteenosaVersions = function (data, tunniste, force) {
      getVersions(data, tunniste, 'perusteenosa', force);
    };

    this.getRakenneVersions = function (data, tunniste, force) {
      getVersions(data, tunniste, 'rakenne', force);
    };

    var getVersions = function(data, tunniste, tyyppi, force) {
      if (!_.isObject(data)) {
        throw 'VersionHelper: not an object!';
      }
      if (force || !data.list) {
        if (tyyppi === 'perusteenosa') {
          PerusteenOsat.versiot({osanId: tunniste.id}, function(res) {
            data.list = res;
            versiotListHandler(data);
          });
        } else if (tyyppi === 'rakenne') {
          RakenneVersiot.query({perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa}, function(res) {
            data.list = res;
            versiotListHandler(data);
          });
        }
      }
    };

    var versiotListHandler = function(data) {
      if (!_.isEmpty(data.list)) { data.list.splice(_.size(data.list) - 1, 1); }
      data.chosen = latest(data.list);
      data.latest = true;
      _.each(data.list, function(item, index) {
        // reverse numbering for UI, oldest = 1
        item.index = data.list.length - index;
      });
    };

    this.chooseLatest = function (data) {
      data.chosen = latest(data.list);
    };

    this.changePerusteenosa = function(data, tunniste, cb) {
      change(data, tunniste, 'Perusteenosa', cb);
    };

    this.changeRakenne = function(data, tunniste, cb) {
      change(data, tunniste, 'Rakenne', cb);
    };

    var change = function(data, tunniste, tyyppi, cb) {
      if (tyyppi === 'Perusteenosa') {
        PerusteenOsat.getVersio({
          osanId: tunniste.id,
          versioId: data.chosen.number
        }, function(response) {
          changeResponseHandler(data, response, cb);
        });
      }
      else if (tyyppi === 'Rakenne') {
        RakenneVersio.get({perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa, versioId: data.chosen.number}, function(response) {
          changeResponseHandler(data, response, cb);
        });
      }
    };

    this.revertPerusteenosa = function (data, object, cb) {
      var isTekstikappale = _.has(object, 'nimi') && _.has(object, 'teksti');
      var type = isTekstikappale ? 'Perusteenosa' : 'Tutkinnonosa';
      revert(data, {id: object.id}, type, cb);
    };

    this.revertRakenne = function (data, tunniste, cb) {
      revert(data, tunniste, 'Rakenne', cb);
    };

    var revert = function (data, tunniste, tyyppi, cb) {
      // revert = get old (currently chosen) data, save as new version
      if (tyyppi === 'Perusteenosa' || tyyppi === 'Tutkinnonosa') {
        PerusteenOsat.getVersio({
          osanId: tunniste.id,
          versioId: data.chosen.number
        }, function(response) {
          if (tyyppi === 'Perusteenosa') {
            response.$saveTekstikappale(cb, Notifikaatiot.serverCb);
          } else {
            response.$saveTutkinnonOsa(cb, Notifikaatiot.serverCb);
          }
        });
      } else if (tyyppi === 'Rakenne') {
        RakenneVersio.get({
          perusteId: tunniste.id,
          suoritustapa: tunniste.suoritustapa,
          versioId: data.chosen.number
        }, function(response) {
          PerusteenRakenne.tallennaRakenne({
            rakenne: response
          }, tunniste.id, tunniste.suoritustapa, cb, Notifikaatiot.serverCb);
        });
      }
    };

    var changeResponseHandler = function(data, response, cb) {
      cb(response);
      data.latest = data.chosen.number === latest(data.list).number;
    };

    this.historyView = function (data) {
      $modal.open({
        template: '<div class="modal-header"><h1>Versiohistoria</h1></div>' +
                '<div class="modal-body">' +
                '<table class="table table-striped">' +
                '<tr><th>{{\'versio\'|kaanna}}</th><th>{{\'muokattu-viimeksi\'|kaanna}}</th><th>{{\'muokkaaja\'|kaanna}}</th></tr>' +
                '<tr ng-repeat="ver in versions.list"><td>{{ver.index}}</td><td>{{ver.date|aikaleima}}</td><td>Erkki Esimerkki</td></tr>' +
                '</div></table>' +
                '<div class="modal-footer"><button class="btn" ng-click="close()">{{\'sulje\' | kaanna }}</button></div>',
        resolve: {
          versions: function() {
            return data;
          }
        },
        controller: 'HistoryViewCtrl'
      });
    };
  })
  .controller('HistoryViewCtrl', function ($scope, versions, $modalInstance) {
    $scope.versions = versions;
    $scope.close = function () {
      $modalInstance.close();
    };
  });

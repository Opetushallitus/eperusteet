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
  .service('VersionHelper', function(PerusteenOsat, $modal, RakenneVersiot,
    RakenneVersio, Notifikaatiot, $state, $location, $stateParams) {

    function rakennaNimi(v) {
        var nimi = (v.kutsumanimi || '') + ' ' + (v.sukunimi || '');
        v.$nimi = nimi === ' ' ? v.muokkaajaOid : nimi;
    }

    function rakennaNimet(list) {
      _.forEach(list, rakennaNimi);
    }

    function getVersions(data, tunniste, tyyppi, force, cb) {
      cb = cb || angular.noop;
      if (!_.isObject(data)) {
        throw 'VersionHelper: not an object!';
      }
      if (force || !data.list) {
        if (tyyppi === 'perusteenosa') {
          PerusteenOsat.versiot({osanId: tunniste.id}, function(res) {
            rakennaNimet(res);
            data.list = res;
            versiotListHandler(data);
            cb();
          });
        }
        else if (tyyppi === 'rakenne') {
          RakenneVersiot.query({perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa}, function(res) {
            rakennaNimet(res);
            data.list = res;
            versiotListHandler(data);
            cb();
          });
        }
      }
    }

    function versiotListHandler(data) {
      data.chosen = latest(data.list);
      data.latest = true;
      _.each(data.list, function(item, index) {
        // reverse numbering for UI, oldest = 1
        item.index = data.list.length - index;
      });
    }

    function latest(data) {
      return _.first(data) || {};
    }

    function revert(data, tunniste, tyyppi, cb) {
      // revert = get old (currently chosen) data, save as new version
      if (tyyppi === 'Perusteenosa' || tyyppi === 'Tutkinnonosa') {
        PerusteenOsat.palauta({
          osanId: tunniste.id,
          versioId: data.chosen.numero
        }, {}, cb, Notifikaatiot.serverCb);
      }
      else if (tyyppi === 'Rakenne') {
        RakenneVersio.palauta({
          perusteId: tunniste.id,
          suoritustapa: tunniste.suoritustapa,
          versioId: data.chosen.numero
        }, {}, cb, Notifikaatiot.serverCb);
      }
    }

    function change(data, tunniste, tyyppi, cb) {
      if (tyyppi === 'Perusteenosa') {
        PerusteenOsat.getVersio({
          osanId: tunniste.id,
          versioId: data.chosen.numero
        }, function(response) {
          changeResponseHandler(data, response, cb);
        });
      }
      else if (tyyppi === 'Rakenne') {
        RakenneVersio.get({perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa, versioId: data.chosen.numero}, function(response) {
          changeResponseHandler(data, response, cb);
        });
      }
    }

    function changeResponseHandler(data, response, cb) {
      cb(response);
      data.latest = data.chosen.numero === latest(data.list).numero;
    }

    this.lastModified = function (data) {
      if (data && data.chosen) {
        var found = _.find(data.list, {numero: data.chosen.numero});
        if (found) {
          return found.pvm;
        }
      }
    };

    this.select = function (data, index) {
      var found = _.find(data.list, {index: parseInt(index, 10)});
      if (found) {
        data.chosen = found;
        data.latest = data.chosen.numero === latest(data.list).numero;
        return found.numero;
      }
    };

    this.currentIndex = function (data) {
      if (data && data.chosen) {
        return data.chosen.index;
      }
    };

    this.latestIndex = function (data) {
      var latestItem = latest(data.list);
      if (latestItem) {
        return latestItem.index;
      }
    };

    this.getPerusteenosaVersions = function (data, tunniste, force, cb) {
      getVersions(data, tunniste, 'perusteenosa', force, cb);
    };

    this.getRakenneVersions = function (data, tunniste, force, cb) {
      getVersions(data, tunniste, 'rakenne', force, cb);
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

    this.revertPerusteenosa = function (data, object, cb) {
      var isTekstikappale = _.has(object, 'nimi') && _.has(object, 'teksti');
      var type = isTekstikappale ? 'Perusteenosa' : 'Tutkinnonosa';
      revert(data, {id: object.id}, type, cb);
    };

    this.revertRakenne = function (data, tunniste, cb) {
      revert(data, tunniste, 'Rakenne', cb);
    };

    this.setUrl = function (data, isRakenne) {
      // Tricks for ui-router 0.2.*
      // We want to update the url only when user changes the version.
      // If we enter with versionless url don't rewrite it.
      // This function will currently navigate to a new state if version has changed.
      if (_.isEmpty(data)) {
        return;
      }
      data.latest = data.chosen.index === latest(data.list).index;
      var state = isRakenne ? 'root.perusteprojekti.suoritustapa.muodostumissaannot' : 'root.perusteprojekti.suoritustapa.perusteenosa';
      var versionlessUrl = $state.href(state, {versio: null}, {inherit:true}).replace(/#/g, '');
      var currentVersion = this.currentIndex(data);
      var isValid = _.isNumber(currentVersion);
      var urlHasVersion = $location.url() !== versionlessUrl;
      if ((urlHasVersion || data.hasChanged) && isValid && !data.latest) {
        data.hasChanged = false;
        var versionUrl = $state.href(state, {versio: '/' + currentVersion}, {inherit:true}).replace(/#/g, '');
        $location.url(versionUrl);
      } else {
        $location.url(versionlessUrl);
      }
    };

    this.historyView = function(data) {
      $modal.open({
        templateUrl: 'views/partials/muokkaus/versiohelper.html',
        controller: 'HistoryViewCtrl',
        resolve: {
          versions: function() {
            return data;
          }
        }
      })
      .result.then(function(re) {
        var params = _.clone($stateParams);
        params.versio = '/' + re.index;
        $state.go($state.current.name, params);
      });
    };
  })
  .controller('HistoryViewCtrl', function ($scope, versions, $modalInstance) {
    $scope.versions = versions;
    $scope.close = function(versio) {
      if (versio) {
        $modalInstance.close(versio);
      }
      else {
        $modalInstance.dismiss();
      }
    };
  });

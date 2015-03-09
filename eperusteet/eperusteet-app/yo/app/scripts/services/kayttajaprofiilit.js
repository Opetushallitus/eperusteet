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
  .factory('Kayttajatiedot', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/kayttajatiedot/:oid', {
      oid: '@oid'
    });
  })
  .factory('Kayttajaprofiilit', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/kayttajaprofiili/:id', {
      id: '@id'
    }, {
      lisaaPreferenssi: {method: 'POST', url: SERVICE_LOC + '/kayttajaprofiili/preferenssi'}
    });
  })
  .service('Profiili', function($state, $rootScope, Suosikit, Notifikaatiot, Kayttajaprofiilit, $stateParams, $http, $q) {
    var info = {
      resolved: false,
      suosikit: [],
      preferenssit: {}
    };

    var infoQ = $q.defer();
    info.$resolved = infoQ.promise;
    info.$resolved.then(function(value) {
      info.resolved = value;
    });

    function isSame(paramsA, paramsB) {
      return _.size(paramsA) === _.size(paramsB) && _.all(paramsA, function(v, k) {
        return paramsB[k] === v;
      });
    }

    function transformSuosikit(uudetSuosikit) {
      return _.map(uudetSuosikit, function(s) {
        s.sisalto = JSON.parse(s.sisalto);
        if (s.sisalto.tyyppi === 'linkki') {
          s.$url = $state.href(s.sisalto.tila, s.sisalto.parametrit);
        }
        return s;
      });
    }

    function transformPreferenssit(preferenssit) {
      return _.zipObject(_.map(preferenssit, 'avain'), _.map(preferenssit, 'arvo'));
    }

    function parseResponse(res, cb) {
      info.suosikit = transformSuosikit(res.suosikit);
      info.preferenssit = transformPreferenssit(res.preferenssit);
      (cb || angular.noop)();
      $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
    }

    Kayttajaprofiilit.get({}, function(res) {
      _.extend(info, res);
      info.suosikit = transformSuosikit(res.suosikit);
      info.preferenssit = transformPreferenssit(res.preferenssit);
      infoQ.resolve(true);
      console.log('kayttajaProfiiliPaivittyi');
      $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
    }, function() {
      infoQ.resolve(false);
    });

    return {
      // Perustiedot
      oid: function() { return info.oid; },
      lang: function() { return info.lang; },
      groups: function() { return info.groups; },
      profiili: function() { return info; },
      resolvedPromise: function() { return info.$resolved; },
      isResolved: function() { return info.resolved; },
      casTiedot: function() {
        // TODO Käyttäjätiedot voisi hakea ensisijaisesti CAS:sta eikä fallbackina
        // käyttäjäprofiilille. CAS:ssa on kuitenkin aina kirjaantuneen käyttäjän tiedot.
        var deferred = $q.defer();
        if (!info.$casFetched) {
          info.$casFetched = true;
          $http.get('/cas/me').success(function(res) {
            if (res.oid) {
              info.oid = res.oid;
              info.lang = res.lang;
              info.groups = res.groups;
            }
            deferred.resolve(res);
            $rootScope.$broadcast('fetched:casTiedot');
          }).error(function() {
            deferred.resolve({});
            $rootScope.$broadcast('fetched:casTiedot');
          });
        } else {
          deferred.resolve(info);
        }
        return deferred.promise;
      },
      setPreferenssi: function(avain, arvo, successCb, failureCb) {
        successCb = successCb || angular.noop;
        failureCb = failureCb || angular.noop;

        if (arvo !== info.preferenssit[avain]) {
          Kayttajaprofiilit.lisaaPreferenssi({
            avain: avain,
            arvo: arvo
          }, function() {
            info.preferenssit[avain] = arvo;
            $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
            successCb();
          }, function(err) {
            failureCb();
            Notifikaatiot.serverCb(err);
          });
        }
      },
      // Suosikit
      asetaSuosikki: function(state, nimi, success, customParams) {
        var stateParams = customParams || $stateParams;
        success = success || angular.noop;
        state = _.isObject(state) ? state.current.name : state;

        var vanha = _(info.suosikit).filter(function(s) {
          return state === s.sisalto.tila && isSame(stateParams, s.sisalto.parametrit);
        }).first();

        if (!_.isEmpty(vanha)) {
          _.remove(info.suosikit, vanha);
          Suosikit.delete({suosikkiId: vanha.id}, function() {
            success(_.clone(info.suosikit));
            $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
          }, Notifikaatiot.serverCb);
        }
        else {
          Suosikit.save({
            sisalto: JSON.stringify({
              tyyppi: 'linkki',
              tila: state,
              parametrit: stateParams,
            }),
            nimi: nimi
          }, function(res) {
            parseResponse(res, success);
          }, Notifikaatiot.serverCb);
        }
      },
      listaaSuosikit: function() {
        return _.clone(info.suosikit);
      },
      haeSuosikki: function(state) {
        var stateParams = $stateParams;
        var haku = _.filter(info.suosikit, function(s) {
          return state.current.name === s.sisalto.tila && isSame(stateParams, s.sisalto.parametrit);
        });
        return _.first(haku);
      },
      haeUrl: function(id) {
        return $state.href(info.suosikit[id].sisalto.tila, info.suosikit[id].sisalto.parametrit);
      },
      paivitaSuosikki: function(suosikki) {
        var payload = _.clone(suosikki);
        payload.sisalto = JSON.stringify(payload.sisalto);
        return Suosikit.update({suosikkiId: payload.id}, payload).$promise.then(function(res) {
          parseResponse(res);
        });
      },
      poistaSuosikki: function(suosikki) {
        return Suosikit.delete({suosikkiId: suosikki.id}).$promise.then(function(res) {
          parseResponse(res);
        });
      }
    };
  });

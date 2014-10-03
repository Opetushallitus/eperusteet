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
      lisaaPreferenssi: { method: 'POST', url: SERVICE_LOC + '/kayttajaprofiili/preferenssi' }
    });
  })
  .service('Profiili', function($state, $rootScope, Suosikit, Notifikaatiot, Kayttajaprofiilit) {
    var info = {
      resolved: false,
      suosikit: [],
      preferenssit: {}
    };

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
      info = res;
      info.oid = res.oid;
      info.suosikit = transformSuosikit(res.suosikit);
      info.preferenssit = transformPreferenssit(res.preferenssit);
      info.resolved = true;
      $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
    });

    return {
      // Perustiedot
      oid: function() { return info.oid; },
      profiili: function() { return info; },
      isResolved: function() { return info.resolved; },

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
      asetaSuosikki: function(state, stateParams, nimi, success) {
        success = success || angular.noop;

        var vanha = _(info.suosikit).filter(function(s) {
          return state.current.name === s.sisalto.tila && isSame(stateParams, s.sisalto.parametrit);
        })
        .first();

        if (!_.isEmpty(vanha)) {
          _.remove(info.suosikit, vanha);
          Suosikit.delete({ suosikkiId: vanha.id }, function() {
            success(_.clone(info.suosikit));
            $rootScope.$broadcast('kayttajaProfiiliPaivittyi');
          }, Notifikaatiot.serverCb);
        }
        else {
          Suosikit.save({
            sisalto: JSON.stringify({
              tyyppi: 'linkki',
              tila: state.current.name,
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
      haeSuosikki: function(state, stateParams) {
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
        return Suosikit.update({suosikkiId: payload.id}, payload).$promise.then(function (res) {
          parseResponse(res);
        });
      },
      poistaSuosikki: function(suosikki) {
        return Suosikit.delete({suosikkiId: suosikki.id}).$promise.then(function (res) {
          parseResponse(res);
        });
      }
    };
  });

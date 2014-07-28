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
  .factory('Suosikit', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/kayttajaprofiili/suosikki/:suosikkiId', {});
  })
  .service('SuosikkiTemp', function($state, $rootScope, Suosikit, Notifikaatiot, Kayttajaprofiilit) {
    var suosikit = [];

    function isSame(paramsA, paramsB) {
      return _.size(paramsA) === _.size(paramsB) && _.all(paramsA, function(v, k) {
        return paramsB[k] === v;
      });
    }

    function transform(uudetSuosikit) {
      return _.map(uudetSuosikit, function(s) {
        s.parametrit = JSON.parse(s.parametrit);
        s.$url = $state.href(s.tila, s.parametrit);
        return s;
      });
    }

    Kayttajaprofiilit.get({}, function(res) {
      suosikit = transform(res.suosikit);
      $rootScope.$broadcast('suosikitMuuttuivat');
    });

    return {
      aseta: function(state, stateParams, nimi, success) {
        success = success || angular.noop;

        var vanha = _(suosikit).filter(function(s) {
          return state.current.name === s.tila && isSame(stateParams, s.parametrit);
        })
        .first();

        if (!_.isEmpty(vanha)) {
          _.remove(suosikit, vanha);
          Suosikit.delete({ suosikkiId: vanha.id }, function() {
            success(_.clone(suosikit));
            $rootScope.$broadcast('suosikitMuuttuivat');
          }, Notifikaatiot.serverCb);
        }
        else {
          Suosikit.save({
            tila: state.current.name,
            parametrit: JSON.stringify(stateParams),
            nimi: nimi
          }, function(res) {
            suosikit = transform(res.suosikit);
            success();
            $rootScope.$broadcast('suosikitMuuttuivat');
          }, Notifikaatiot.serverCb);
        }
      },
      listaa: function() {
        return _.clone(suosikit);
      },
      hae: function(state, stateParams) {
        var haku = _.filter(suosikit, function(s) {
          return state.current.name === s.tila && isSame(stateParams, s.parametrit);
        });
        return _.first(haku);
      },
      haeUrl: function(id) {
        return $state.href(suosikit[id].tila, suosikit[id].parametrit);
      }
    };
  });

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
  .service('SuosikkiTemp', function($state, $rootScope, Suosikit, Notifikaatiot) {
    var suosikit = [];

    function isSame(paramsA, paramsB) {
      return _.size(paramsA) === _.size(paramsB) && _.all(paramsA, function(v, k) {
        return paramsB[k] === v;
      });
    }

    return {
      aseta: function(state, stateParams, nimi) {
        var vanha = _(suosikit).filter(function(s) {
          return state.current.name === s.tila && isSame(stateParams, s.parametrit);
        }).first();

        var re;
        if (!_.isEmpty(vanha)) {
          _.remove(suosikit, vanha);
        }
        else {
          var uusi = {
            tila: state.current.name,
            parametrit: stateParams,
            nimi: nimi
          };

          Suosikit.add(uusi, function(res) {
            res.$url = $state.href(state.current.name, stateParams);
            suosikit.push(res);
            re = res;
          }, Notifikaatiot.serverCb);
        }

        $rootScope.$broadcast('suosikitMuuttuivat', _.clone(suosikit));
        return re;
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

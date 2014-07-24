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
  .service('Algoritmit', function(Kaanna) {
    function rajausVertailu(input, kentta) {
      kentta = arguments.length > 2 ? kentta[arguments[2]] : kentta;
      for (var i = 3; i < arguments.length; ++i) {
        if (!kentta) { return undefined; }
        kentta = kentta[arguments[i]];
      }

      var kaannetty = Kaanna.kaanna(kentta);
      kaannetty = _.isString(kaannetty) ? kaannetty : '';
      return kaannetty.toLowerCase().indexOf(input.toLowerCase()) !== -1;
    }

    function mapLapsisolmut(objekti, lapsienAvain, cb) {
      return _.map(objekti[lapsienAvain], function(solmu) {
        solmu[lapsienAvain] = kaikilleLapsisolmuille(solmu, lapsienAvain, cb);
        return cb(solmu);
      });
    }

    function kaikilleLapsisolmuille(objekti, lapsienAvain, cb) {
      _.forEach(objekti[lapsienAvain], function(solmu) {
        kaikilleLapsisolmuille(solmu, lapsienAvain, cb);
        cb(solmu);
      });
    }

    function asyncTraverse(list, cb, done) {
      done = done || angular.noop;
      list = list || [];
      if (_.isEmpty(list)) { done(); return; }
      cb(_.first(list), function() { asyncTraverse(_.rest(list), cb, done); });
    }

    function match(input, to) {
      return Kaanna.kaanna(to).toLowerCase().indexOf(input.toLowerCase()) !== -1;
    }

    function access(object) {
      if (object && _.isObject(object) && arguments.length > 1) {
        return access.apply([object[arguments[1]].concat(arguments.splice(2))]);
      }
      return object;
    }

    function perusteenSuoritustavanYksikko(peruste, suoritustapa) {
      var st = _.first(_.filter(peruste.suoritustavat, function(st) { return st.suoritustapakoodi === suoritustapa; }));
      return st.laajuusYksikko;
    }

    return {
      rajausVertailu: rajausVertailu,
      mapLapsisolmut: mapLapsisolmut,
      kaikilleLapsisolmuille: kaikilleLapsisolmuille,
      asyncTraverse: asyncTraverse,
      match: match,
      access: access,
      perusteenSuoritustavanYksikko: perusteenSuoritustavanYksikko
    };
  });

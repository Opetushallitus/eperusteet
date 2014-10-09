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
      return match(input, kentta);
    }

    function mapLapsisolmut(objekti, lapsienAvain, cb) {
      return _.map(_.isArray(objekti) ? objekti : objekti[lapsienAvain], function(solmu) {
        solmu = _.clone(solmu);
        solmu[lapsienAvain] = mapLapsisolmut(solmu, lapsienAvain, cb);
        return cb(solmu);
      });
    }

    function kaikilleLapsisolmuille(objekti, lapsienAvain, cb, depth) {
      depth = depth || 0;
      _.forEach(objekti[lapsienAvain], function(solmu) {
        if (!cb(solmu, depth)) {
          kaikilleLapsisolmuille(solmu, lapsienAvain, cb, depth + 1);
        }
      });
    }

    function asyncTraverse(list, cb, done) {
      done = done || angular.noop;
      list = list || [];
      if (_.isEmpty(list)) { done(); return; }
      cb(_.first(list), function() { asyncTraverse(_.rest(list), cb, done); });
    }

    function match(input, to) {
      var vertailu = Kaanna.kaanna(to) || '';
      return vertailu.toLowerCase().indexOf(input.toLowerCase()) !== -1;
    }

    function access(object) {
      if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
          object = object && _.isPlainObject(object) ? object[arguments[i]] : undefined;
        }
      }
      return object;
    }

    function perusteenSuoritustavanYksikko(peruste, suoritustapa) {
      var st = _.first(_.filter(peruste.suoritustavat, function(st) { return st.suoritustapakoodi === suoritustapa; }));
      return st.laajuusYksikko;
    }

    function kaikilleTutkintokohtaisilleOsille(juuri, cb) {
      var lapsellaOn = false;
      _.forEach(juuri.lapset, function(osa) {
        lapsellaOn = kaikilleTutkintokohtaisilleOsille(osa, cb) || lapsellaOn;
      });
      return cb(juuri, lapsellaOn) || lapsellaOn;
    }

    return {
      rajausVertailu: rajausVertailu,
      mapLapsisolmut: mapLapsisolmut,
      kaikilleLapsisolmuille: kaikilleLapsisolmuille,
      asyncTraverse: asyncTraverse,
      match: match,
      access: access,
      perusteenSuoritustavanYksikko: perusteenSuoritustavanYksikko,
      kaikilleTutkintokohtaisilleOsille: kaikilleTutkintokohtaisilleOsille
    };
  });


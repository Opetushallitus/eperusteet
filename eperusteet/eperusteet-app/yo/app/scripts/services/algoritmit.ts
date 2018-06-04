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

import _ from "lodash";
import * as angular from "angular";

angular.module("eperusteApp").service("Algoritmit", Kaanna => {
    function rajausVertailu(input, kentta) {
        kentta = arguments.length > 2 ? kentta[arguments[2]] : kentta;
        for (var i = 3; i < arguments.length; ++i) {
            if (!kentta) {
                return undefined;
            }
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
        if (!_.isEmpty(objekti)) {
            _.forEach(objekti[lapsienAvain], function(solmu) {
                if (!cb(solmu, depth)) {
                    kaikilleLapsisolmuille(solmu, lapsienAvain, cb, depth + 1);
                }
            });
        }
    }

    function asyncTraverse(list, cb, done) {
        done = done || angular.noop;
        list = list || [];
        if (_.isEmpty(list)) {
            done();
            return;
        }
        cb(_.first(list), function() {
            asyncTraverse(_.rest(list), cb, done);
        });
    }

    function match(input, to, kaanna = true) {
        var vertailu = kaanna ? Kaanna.kaanna(to) || "" : to;
        return _.isString(vertailu) && _.isString(input) && vertailu.toLowerCase().indexOf(input.toLowerCase()) !== -1;
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
        var foundSt = _.find(peruste.suoritustavat, function(st: any) {
            return st.suoritustapakoodi === suoritustapa;
        });
        return foundSt ? foundSt.laajuusYksikko : "OSAAMISPISTE";
    }

    function kaikilleTutkintokohtaisilleOsille(juuri, cb) {
        var lapsellaOn = false;
        _.forEach(juuri.lapset, function(osa) {
            lapsellaOn = kaikilleTutkintokohtaisilleOsille(osa, cb) || lapsellaOn;
        });
        return cb(juuri, lapsellaOn) || lapsellaOn;
    }

    function normalizeTeksti(teksti) {
        function poistaTurhat(t) {
            t = t || "";
            var txt = document.createElement("textarea");
            txt.innerHTML = t;
            t = txt.value;
            t = t.replace(/[\u00A0|\u0000-\u001F]/g, " ");

            var last;
            do {
                last = t;
                t = last.replace(/  /g, " ");
            } while (_.size(last) !== _.size(t));
            return t.trim();
        }

        if (_.isString(teksti)) {
            return poistaTurhat(teksti);
        } else if (_.isPlainObject(teksti)) {
            return _.zipObject(_.keys(teksti), _.map(_.values(teksti), poistaTurhat));
        } else {
            return teksti;
        }
    }

    function removeFieldsRecursiveFromObject(obj, pattern: RegExp, depth = 0) {
        if (_.isObject(obj)) {
            _.each(obj, (value, key) => {
                removeFieldsRecursiveFromObject(obj[key], pattern, depth + 1);

                if (pattern instanceof RegExp && pattern.test(key)) {
                    delete obj[key];
                }
            });
        }
    }

    return {
        normalizeTeksti,
        rajausVertailu,
        mapLapsisolmut,
        kaikilleLapsisolmuille,
        asyncTraverse,
        match,
        access,
        perusteenSuoritustavanYksikko,
        kaikilleTutkintokohtaisilleOsille,
        removeFieldsRecursiveFromObject
    };
});

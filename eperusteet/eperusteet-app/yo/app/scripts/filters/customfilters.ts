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

"use strict";
/* global _, moment */

angular
    .module("eperusteApp")
    .filter("mapFilter", function() {
        return function(input, f) {
            input = _.filter(input, function(v) {
                var bool = f(v);
                return bool;
            });
            return input;
        };
    })
    .filter("koodisto", function(Kaanna) {
        return function(koodi) {
            if (!koodi || _.isEmpty(koodi)) {
                return Kaanna.kaanna("ei-asetettu");
            } else {
                var indeksi = koodi.indexOf("_");
                koodi = indeksi !== -1 ? koodi.substr(indeksi + 1) : koodi;
                return koodi;
            }
        };
    })
    .service("AikaleimaCache", function($rootScope, CacheFactory) {
        var cache = null;
        this.create = function() {
            cache = CacheFactory.createCache("momentCache", {
                capacity: 1024,
                maxAge: 60000,
                deleteOnExpire: "aggressive"
            });
        };
        this.get = function(key) {
            return cache.get(key);
        };
        this.put = function(key, value) {
            return cache.put(key, value);
        };
        $rootScope.$on("changed:uikieli", function() {
            cache && cache.removeAll();
        });
    })
    /**
   * Muotoilee timestampit
   * default: time (ago)
   * parametrit:
   * 'time' pelkkä päiväys ja kellonaika
   * 'date' pelkkä päiväys
   * 'ago' pelkkä ihmisluettava esim. '4 tuntia sitten'
   */
    .filter("aikaleima", function($filter, AikaleimaCache) {
        var dateFilter = $filter("date");
        AikaleimaCache.create();
        return function(input, options, format) {
            var date = null;
            if (!input) {
                return "";
            }
            if (format === "date") {
                date = dateFilter(input, "d.M.yyyy");
            } else {
                date = dateFilter(input, "d.M.yyyy H:mm");
            }

            var ago = AikaleimaCache.get(input);
            if (!ago) {
                ago = moment(input).fromNow();
                AikaleimaCache.put(input, ago);
            }

            if (options === "ago") {
                return ago;
            } else if (options === "time") {
                return date;
            }
            return date + " (" + ago + ")";
        };
    })
    .filter("tyhja", function(Kaanna) {
        return function(input) {
            return _.isEmpty(input) ? Kaanna.kaanna("ei-asetettu") : input;
        };
    })
    .filter("unsafe", [
        "$sce",
        function($sce) {
            return function(val) {
                return $sce.trustAsHtml(val);
            };
        }
    ])
    .filter("paragraphsplit", function() {
        return function(text) {
            return text.split("\n");
        };
    });

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

import * as _ from "lodash";
import * as angular from "angular";

const fi = require("../../localisation/locale-fi.json");
const sv = require("../../localisation/locale-fi.json");
const en = require("../../localisation/locale-fi.json");

const Locales = {
    fi,
    sv,
    en
};

angular
    .module("eperusteApp")
    .factory("LokalisointiResource", (LOKALISOINTI_SERVICE_LOC, $resource) => {
        return $resource(
            "/lokalisointi/cxf/rest/v1/localisation?category=eperusteet",
            {},
            {
                get: {
                    method: "GET",
                    isArray: true,
                    cache: true
                }
            }
        );
    })
    .factory("LokalisointiLoader", ($q, $http, LokalisointiResource, $window) => {
        const PREFIX = "localisation/locale-",
            SUFFIX = ".json",
            BYPASS_REMOTE = !$window.location.host || $window.location.host.indexOf("localhost") === 0;

        return async options =>
            new Promise((resolve, reject) => {
                const translations = {};
                const langs = Locales[options.key];

                try {
                    _.extend(translations, langs);
                    if (BYPASS_REMOTE) {
                        return resolve(translations);
                    } else {
                        LokalisointiResource.get(
                            { locale: options.key },
                            (res: any) => {
                                const remotes = _.zipObject(_.map(res, "key"), _.map(res, "value"));
                                _.extend(translations, remotes);
                                resolve(translations);
                            },
                            () => {
                                reject(options.key);
                            }
                        );
                    }
                } catch (err) {
                    console.error(err);
                    throw "Käännösten haku epäonnistui: " + options.key;
                }
            });
    });

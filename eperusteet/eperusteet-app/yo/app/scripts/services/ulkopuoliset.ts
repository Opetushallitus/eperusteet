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

import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .factory("KoodistoHaku", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/koodisto/:koodistoUri",
            {
                koodistoUri: "@koodistoUri"
            },
            {
                get: { method: "GET", isArray: true }
            }
        );
    })
    .factory("Organisaatioryhmat", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/ulkopuoliset/organisaatioryhmat/",
            {
                oid: "@oid"
            },
            {
                get: { method: "GET", isArray: true },
                yksi: { method: "GET", isArray: false, url: SERVICE_LOC + "/ulkopuoliset/organisaatioryhmat/:oid" }
            }
        );
    });

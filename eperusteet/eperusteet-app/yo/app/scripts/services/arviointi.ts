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

angular.module("eperusteApp")
    .factory("Arviointiasteikot", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/arviointiasteikot/:asteikkoId",
            {
                asteikkoId: "@id"
            },
            {
                list: { method: "GET", isArray: true, cache: true },
                save: { method: "PUT", isArray: true }
            }
        );
    })
    .service("ArviointiasteikkoHelper", (Api) => {
        return {
            async getMappedArviointiasteikot() {
                const asteikot = await Api.all("arviointiasteikot").getList();
                return _(asteikot)
                    .sortBy(aa => _.size(aa.osaamistasot))
                    .map(aa => ({
                        ...aa,
                        osaamistasotMap: _.indexBy(aa.osaamistasot, (taso: any) => String(taso.id)),
                    }))
                    .reverse()
                    .indexBy(asteikko => String(asteikko.id))
                    .value();

            },
        };
    });

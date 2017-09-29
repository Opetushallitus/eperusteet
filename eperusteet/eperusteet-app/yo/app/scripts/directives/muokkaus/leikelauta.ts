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

angular.module("eperusteApp").directive("leikelauta", TutkinnonOsaLeikelautaService => {
    return {
        templateUrl: "views/partials/muokkaus/leikelauta.html",
        restrict: "E",
        transclude: true,
        scope: {
            isOpen: "="
        },
        link: function(scope, element, attrs) {
            scope["leikelautaSortableOptions"] = TutkinnonOsaLeikelautaService.createLeikelautaSortable(scope, {
                handle: ".handle",
                connectWith:
                    ".container-items, .container-items-arviointi, .container-items-kohteet," +
                    " .container-items-leikelauta, .container-items-ammattitaito, .container-items-vaatimuksenKohteet",
                cursor: "move",
                cursorAt: { top: 10, left: 10 },
                tolerance: "pointer",
                forceHelperSize: true,
                placeholder: "sortable-placeholder",
                forcePlaceholderSize: true,
                opacity: ".7"
            });
            scope["poistaLeikelaudasta"] = TutkinnonOsaLeikelautaService.poistaLeikelaudasta;
            scope["leikelauta"] = TutkinnonOsaLeikelautaService.initLeikelauta();
        }
    };
});

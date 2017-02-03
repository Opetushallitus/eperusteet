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

angular.module("app")
.factory("Api", Restangular => Restangular.withConfig(config => {
    config.setBaseUrl("/eperusteet-service/api");

    // config.addResponseInterceptor((data, operation, what, url, response, deferred) => {
    //     if (response && response.status >= 400) {
    //         if (response.status >= 500) {
    //             // fataali(KaannaService.kaanna("jarjestelmavirhe-teksti", {
    //             //     virhekoodi: response.status
    //             // }), () => {
    //             //     // TODO Ota käyttöön myöhemmin
    //             //     // $state.go("root.virhe");
    //             // });
    //         }
    //         else if (response.data && response.data.syy) {
    //             let syy = response.data.syy;
    //             NotifikaatioService.varoitus(_.isArray(syy) ? syy[0] : syy);
    //         }
    //         else {
    //             NotifikaatioService.varoitus(KaannaService.kaanna("odottamaton-virhe"));
    //         }
    //     }
    //     return data;
    // });
}));

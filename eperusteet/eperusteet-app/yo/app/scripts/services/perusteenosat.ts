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
/*global _*/

angular
    .module("eperusteApp")
    .factory("PerusteenOsat", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteenosat/:osanId",
            {
                osanId: "@id"
            },
            {
                byKoodiUri: { method: "GET", isArray: true, params: { koodi: true } },
                saveTekstikappale: { method: "POST" },
                saveTutkinnonOsa: { method: "POST" },
                versiot: { method: "GET", isArray: true, url: SERVICE_LOC + "/perusteenosat/:osanId/versiot" },
                getVersio: { method: "GET", url: SERVICE_LOC + "/perusteenosat/:osanId/versio/:versioId" },
                palauta: { method: "POST", url: SERVICE_LOC + "/perusteenosat/:osanId/palauta/:versioId" },
                getByViite: { method: "GET", url: SERVICE_LOC + "/perusteenosat/viite/:viiteId" },
                versiotByViite: {
                    method: "GET",
                    isArray: true,
                    url: SERVICE_LOC + "/perusteenosat/viite/:viiteId/versiot"
                },
                getVersioByViite: { method: "GET", url: SERVICE_LOC + "/perusteenosat/viite/:viiteId/versio/:versioId" }
            }
        );
    })
    .factory("PerusteenOsaViitteet", function($resource, SERVICE_LOC, $stateParams, PerusteprojektiTiedotService) {
        var baseUrl = SERVICE_LOC + "/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto/:viiteId";
        //FIXME
        var pleaseFixMe;
        PerusteprojektiTiedotService.then(function(value) {
            pleaseFixMe = value;
        });
        return $resource(
            baseUrl,
            {
                viiteId: "@viiteId",
                perusteId: function() {
                    //FIXME (parempi tapa perusteen id:n hakemiseen)
                    return pleaseFixMe.getPeruste().id;
                },
                suoritustapa: function() {
                    return $stateParams.suoritustapa;
                }
            },
            {
                kloonaaTekstikappale: {
                    method: "POST",
                    url: baseUrl + "/muokattavakopio"
                },
                kloonaaTutkinnonOsa: {
                    method: "POST",
                    url:
                        SERVICE_LOC +
                        "/perusteet/:perusteId/suoritustavat/:suoritustapa/tutkinnonosat/:viiteId/muokattavakopio"
                },
                update: { method: "POST" }
            }
        );
    })
    .factory("TutkinnonOsaViitteet", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC,
            {},
            {
                versiot: { method: "GET", isArray: true, url: SERVICE_LOC + "/tutkinnonosat/viite/:viiteId/versiot" },
                getVersio: { method: "GET", url: SERVICE_LOC + "/tutkinnonosat/viite/:viiteId/versio/:versioId" },
                palauta: { method: "POST", url: SERVICE_LOC + "/tutkinnonosat/palauta/viite/:viiteId/versio/:versioId" }
            }
        );
    })
    .factory("TutkinnonOsanOsaAlue", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteenosat/:viiteId/osaalue/:osaalueenId",
            {
                osaalueenId: "@id"
            },
            {
                list: { method: "GET", isArray: true, url: SERVICE_LOC + "/perusteenosat/:osanId/osaalueet" },
                versioList: {
                    method: "GET",
                    isArray: true,
                    url: SERVICE_LOC + "/perusteenosat/:osanId/osaalueet/versio/:versioId"
                }
            }
        );
    })
    .factory("PerusteenOsanTyoryhmat", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteprojektit/:projektiId/perusteenosat/:osaId/tyoryhmat",
            {
                projektiId: "@projektiId",
                osaId: "@osaId"
            },
            {
                get: { method: "GET", isArray: true },
                save: { method: "POST", isArray: true }
            }
        );
    })
    .factory("Osaamistavoite", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteenosat/:osanId/osaalue/:osaalueenId/osaamistavoite/:osaamistavoiteId",
            {
                osaamistavoiteId: "@id"
            },
            {
                list: {
                    method: "GET",
                    isArray: true,
                    url: SERVICE_LOC + "/perusteenosat/:osanId/osaalue/:osaalueenId/osaamistavoitteet"
                }
            }
        );
    })
    .service("Tutke2Service", function(Tutke2OsaData, Utils, YleinenData) {
        return {
            fetch: function(tyyppi) {
                if (_.includes(YleinenData.yhteisetTutkinnonOsat, tyyppi)) {
                    //if (tyyppi === "tutke2") {
                    if (Tutke2OsaData.get()) {
                        Tutke2OsaData.get().fetch();
                    }
                }
            },
            mergeOsaAlueet: function(tutkinnonOsa) {
                if (_.includes(YleinenData.yhteisetTutkinnonOsat, tutkinnonOsa.tyyppi)) {
                    //if (tutkinnonOsa.tyyppi === "tutke2") {
                    tutkinnonOsa.osaAlueet = _.map(Tutke2OsaData.get().$editing, function(osaAlue) {
                        var item = { nimi: osaAlue.nimi, id: null };
                        if (osaAlue.id) {
                            item.id = osaAlue.id;
                        }
                        return item;
                    });
                }
            },
            validate: function(tyyppi) {
                if (_.includes(YleinenData.yhteisetTutkinnonOsat, tyyppi)) {
                    //if (tyyppi === "tutke2") {
                    return _.all(
                        _.map(Tutke2OsaData.get().$editing, function(item) {
                            return Utils.hasLocalizedText(item.nimi);
                        })
                    );
                } else {
                    return true;
                }
            }
        };
    })
    .service("TutkinnonOsanValidointi", function($q, PerusteenOsat) {
        function validoi(tutkinnonOsa) {
            var virheet = [];
            var kentat = ["nimi"];
            _.forEach(kentat, function(f) {
                if (!tutkinnonOsa[f] || tutkinnonOsa[f] === "") {
                    virheet.push(f);
                }
            });
            if (!_.isEmpty(virheet)) {
                virheet.unshift("koodi-virhe-3");
            }
            return virheet;
        }

        return {
            validoi: function(tutkinnonOsa) {
                var deferred = $q.defer();

                PerusteenOsat.byKoodiUri(
                    {
                        osanId: tutkinnonOsa.koodiUri
                    },
                    function(re) {
                        if (re.length === 0) {
                            deferred.resolve();
                        } else {
                            deferred.reject(["koodi-virhe-2"]);
                        }
                    },
                    function() {
                        var virheet = validoi(tutkinnonOsa);
                        if (_.isEmpty(virheet)) {
                            deferred.resolve();
                        } else {
                            deferred.reject(virheet);
                        }
                    }
                );
                return deferred.promise;
            }
        };
    });

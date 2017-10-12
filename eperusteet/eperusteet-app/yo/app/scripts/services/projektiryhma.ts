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
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .factory("PerusteprojektiTyoryhmat", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteprojektit/:id/tyoryhma/:nimi",
            {
                id: "@id",
                nimi: "@nimi"
            },
            {
                getAll: {
                    method: "GET",
                    isArray: true,
                    url: SERVICE_LOC + "/perusteprojektit/:id/perusteenosientyoryhmat"
                },
                get: { method: "GET", isArray: true },
                save: { method: "POST", isArray: true }
            }
        );
    })
    .factory("PerusteprojektiJasenet", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteprojektit/:id/jasenet/tiedot",
            { id: "@id" },
            {
                get: { method: "GET", isArray: true }
            }
        );
    })
    .service("Projektiryhma", function(PerusteprojektiTyoryhmat, PerusteprojektiJasenet, VariHyrra, $q) {
        function parsiJasenet(jasenet) {
            var reval: any = {};

            VariHyrra.reset();
            _.forEach(jasenet, function(j) {
                // Käyttäjän nimi
                j.$nimi = (!_.isEmpty(j.kutsumanimi) ? j.kutsumanimi : j.etunimet) + " " + j.sukunimi;
                j.color = VariHyrra.next();

                if (!_.isEmpty(j.yhteystiedot)) {
                    // Yhteystietotyyppit
                    _.forEach((<any>_.first(j.yhteystiedot)).yhteystiedot, function(yt) {
                        if (yt.yhteystietoTyyppi === "YHTEYSTIETO_SAHKOPOSTI") {
                            j.$sahkoposti = yt.yhteystietoArvo;
                        } else if (
                            yt.yhteystietoTyyppi === "YHTEYSTIETO_MATKAPUHELINNUMERO" &&
                            !_.isEmpty(yt.yhteystietoArvo)
                        ) {
                            j.$puhelinnumero = yt.yhteystietoArvo;
                        } else if (
                            _.isEmpty(j.$puhelinnumero) &&
                            yt.yhteystietoTyyppi === "YHTEYSTIETO_PUHELINNUMERO" &&
                            !_.isEmpty(yt.yhteystietoArvo)
                        ) {
                            j.$puhelinnumero = yt.yhteystietoArvo;
                        }
                    });
                }
            });

            reval.ryhma = _.groupBy(jasenet, "tehtavanimike");
            reval.jasenet = jasenet;
            return reval;
        }

        function parsiTyoryhmat(tyoryhmat) {
            var reval: any = {};
            reval.tyoryhmat = _.groupBy(tyoryhmat, "nimi");
            _.forEach(reval.tyoryhmat, function(v, k) {
                reval.tyoryhmat[k] = _.zipObject(_.map(v, "kayttajaOid"), v);
            });
            return reval;
        }

        return {
            tyoryhmat: function(perusteprojektiId) {
                var deferred = $q.defer();
                PerusteprojektiTyoryhmat.get(
                    { id: perusteprojektiId },
                    function(res) {
                        deferred.resolve(_.unique(_.map(res, "nimi")));
                    },
                    deferred.reject
                );
                return deferred.promise;
            },
            jasenetJaTyoryhmat: function(perusteprojektiId, successCb, failureCb) {
                successCb = successCb || angular.noop;
                failureCb = failureCb || angular.noop;

                $q
                    .all([
                        PerusteprojektiJasenet.get({ id: perusteprojektiId }).$promise,
                        PerusteprojektiTyoryhmat.get({ id: perusteprojektiId }).$promise
                    ])
                    .then(function(data) {
                        successCb(_.merge(parsiJasenet(data[0]), parsiTyoryhmat(data[1])));
                    }, failureCb);
            }
        };
    });

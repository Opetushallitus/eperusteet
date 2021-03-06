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
    .factory("Dokumentti", function($resource, SERVICE_LOC) {
        // api:
        //
        // Generointi:
        // POST /dokumentit?perusteId=<id>&kieli=fi
        // GET  /dokumentit/:token/tila
        // Valmiin dokumentin hakeminen:
        // GET /dokumentit/:token

        const baseUrl = SERVICE_LOC + "/dokumentit/:id";
        return $resource(
            baseUrl,
            {
                id: "@id"
            },
            {
                tila: {
                    method: "GET",
                    url: baseUrl + "/tila"
                }
            }
        );
    })
    .service("Pdf", function(Dokumentti, SERVICE_LOC) {
        function generoiPdf(perusteId, kieli, suoritustapa, version) {
            return Dokumentti.save(
                {
                    perusteId: perusteId,
                    kieli: kieli,
                    suoritustapakoodi: suoritustapa,
                    version: version
                },
                null
            ).$promise;
        }

        function haeTila(tokenId) {
            return Dokumentti.tila({
                id: tokenId
            }).$promise;
        }

        function haeDokumentti(tokenId) {
            return Dokumentti.get({
                id: tokenId
            }).$promise;
        }

        function haeLinkki(tokenId) {
            return SERVICE_LOC + "/dokumentit/" + tokenId + ".pdf";
        }

        function haeUusin(perusteId, kieli, suoritustapa) {
            return Dokumentti.get({
                perusteId: perusteId,
                kieli: kieli,
                suoritustapa: suoritustapa
            }).$promise;
        }

        function hae(perusteId, kieli, version, suoritustapa) {
            return Dokumentti.get({
                perusteId: perusteId,
                kieli: kieli,
                version: version,
                suoritustapa: suoritustapa
            }).$promise;
        }

        return {
            generoiPdf,
            haeDokumentti,
            haeTila,
            haeLinkki,
            haeUusin,
            hae
        };
    })
    .factory("PdfCreation", function($uibModal, YleinenData) {
        let peruste = null;
        return {
            setPeruste(p) {
                peruste = p;
            },
            openModal(isOpas, isAmmatillinen) {
                $uibModal.open({
                    template: require("views/modals/pdfcreation.html"),
                    controller: "PdfCreationController",
                    resolve: {
                        peruste: () => peruste,
                        perusteId: () => peruste.id,
                        isOpas: () => isOpas,
                        isAmmatillinen: () => isAmmatillinen,
                        kielet: () => ({
                            lista: YleinenData.kielet,
                            valittu: YleinenData.kieli
                        })
                    }
                });
            }
        };
    })
    .controller("PdfCreationController", function(
        $scope,
        $window,
        kielet,
        Pdf,
        peruste,
        perusteId,
        $timeout,
        Notifikaatiot,
        Kaanna,
        PerusteProjektiService,
        $stateParams,
        YleinenData,
        isAmmatillinen,
        isOpas
    ) {
        $scope.isOpas = isOpas;
        $scope.isAmmatillinen = isAmmatillinen;
        $scope.kielet = kielet;
        $scope.kvliitekielet = YleinenData.kvliitekielet;
        $scope.docs = {};
        $scope.kvliitteet = {};
        $scope.versiot = {
            lista: ["uusi"],
            valittu: "uusi"
        };

        let pdfToken = null;
        let suoritustapa =
            $stateParams.suoritustapa ||
            PerusteProjektiService.getSuoritustapa() ||
            YleinenData.valitseSuoritustapaKoulutustyypille(
                peruste.koulutustyyppi,
                _.find(peruste.suoritustavat, { suoritustapakoodi: "reformi" })
            );

        $scope.hasPdf = (kieli: string, version: string = $scope.versiot.valittu) => {
            const doc = version === "kvliite" ? $scope.kvliitteet[kieli] : $scope.docs[kieli];
            return _.isObject(doc) && doc.id && doc.tila === "valmis";
        };

        async function fetchLatest() {
            const docs: any = await Promise.all(
                _.map(kielet.lista, kieli => Pdf.haeUusin(perusteId, kieli, suoritustapa))
            );
            for (const doc of docs) {
                if (doc.id !== null) {
                    doc.url = Pdf.haeLinkki(doc.id);
                }
                $scope.docs[doc.kieli] = doc;
            }

            const kvliitteet: any = await Promise.all(
                _.map(YleinenData.kvliitekielet, kieli => Pdf.hae(perusteId, kieli, "kvliite", suoritustapa))
            );
            for (const kvliite of kvliitteet) {
                if (kvliite.id !== null) {
                    kvliite.url = Pdf.haeLinkki(kvliite.id);
                }
                $scope.kvliitteet[kvliite.kieli] = kvliite;
            }
        }

        $scope.download = (kieli, version) => {
            if (version === "kvliite") {
                $window.open($scope.kvliitteet[kieli].url, "_blank");
            } else {
                $window.open($scope.docs[kieli].url, "_blank");
            }
        };

        function enableActions(value: boolean) {
            $scope.generateInProgress = !value;
        }

        async function getStatus(id) {
            const res = await Pdf.haeTila(id);
            $scope.tila = res.tila;
            switch (res.tila) {
                case "jonossa":
                case "luodaan":
                case "ei_ole":
                    startPolling(res.id);
                    break;
                case "valmis":
                    Notifikaatiot.onnistui("dokumentti-luotu");
                    res.url = Pdf.haeLinkki(res.id);
                    if (res.generatorVersion === "kvliite") {
                        $scope.kvliitteet[res.kieli] = res;
                    } else {
                        $scope.docs[res.kieli] = res;
                    }
                    enableActions(true);
                    break;
                default:
                    // "epaonnistui" + others(?)
                    Notifikaatiot.fataali(
                        Kaanna.kaanna("dokumentin-luonti-epaonnistui") + ": " + res.virhekoodi || res.tila
                    );
                    enableActions(true);
                    break;
            }
        }

        function startPolling(id) {
            $scope.poller = $timeout(() => {
                getStatus(id);
            }, 3000);
        }

        $scope.$on("$destroy", () => {
            $timeout.cancel($scope.poller);
        });

        $scope.generate = async function(kieli, versio = $scope.versiot.valittu) {
            enableActions(false);
            $scope.tila = "jonossa";
            try {
                const res = await Pdf.generoiPdf(perusteId, kieli, suoritustapa, versio);
                if (res.id !== null) {
                    pdfToken = res.id;
                    startPolling(res.id);
                }
            } catch (ex) {
                enableActions(true);
                $scope.tila = "ei_ole";
            }
        };

        fetchLatest();
    });

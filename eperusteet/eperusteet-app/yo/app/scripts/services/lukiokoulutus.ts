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
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .service("LukiokoulutusService", function(
        LukionOppiaineet,
        $q,
        SuoritustapaSisalto,
        $log,
        Notifikaatiot,
        LukiokoulutuksenSisalto,
        LukioKurssit,
        LukiokoulutusYleisetTavoitteet,
        LukiokoulutusAihekokonaisuudet
    ) {
        this.OPETUKSEN_YLEISET_TAVOITTEET = "opetuksen_yleiset_tavoitteet";
        this.AIHEKOKONAISUUDET = "aihekokonaisuudet";
        this.OPPIAINEET_OPPIMAARAT = "oppiaineet_oppimaarat";
        this.KURSSIT = "kurssit";

        this.LABELS = {
            "opetuksen-yleiset-tavoitteet": this.OPETUKSEN_YLEISET_TAVOITTEET,
            aihekokonaisuudet: this.AIHEKOKONAISUUDET,
            "oppiaineet-oppimaarat": this.OPPIAINEET_OPPIMAARAT
        };

        var tiedot = null;
        var cached = {};
        this.setTiedot = function(value) {
            tiedot = value;
        };

        var getPerusteId = function() {
            return tiedot.getProjekti()._peruste;
        };
        this.getPerusteId = getPerusteId;

        this.sisallot = [
            {
                tyyppi: this.OPETUKSEN_YLEISET_TAVOITTEET,
                label: "opetuksen-yleiset-tavoitteet",
                emptyPlaceholder: "tyhja-placeholder-opetuksen-yleiset-tavoitteet",
                addLabel: "lisaa-opetuksen-yleinen-tavoite"
            },
            {
                tyyppi: this.AIHEKOKONAISUUDET,
                label: "aihekokonaisuudet",
                emptyPlaceholder: "tyhja-placeholder-aihekokonaisuudet",
                addLabel: "lisaa-aihekokonaisuus"
            },
            {
                tyyppi: this.OPPIAINEET_OPPIMAARAT,
                label: "oppiaineet-oppimaarat",
                emptyPlaceholder: "tyhja-placeholder-oppiaineet-oppimaarat",
                addLabel: "lisaa-oppiaine"
            }
        ];

        var errorCb = function(err) {
            Notifikaatiot.serverCb(err);
        };

        function promisify(data) {
            var deferred = $q.defer();
            if (!_.isArray(data)) {
                _.extend(deferred, data);
            }
            deferred.resolve(data);
            return deferred.promise;
        }

        function commonParams(extra?) {
            if (!tiedot) {
                return {};
            }
            var obj = { perusteId: tiedot.getProjekti()._peruste };
            if (extra) {
                _.extend(obj, extra);
            }
            return obj;
        }

        function getOsaGeneric(resource, params) {
            return resource.get(commonParams({ osanId: params.osanId, versioId: params.revNumber })).$promise;
        }

        function getAihekokonasuusOsa(params) {
            if (params.versioId) {
                return LukiokoulutusAihekokonaisuudet.getAihekokonaisuusByVersio(
                    commonParams({ aihekokonaisuusId: params.osanId, versioId: params.versioId })
                ).$promise;
            } else {
                return getOsaGeneric(LukiokoulutusAihekokonaisuudet, params);
            }
        }

        this.getOsa = function(params) {
            if (params.osanId === "uusi") {
                return promisify({});
            }

            switch (params.osanTyyppi) {
                case this.OPETUKSEN_YLEISET_TAVOITTEET:
                    return promisify({});
                case this.AIHEKOKONAISUUDET:
                    return getAihekokonasuusOsa(params);
                case this.OPPIAINEET_OPPIMAARAT:
                    return getOsaGeneric(LukionOppiaineet, params);
                case this.KURSSIT:
                    return getOsaGeneric(LukioKurssit, params);
                case "tekstikappale":
                    return getOsaGeneric(LukiokoulutuksenSisalto, params);
                default:
                    break;
            }
        };

        this.deleteOsa = function(osa, success) {
            var successCb = success || angular.noop;
            if (osa.$url) {
                LukiokoulutuksenSisalto.delete(commonParams({ osanId: osa.id }), successCb, errorCb);
            } else {
                osa.$delete(commonParams(), successCb, errorCb);
            }
        };

        this.saveOsa = function(data, config, success) {
            var successCb = success || angular.noop;
            switch (config.osanTyyppi) {
                case this.OPPIAINEET_OPPIMAARAT:
                    LukionOppiaineet.save(commonParams(), data, successCb, errorCb);
                    break;
                default:
                    // Sisältö
                    LukiokoulutuksenSisalto.save(commonParams(), data, successCb, errorCb);
                    break;
            }
        };

        this.addSisaltoChild = function(id, success) {
            LukiokoulutuksenSisalto.addChild(commonParams({ osanId: id }), {}, success);
        };

        this.updateSisaltoViitteet = function(sisalto, data, success) {
            var payload = commonParams(sisalto);
            LukiokoulutuksenSisalto.updateViitteet(payload, success, Notifikaatiot.serverCb);
        };

        this.getTekstikappaleet = function() {
            // TODO oikea data
            return [];
        };

        this.getOppimaarat = function(oppiaine) {
            if (!oppiaine.koosteinen) {
                return promisify([]);
            }
            return LukionOppiaineet.oppimaarat(commonParams({ osanId: oppiaine.id })).$promise;
        };

        this.getSisalto = function() {
            return SuoritustapaSisalto.get(commonParams({ suoritustapa: "lukiokoulutus" }));
        };

        this.clearCache = function() {
            cached = {};
        };

        this.getOsat = function(tyyppi, useCache) {
            if (useCache && cached[tyyppi]) {
                return promisify(cached[tyyppi]);
            }
            switch (tyyppi) {
                case this.OPPIAINEET_OPPIMAARAT:
                    return LukionOppiaineet.query(commonParams(), function(data) {
                        cached[tyyppi] = data;
                    }).$promise;
                case this.KURSSIT:
                    return LukioKurssit.query(commonParams(), function(data) {
                        cached[tyyppi] = data;
                    }).$promise;
                case this.AIHEKOKONAISUUDET:
                    return LukiokoulutusAihekokonaisuudet.query(commonParams(), function(data) {
                        cached[tyyppi] = data;
                    }).$promise;
                default:
                    return promisify([]);
            }
        };
    })
    .service("LukioKurssiService", function(
        LukioKurssit,
        Lukitus,
        Notifikaatiot,
        LukioOppiaineKurssiRakenne,
        LukiokoulutusService,
        $translate,
        $q,
        $log,
        LukioRakenne
    ) {
        var lukittu = function(id, cb, editointiCheck) {
                return Lukitus.lukitseLukioKurssi(id, cb, editointiCheck);
            },
            success = function(d, msg) {
                return function(tiedot) {
                    Notifikaatiot.onnistui(msg);
                    d.resolve(tiedot);
                };
            },
            vapauta = function(d, msg) {
                return function(kurssinTiedot) {
                    Lukitus.vapauta(function() {
                        success(d, msg)(kurssinTiedot);
                    });
                };
            };
        var kurssitCache = {};

        /**
     * Lists kurssit and related oppiaineet with jarjestys for given peruste.
     *
     * @param perusteId <number> id of Peruste
     * @return Promise<LukioKurssiListausDto[]>
     */
        var listByPeruste = function(perusteId, cb?) {
            if (kurssitCache[perusteId]) {
                var d = $q.defer(),
                    toReturn = _.cloneDeep(kurssitCache[perusteId]);
                d.resolve(toReturn);
                if (cb) {
                    cb(toReturn);
                }
                return d.promise;
            }
            return LukioKurssit.query(
                {
                    perusteId: perusteId
                },
                cb
            ).$promise.then(function(kurssit) {
                kurssitCache[perusteId] = kurssit;
            });
        };

        /**
     * @param id of kurssi
     * @return LukiokurssiTarkasteleDto
     */
        var get = function(id, cb) {
            return LukioKurssit.get({ osanId: id, perusteId: LukiokoulutusService.getPerusteId() }, cb);
        };

        /**
     * @param id of kurssi
     * @param version of kurssi
     * @returns LukiokurssiTarkasteleDto
     */
        var getVersion = function(id, version, cb) {
            return LukioKurssit.getVersion(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id,
                    version: version
                },
                cb
            );
        };

        /**
     * Saves a new Lukiokurssi and related oppiaineet
     *
     * @param kurssi <LukioKurssiLuontiDto>
     * @return Promise<LukiokurssiTarkasteleDto>
     */
        var save = function(kurssi) {
            var d = $q.defer();
            delete kurssitCache[LukiokoulutusService.getPerusteId()];
            LukioKurssit.save(
                {
                    perusteId: LukiokoulutusService.getPerusteId()
                },
                kurssi,
                success(d, "tallennus-onnistui"),
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * Locks and updates Lukiokurssi and related oppiaineet
     *
     * @param kurssi <LukiokurssiMuokkausDto>
     * @return Promise<LukiokurssiTarkasteleDto>
     */
        var update = function(kurssi) {
            var d = $q.defer();
            delete kurssitCache[LukiokoulutusService.getPerusteId()];
            LukioKurssit.update(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: kurssi.id
                },
                kurssi,
                vapauta(d, "tallennus-onnistui"),
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * Locks and updates Lukiokurssi's related oppiaineet
     *
     * @param kurssi <LukiokurssiMuokkausDto>
     * @return Promise<LukiokurssiTarkasteleDto>
     */
        var updateOppiaineRelations = function(kurssi) {
            $log.info("Update relations of", kurssi);
            var d = $q.defer();
            delete kurssitCache[LukiokoulutusService.getPerusteId()];
            LukioKurssit.updateRelatedOppiainees(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: kurssi.id
                },
                kurssi,
                vapauta(d, "tallennus-onnistui"),
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * @param kurssit Array
     * @param oppiaineFilter function(kurssiOppiaine) => boolean
     * @returns kurssit filtered by oppiaineFilter and ordered by jarjestys in oppiaine matching oppiaineFilter
     */
        var filterOrderedKurssisByOppiaine = function(kurssit, oppiaineFilter) {
            return _(kurssit)
                .filter(function(kurssi) {
                    return _.any(kurssi.oppiaineet, oppiaineFilter);
                })
                .map(_.cloneDeep)
                .sortBy(function(kurssi) {
                    return _(kurssi.oppiaineet)
                        .filter(oppiaineFilter)
                        .first().jarjestys;
                })
                .value();
        };

        /**
     * @param oppiaineId id of oppiaine
     * @return ordered kurssit related to the oppiaine in question
     */
        var listByOppiaine = function(oppiaineId) {
            var d = $q.defer();
            listByPeruste(LukiokoulutusService.getPerusteId()).then(function(kurssit) {
                d.resolve(
                    filterOrderedKurssisByOppiaine(kurssit, function(oa) {
                        return oa.oppiaineId == oppiaineId;
                    })
                );
            });
            return d.promise;
        };

        /**
     * @param tree root node
     */
        var updateOppiaineKurssiStructure = function(tree, liittamattomatKurssit, kommentti) {
            var d = $q.defer();
            var chain = _(tree).flattenTree(function(node) {
                    var kurssiJarjestys = 1,
                        oppiaineJarjestys = 1;
                    return _(node.lapset)
                        .map(function(n) {
                            if (n.dtype == "kurssi") {
                                return {
                                    id: n.id,
                                    oppiaineet: [
                                        {
                                            oppiaineId: node.id,
                                            jarjestys: kurssiJarjestys++
                                        }
                                    ]
                                };
                            } else if (!n.root) {
                                n.oppiaineId = node.root ? null : node.id;
                                n.jarjestys = oppiaineJarjestys++;
                            }
                            return n;
                        })
                        .value();
                }),
                update = {
                    oppiaineet: chain
                        .filter(function(n) {
                            return !n.root && n.dtype == "oppiaine";
                        })
                        .map(function(oa) {
                            return {
                                id: oa.id,
                                oppiaineId: oa.oppiaineId,
                                jarjestys: oa.jarjestys
                            };
                        })
                        .value(),
                    kurssit: chain
                        .union(
                            _.map(liittamattomatKurssit, function(liittamaton) {
                                return {
                                    id: liittamaton.id,
                                    oppiaineet: []
                                };
                            })
                        )
                        .filter(function(n) {
                            return n.dtype != "oppiaine";
                        })
                        .reducedIndexOf(_.property("id"), function(a, b) {
                            var c = _.clone(a);
                            c.oppiaineet = _.union(a.oppiaineet, b.oppiaineet);
                            return c;
                        })
                        .values()
                        .value(),
                    kommentti: kommentti
                };
            $log.info("Update stucture:", update);
            delete kurssitCache[LukiokoulutusService.getPerusteId()];
            LukioOppiaineKurssiRakenne.updateStructure(
                { perusteId: LukiokoulutusService.getPerusteId() },
                update,
                vapauta(d, "tallennus-onnistui"),
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * Lists versions for given Lukiokurssi
     *
     * @param id of kurssi
     */
        var listVersions = function(id, cb) {
            var d = $q.defer();
            LukioKurssit.versions(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * @param id of kurssi
     * @param version to revert to
     * @param cb
     */
        var palautaLukiokurssi = function(id, version, cb) {
            var d = $q.defer();
            LukioKurssit.revert(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id,
                    version: version
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * @param id of kurssi to delete
     */
        var deleteKurssi = function(id) {
            var d = $q.defer();
            Lukitus.lukitseLukiorakenne(null, false).then(function() {
                lukittu(id, null, false).then(function(res, dl) {
                    delete kurssitCache[LukiokoulutusService.getPerusteId()];
                    LukioKurssit.delete(
                        {
                            perusteId: LukiokoulutusService.getPerusteId(),
                            osanId: id
                        },
                        function(tiedot) {
                            Lukitus.vapauta(function() {
                                Lukitus.vapautaLukiorakenne(function() {
                                    success(d, "poisto-onnistui")(tiedot);
                                });
                            });
                        },
                        Notifikaatiot.serverCb
                    );
                });
            });
            return d.promise;
        };

        /**
     * Lists versions for oppiaine/kurssi-rakenne of current peruste
     */
        var listRakenneVersions = function(cb) {
            var d = $q.defer();
            LukioRakenne.versions(
                {
                    perusteId: LukiokoulutusService.getPerusteId()
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * @param version of rakenne
     */
        var getRakenneVersion = function(version, cb) {
            return LukioRakenne.getVersion(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    version: version
                },
                cb
            );
        };

        /**
     * @param version to revert to
     * @param cb
     */
        var palautaRakenne = function(version, cb) {
            var d = $q.defer();
            LukioRakenne.revert(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    version: version
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        return {
            listByPeruste: listByPeruste,
            get: get,
            save: save,
            lukitse: lukittu,
            update: update,
            listByOppiaine: listByOppiaine,
            filterOrderedKurssisByOppiaine: filterOrderedKurssisByOppiaine,
            deleteKurssi: deleteKurssi,
            updateOppiaineRelations: updateOppiaineRelations,
            updateOppiaineKurssiStructure: updateOppiaineKurssiStructure,
            listVersions: listVersions,
            getVersion: getVersion,
            palautaLukiokurssi: palautaLukiokurssi,
            listRakenneVersions: listRakenneVersions,
            getRakenneVersion: getRakenneVersion,
            palautaRakenne: palautaRakenne
        };
    })
    .service("LukioOppiaineService", function(LukiokoulutusService, LukionOppiaineet, Lukitus, Notifikaatiot, $q) {
        /**
     * @param id of kurssi
     * @param version of kurssi
     * @returns LukiokurssiTarkasteleDto
     */
        var getVersion = function(id, version, cb) {
            return LukionOppiaineet.getVersion(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id,
                    version: version
                },
                cb
            );
        };

        /**
     * Lists versions for given Lukiooppiaine
     *
     * @param id of oppiaine
     */
        var listVersions = function(id, cb) {
            var d = $q.defer();
            LukionOppiaineet.versions(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * @param id of oppiaine
     * @param version to revert to
     * @param cb
     */
        var palautaLukioOppiaine = function(id, version, cb) {
            var d = $q.defer();
            LukionOppiaineet.revert(
                {
                    perusteId: LukiokoulutusService.getPerusteId(),
                    osanId: id,
                    version: version
                },
                null,
                function(results) {
                    d.resolve(results);
                    if (cb) {
                        cb(results);
                    }
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        return {
            listVersions: listVersions,
            getVersion: getVersion,
            palautaLukioOppiaine: palautaLukioOppiaine
        };
    })
    .service("LukioAihekokonaisuudetService", function(
        LukiokoulutusAihekokonaisuudet,
        Lukitus,
        Notifikaatiot,
        LukiokoulutusService,
        $translate,
        $q
    ) {
        /**
     * Tallentaa yhden aihekokonaisuuden
     *
     * @param aihekokonaisuus <LukioAihekokonaisuusLuontiDto>
     * @return Promise<LukioAihekokonaisuusMuokkausDto>
     */
        var saveAihekokonaisuus = function(aihekokonaisuus) {
            var d = $q.defer();
            LukiokoulutusAihekokonaisuudet.saveAihekokonaisuus(
                {
                    perusteId: LukiokoulutusService.getPerusteId()
                },
                aihekokonaisuus,
                function(aihekokonaisuusTiedot) {
                    Notifikaatiot.onnistui("tallennus-onnistui");
                    d.resolve(aihekokonaisuusTiedot);
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     * Tallentaa aihekokobaisuudet yleiskuvauksen
     *
     * @param aihekokonaisuus <AihekokonaisuudetYleiskuvausDto>
     * @return Promise<AihekokonaisuudetYleiskuvausDto>
     */
        var saveAihekokonaisuudetYleiskuvaus = function(aihekokonaisuudetYleiskuvaus) {
            var d = $q.defer();
            LukiokoulutusAihekokonaisuudet.saveAihekokonaisuudetYleiskuvaus(
                {
                    perusteId: LukiokoulutusService.getPerusteId()
                },
                aihekokonaisuudetYleiskuvaus,
                function(aihekokonaisuudetYleiskuvausTiedot) {
                    Notifikaatiot.onnistui("tallennus-onnistui");
                    d.resolve(aihekokonaisuudetYleiskuvausTiedot);
                },
                Notifikaatiot.serverCb
            );
            return d.promise;
        };

        /**
     *
     * Lukitesee ja muokkaa LukioAihekokobaisuuden
     *
     * @param aihekokonaisuus <LukioAihekokonaisuusMuokkausDto>
     * @return Promise<LukioAihekokonaisuusMuokkausDto>
     */
        var updateAihekokonaisuus = function(aihekokonaisuus) {
            var d = $q.defer();
            Lukitus.lukitseLukioAihekokonaisuus(aihekokonaisuus.id, function() {
                LukiokoulutusAihekokonaisuudet.updateAihekokonaisuus(
                    {
                        perusteId: LukiokoulutusService.getPerusteId(),
                        aihekokonaisuusId: aihekokonaisuus.id
                    },
                    aihekokonaisuus,
                    function(aihekokonaisuusTiedot) {
                        Lukitus.vapautaLukioAihekokonaisuus(aihekokonaisuusTiedot.id, function() {
                            Notifikaatiot.onnistui("tallennus-onnistui");
                            d.resolve(aihekokonaisuusTiedot);
                        });
                    },
                    Notifikaatiot.serverCb
                );
            });
            return d.promise;
        };

        var getAihekokonaisuus = function(aihekokonaisuusId, cb) {
            return LukiokoulutusAihekokonaisuudet.query(
                {
                    aihekokonaisuusId: aihekokonaisuusId
                },
                cb
            ).$promise;
        };

        var getAihekokonaisuudetYleiskuvaus = function(versio) {
            if (versio) {
                return LukiokoulutusAihekokonaisuudet.getAihekokonaisuudetYleiskuvausByVersio({
                    perusteId: LukiokoulutusService.getPerusteId(),
                    versioId: versio
                }).$promise;
            } else {
                return LukiokoulutusAihekokonaisuudet.getAihekokonaisuudetYleiskuvaus({
                    perusteId: LukiokoulutusService.getPerusteId()
                }).$promise;
            }
        };

        var deleteAihekokonaisuus = function(aihekokonaisuusId) {
            var d = $q.defer();
            Lukitus.lukitseLukioAihekokonaisuus(aihekokonaisuusId, function() {
                LukiokoulutusAihekokonaisuudet.delete(
                    {
                        perusteId: LukiokoulutusService.getPerusteId(),
                        osanId: aihekokonaisuusId
                    },
                    aihekokonaisuusId,
                    function() {
                        Lukitus.vapautaLukioAihekokonaisuus(aihekokonaisuusId, function() {
                            Notifikaatiot.onnistui("poisto-onnistui");
                            d.resolve(aihekokonaisuusId);
                        });
                    },
                    Notifikaatiot.serverCb
                );
            });
            return d.promise;
        };

        var getAihekokonaisuudetYleiskuvausVersiot = function() {
            return LukiokoulutusAihekokonaisuudet.aihekokonaisuudetYleiskuvausVersiot({
                perusteId: LukiokoulutusService.getPerusteId()
            }).$promise;
        };

        var palautaAihekokonaisuudetYleiskuvaus = function(versioId) {
            return LukiokoulutusAihekokonaisuudet.palautaAihekokonaisuudetYleiskuvaus({
                perusteId: LukiokoulutusService.getPerusteId(),
                versioId: versioId
            }).$promise;
        };

        var getAihekokonaisuusVersiot = function(aihekokonaisuusId) {
            return LukiokoulutusAihekokonaisuudet.aihekokonaisuusVersiot({
                perusteId: LukiokoulutusService.getPerusteId(),
                aihekokonaisuusId: aihekokonaisuusId
            }).$promise;
        };

        var palautaAihekokonaisuus = function(aihekokonaisuusId, versioId) {
            return LukiokoulutusAihekokonaisuudet.palautaAihekokonaisuus({
                perusteId: LukiokoulutusService.getPerusteId(),
                aihekokonaisuusId: aihekokonaisuusId,
                versioId: versioId
            }).$promise;
        };

        return {
            saveAihekokonaisuus: saveAihekokonaisuus,
            saveAihekokonaisuudetYleiskuvaus: saveAihekokonaisuudetYleiskuvaus,
            updateAihekokonaisuus: updateAihekokonaisuus,
            getAihekokonaisuus: getAihekokonaisuus,
            getAihekokonaisuudetYleiskuvaus: getAihekokonaisuudetYleiskuvaus,
            deleteAihekokonaisuus: deleteAihekokonaisuus,
            getAihekokonaisuudetYleiskuvausVersiot: getAihekokonaisuudetYleiskuvausVersiot,
            palautaAihekokonaisuudetYleiskuvaus: palautaAihekokonaisuudetYleiskuvaus,
            getAihekokonaisuusVersiot: getAihekokonaisuusVersiot,
            palautaAihekokonaisuus: palautaAihekokonaisuus
        };
    })
    .service("LukioYleisetTavoitteetService", function(
        LukiokoulutusYleisetTavoitteet,
        Lukitus,
        Notifikaatiot,
        LukiokoulutusService,
        $translate,
        $q
    ) {
        /**
       * Tallentaa aihekokobaisuudet yleiskuvauksen
       *
       * @param yleisetTavoitteet <LukiokoulutuksenYleisetTavoitteetDto>
       * @return Promise<LukiokoulutuksenYleisetTavoitteetDto>
       */
        var updateYleistTavoitteet = function(yleisetTavoitteet) {
            var d = $q.defer();
            LukiokoulutusYleisetTavoitteet.update(
                {
                    perusteId: LukiokoulutusService.getPerusteId()
                },
                yleisetTavoitteet,
                function(yleisetTavoitteetTiedot) {
                    Notifikaatiot.onnistui("tallennus-onnistui");
                    d.resolve(yleisetTavoitteetTiedot);
                },
                Notifikaatiot.serverCb
            );

            return d.promise;
        };

        var getYleisetTavoitteet = function(versio) {
            if (versio) {
                return LukiokoulutusYleisetTavoitteet.getByVersio({
                    perusteId: LukiokoulutusService.getPerusteId(),
                    versioId: versio
                }).$promise;
            } else {
                return LukiokoulutusYleisetTavoitteet.get({
                    perusteId: LukiokoulutusService.getPerusteId()
                }).$promise;
            }
        };

        var getVersiot = function() {
            return LukiokoulutusYleisetTavoitteet.versiot({
                perusteId: LukiokoulutusService.getPerusteId()
            }).$promise;
        };

        var palauta = function(perusteId, versioId) {
            return LukiokoulutusYleisetTavoitteet.palauta({
                perusteId: perusteId,
                versioId: versioId
            }).$promise;
        };

        return {
            updateYleistTavoitteet: updateYleistTavoitteet,
            getYleisetTavoitteet: getYleisetTavoitteet,
            getVersiot: getVersiot,
            palauta: palauta
        };
    });

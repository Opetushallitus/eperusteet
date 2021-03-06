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
    .factory("PerusteprojektiTila", function($resource, SERVICE_LOC) {
        return $resource(SERVICE_LOC + "/perusteprojektit/:id/tila/:tila", { id: "@id" });
    })
    .factory("OmatPerusteprojektit", function($resource, SERVICE_LOC) {
        return $resource(SERVICE_LOC + "/perusteprojektit/omat");
    })
    .factory("PerusteprojektiResource", function($resource, SERVICE_LOC) {
        return $resource(
            SERVICE_LOC + "/perusteprojektit/:id",
            { id: "@id" },
            {
                update: { method: "POST", isArray: false }
            }
        );
    })
    .factory("PerusteprojektiOikeudet", function($resource, SERVICE_LOC) {
        return $resource(SERVICE_LOC + "/perusteprojektit/:id/oikeudet", { id: "@id" });
    })
    .factory("DiaarinumeroUniqueResource", function($resource, SERVICE_LOC) {
        return $resource(SERVICE_LOC + "/perusteprojektit/diaarinumero/uniikki/:diaarinumero");
    })
    .service("PerusteProjektit", function($http, SERVICE_LOC, Notifikaatiot) {
        const haku = (query, success, failure, urlPostfix) => {
            success = success || angular.noop;
            failure = failure || Notifikaatiot.serverCb;
            $http
                .get(SERVICE_LOC + urlPostfix, query)
                .then(res => success(res))
                .catch(err => failure(err));
        };

        const hae = (query, success, failure) => haku(query, success, failure, "/perusteprojektit/info");
        const perusteHaku = (query, success, failure) => haku(query, success, failure, "/perusteprojektit/perusteHaku");

        return {
            hae: hae,
            perusteHaku: perusteHaku
        };
    })
    .service("PerusteProjektiService", function(
        $rootScope,
        $location,
        $state,
        $stateParams,
        $q,
        $timeout,
        $uibModal,
        YleinenData
    ) {
        var pp = {};
        var suoritustapa = "";

        function save(obj) {
            obj = obj || {};
            pp = _.merge(_.clone(pp), _.clone(obj));
        }

        function get() {
            return _.clone(pp);
        }

        function clean() {
            pp = {};
        }

        function watcher(scope, kentta) {
            scope.$watch(
                kentta,
                function(temp) {
                    save(temp);
                },
                true
            );
        }

        function update() {
            $rootScope.$broadcast("update:perusteprojekti");
        }

        function getSuoritustapa() {
            return _.clone(suoritustapa);
        }

        function setSuoritustapa(st) {
            suoritustapa = _.clone(st);
        }

        function cleanSuoritustapa() {
            suoritustapa = "";
        }

        function hasSuoritustapa(peruste, suoritustapakoodi) {
            return (
                peruste &&
                _.find(peruste.suoritustavat, function(st: any) {
                    return st.suoritustapakoodi === suoritustapakoodi;
                })
            );
        }

        function getRightSuoritustapa(peruste, projekti) {
            return hasSuoritustapa(peruste, getSuoritustapa()) ? getSuoritustapa() : projekti.suoritustapa;
        }

        function getInfo(projekti, peruste) {
            // Joko projektissa tai perusteessa voi olla tieto koulutustyypistä ja toteutuksesta
            if (_.isObject(projekti)
                && projekti.koulutustyyppi === "koulutustyyppi_2"
                && projekti.toteutus === "lops2019"
                || (_.isObject(peruste)
                    && peruste.koulutustyyppi === "koulutustyyppi_2"
                    && peruste.toteutus === "lops2019")) {
                return {
                    oletusSuoritustapa: "lukiokoulutus2019",
                    sisaltoTunniste: "lops2019"
                }
            } else {
                const koulutustyyppi = (_.isObject(projekti) && projekti.koulutustyyppi)
                    || (_.isObject(peruste) && peruste.koulutustyyppi);
                return YleinenData.koulutustyyppiInfo[koulutustyyppi];
            }
        }

        /**
         * Luo oikea url perusteprojektille
         * @param peruste optional
         */
        function urlFn(projekti, peruste) {
            let suoritustapa;
            let sisaltoTunniste = "sisalto";
            let info = getInfo(projekti, peruste);

            if (peruste && peruste.tyyppi === "opas") {
                suoritustapa = "opas";
                sisaltoTunniste = "opassisalto";
            } else {
                if (peruste && peruste.reforminMukainen) {
                    suoritustapa = "reformi";
                } else if (info) {
                    suoritustapa = info.oletusSuoritustapa;
                    sisaltoTunniste = info.sisaltoTunniste;
                }

                // Tilanteesta riippuen suoritustapatieto voi tulla projektin mukana (listausnäkymät)
                // Yritetään ensisijaisesti käyttää perusteen tietoa
                if (peruste && _.isArray(peruste.suoritustavat) && !_.isEmpty(peruste.suoritustavat)) {
                    const suoritustapakoodit = _.map(peruste.suoritustavat, "suoritustapakoodi");
                    if (!_.includes(suoritustapakoodit, suoritustapa)) {
                        suoritustapa = _.first(suoritustapakoodit);
                    }
                } else if (projekti && _.isArray(projekti.suoritustavat) && !_.isEmpty(projekti.suoritustavat)) {
                    if (!_.includes(projekti.suoritustavat, suoritustapa)) {
                        suoritustapa = _.first(projekti.suoritustavat);
                    }
                }
            }

            const stateName = "root.perusteprojekti.suoritustapa." + sisaltoTunniste;
            const hrefParams = {
                lang: $stateParams.lang || "fi",
                perusteProjektiId: projekti.id,
                suoritustapa
            };

            const url = $state.href(stateName, hrefParams);
            return url;
        }

        function mergeProjekti(projekti, tuoPohja) {
            var deferred = $q.defer();
            $uibModal
                .open({
                    template: require("views/modals/projektiSisaltoTuonti.html"),
                    controller: "ProjektiTiedotSisaltoModalCtrl",
                    resolve: {
                        pohja: function() {
                            return !!tuoPohja;
                        }
                    }
                })
                .result.then(function(peruste) {
                    peruste.tila = "laadinta";
                    peruste.tyyppi = "normaali";
                    var onOps = false;
                    projekti.perusteId = peruste.id;
                    projekti.koulutustyyppi = peruste.koulutustyyppi;
                    _.forEach(peruste.suoritustavat, function(st) {
                        if (st.suoritustapakoodi === "ops") {
                            onOps = true;
                            projekti.laajuusYksikko = st.laajuusYksikko;
                        }
                    });
                    deferred.resolve(peruste, projekti);
                }, deferred.reject);
            return deferred.promise;
        }

        function goToProjektiState(projekti, peruste) {
            const url = urlFn(projekti, peruste);
            $timeout(() => {
                $location.url(url.slice(1));
            });
        }

        return {
            mergeProjekti: mergeProjekti,
            save: save,
            get: get,
            watcher: watcher,
            clean: clean,
            update: update,
            getSuoritustapa: getSuoritustapa,
            setSuoritustapa: setSuoritustapa,
            cleanSuoritustapa: cleanSuoritustapa,
            getUrl: urlFn,
            goToProjektiState,
            isPdfEnabled: function(peruste) {
                if (peruste.tyyppi === "opas") {
                    return true;
                }
                return (
                    YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi] &&
                    YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi].hasPdfCreation
                );
            }
        };
    })
    .service("TutkinnonOsaEditMode", function() {
        this.mode = false;
        this.setMode = function(mode) {
            this.mode = mode;
        };
        this.getMode = function() {
            var ret = this.mode;
            this.mode = false;
            return ret;
        };
    })
    .service("PerusteprojektiTiedotService", function(
        $q,
        $state,
        $stateParams,
        PerusteprojektiResource,
        Perusteet,
        $log,
        $rootScope,
        PerusteProjektiService,
        Notifikaatiot,
        YleinenData,
        PerusopetusService,
        SuoritustapaSisalto,
        LukiokoulutusService,
        LukioKurssiService,
        AIPEService,
        Lops2019Service,
    ) {
        var deferred = $q.defer();
        var projekti: any = {};
        var peruste: any = {};
        var sisalto: any = {};
        var ylTiedot: any = {};
        var self = this;
        var projektinTiedotDeferred = $q.defer();

        this.getProjekti = function() {
            return _.clone(projekti);
        };

        this.setProjekti = function(obj) {
            projekti = _.clone(obj);
        };

        this.getPeruste = function() {
            return _.clone(peruste);
        };

        this.getSisalto = function() {
            return _.clone(sisalto);
        };

        this.getYlTiedot = function() {
            return _.clone(ylTiedot);
        };

        function setPeruste(uusiPeruste) {
            peruste = uusiPeruste;
        }

        const PerusteContextMapping = {
            "projektin-tiedot": { opas: "projekti-opas" },
            "projekti-projektiryhma": { opas: "projekti-opastyoryhma" },
            "perusteen-kielet": { opas: "oppaan-kielet" },
            "perusteen-tiedot": { opas: "oppaan-tiedot" },
            "luo-pdf-dokumentti-perusteesta": { opas: "luo-pdf-dokumentti-oppaasta" }
        };

        this.getPerusteprojektiKaannokset = (tyyppi?: string) => {
            if (tyyppi === "opas") {
                return _.zipObject(_.keys(PerusteContextMapping), _.map(PerusteContextMapping, "opas"));
            } else {
                return _.zipObject(_.keys(PerusteContextMapping), _.keys(PerusteContextMapping));
            }
        };

        this.cleanData = function() {
            projekti = {};
            peruste = {};
            sisalto = {};
        };

        function getYlStructure(labels, osatProvider, sisaltoProvider, kurssitProvider) {
            // TODO replace with one resource call that fetches the whole structure
            var promises = [];
            _.each(labels, function(key) {
                var promise = osatProvider(key);
                promise.then(function(data) {
                    ylTiedot[key] = data;
                });
                promises.push(promise);
            });
            var sisaltoPromise = sisaltoProvider();
            sisaltoPromise.then(function(data) {
                ylTiedot.sisalto = data;
            });
            promises.push(sisaltoPromise);
            if (kurssitProvider) {
                var kurssiPromise = kurssitProvider();
                kurssiPromise.then(function(data) {
                    ylTiedot.kurssit = data;
                });
                promises.push(kurssiPromise);
            }
            const result = $q.all(promises);
            return result;
        }

        this.haeSisalto = (perusteId, suoritustapa): Promise<any> => {
            return new Promise(async (resolve, reject) => {
                if (peruste.tyyppi === "opas") {
                    SuoritustapaSisalto.get(
                        {
                            perusteId: perusteId,
                            suoritustapa: "opas"
                        },
                        function(vastaus) {
                            deferred.resolve(vastaus);
                            sisalto = vastaus;
                            resolve(sisalto);
                        },
                        function(virhe) {
                            Notifikaatiot.virhe(virhe);
                            reject(virhe);
                            deferred.reject(virhe);
                        }
                    );
                } else if (
                    YleinenData.isPerusopetus(peruste) ||
                    YleinenData.isAipe(peruste) ||
                    YleinenData.isLukiokoulutus(peruste)
                ) {
                    var labels,
                        osatProvider,
                        sisaltoProvider,
                        kurssitProvider = null;
                    if (YleinenData.isLukiokoulutus(peruste)) {
                        if (peruste.toteutus === 'lops2019') {
                            osatProvider = function(key) {
                                return Lops2019Service.getOsat(key, true);
                            };
                            sisaltoProvider = function() {
                                return Lops2019Service.getSisalto();
                            };
                        } else {
                            labels = LukiokoulutusService.LABELS;
                            osatProvider = function(key) {
                                return LukiokoulutusService.getOsat(key, true);
                            };
                            sisaltoProvider = function() {
                                return LukiokoulutusService.getSisalto().$promise;
                            };
                            kurssitProvider = function() {
                                return LukioKurssiService.listByPeruste(perusteId);
                            };
                        }
                    } else if (YleinenData.isAipe(peruste)) {
                        labels = AIPEService.LABELS;
                        osatProvider = function(key) {
                            return AIPEService.getOsat(key, true);
                        };
                        sisaltoProvider = function() {
                            return AIPEService.getSisalto(suoritustapa).$promise;
                        };
                    } else {
                        labels = PerusopetusService.LABELS;
                        osatProvider = function(key) {
                            return PerusopetusService.getOsat(key, true);
                        };
                        sisaltoProvider = function() {
                            return PerusopetusService.getSisalto(suoritustapa).$promise;
                        };
                    }

                    getYlStructure(labels, osatProvider, sisaltoProvider, kurssitProvider).then(sisalto => {
                        sisalto = ylTiedot.sisalto;
                        resolve(sisalto);
                        deferred.resolve(ylTiedot.sisalto);
                    });
                } else {
                    SuoritustapaSisalto.get(
                        {
                            perusteId: perusteId,
                            suoritustapa: suoritustapa
                        },
                        function(vastaus) {
                            sisalto = vastaus;
                            deferred.resolve(sisalto);
                            resolve(sisalto);
                        },
                        function(virhe) {
                            deferred.reject(virhe);
                        }
                    );
                }
            });
        };

        this.projektinTiedotAlustettu = function() {
            return projektinTiedotDeferred.promise;
        };

        this.oikeastiHaeProjekti = async function(id) {
            try {
                projekti = await PerusteprojektiResource.get({ id }).$promise;
                peruste = await Perusteet.get({ perusteId: projekti._peruste }).$promise;
                if (!_.isEmpty(peruste.suoritustavat)) {
                    peruste.suoritustavat = _.sortBy(peruste.suoritustavat, "suoritustapakoodi");
                }
                return projekti;
            } catch (virhe) {
                Notifikaatiot.serverCb(virhe);
            }
        };

        this.oikeastiHaePeruste = async function(perusteId) {
            try {
                const peruste = await Perusteet.get({ perusteId }).$promise;
                return peruste;
            } catch (virhe) {
                Notifikaatiot.serverCb(virhe);
            }
        };

        this.alustaProjektinTiedot = function(stateParams) {
            LukiokoulutusService.setTiedot(this);
            Lops2019Service.setTiedot(this);
            PerusopetusService.setTiedot(this);
            AIPEService.setTiedot(this);
            projektinTiedotDeferred = $q.defer();
            try {
                PerusteprojektiResource.get({ id: stateParams.perusteProjektiId }).$promise.then(projektiRes => {
                    projekti = projektiRes;
                    Perusteet.get({ perusteId: projekti._peruste }).$promise.then(perusteRes => {
                        if (!_.isEmpty(perusteRes.suoritustavat)) {
                            perusteRes.suoritustavat = _.sortBy(perusteRes.suoritustavat, "suoritustapakoodi");
                        }
                        setPeruste(perusteRes);
                        projektinTiedotDeferred.resolve();
                    });
                });
            } catch (virhe) {
                Notifikaatiot.serverCb(virhe);
                projektinTiedotDeferred.reject(virhe);
            }
            return projektinTiedotDeferred.promise;
        };

        this.alustaPerusteenSisalto = async function(stateParams, forced) {
            PerusteProjektiService.setSuoritustapa(stateParams.suoritustapa);
            if (forced ||
                stateParams.suoritustapa === "opas" ||
                YleinenData.isPerusopetus(peruste) ||
                YleinenData.isAipe(peruste) ||
                YleinenData.isSimple(peruste) ||
                !_.isEmpty(peruste.suoritustavat)
            ) {
                try {
                    const result = await self.haeSisalto(peruste.id, stateParams.suoritustapa);
                    return result;
                } catch (virhe) {
                    Notifikaatiot.serverCb(virhe);
                    throw virhe;
                }
            }
        };

        deferred.resolve(this);
        return deferred.promise;
    })
    .service("PerusteprojektiOikeudetService", function(
        $rootScope,
        $stateParams,
        PerusteprojektiOikeudet,
        PerusteprojektiTiedotService
    ) {
        var oikeudet;
        var projektiId = null;
        var projektiTila = null;

        function noudaOikeudet(stateParams) {
            var vastaus = PerusteprojektiOikeudet.get({ id: stateParams.perusteProjektiId }, function(vastaus) {
                oikeudet = vastaus;
            });

            return vastaus.$promise;
        }

        function getOikeudet() {
            return _.clone(oikeudet);
        }

        function onkoOikeudet(target, permission) {
            if (oikeudet) {
                if (_.contains(oikeudet[target], permission)) {
                    return true;
                }
            }
            return false;
        }

        $rootScope.$on("$stateChangeSuccess", function() {
            PerusteprojektiTiedotService.then(function(res) {
                var projekti = res.getProjekti();
                if (projektiId && projektiId === projekti.id && projektiTila !== projekti.tila) {
                    noudaOikeudet($stateParams);
                } else {
                    projektiId = projekti.id;
                    projektiTila = projekti.tila;
                }
            });
        });

        return {
            noudaOikeudet,
            getOikeudet,
            onkoOikeudet,
        };
    });

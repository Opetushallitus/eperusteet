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
    .service("TekstikappaleOperations", function(
        YleinenData,
        PerusteenOsaViitteet,
        Editointikontrollit,
        Notifikaatiot,
        $state,
        SuoritustapaSisalto,
        TutkinnonOsaEditMode,
        PerusopetusService,
        $stateParams,
        LukiokoulutusService
    ) {
        var peruste = null;
        var deleteDone = false;

        this.setPeruste = function(value) {
            peruste = value;
        };

        function goToView(response, id = response.id) {
            var params = {
                perusteenOsaViiteId: id,
                versio: ""
            };
            $state.go("root.perusteprojekti.suoritustapa.tekstikappale", params, { reload: true });
        }

        this.add = function() {
            if (YleinenData.isPerusopetus(peruste)) {
                PerusopetusService.saveOsa(
                    {},
                    {
                        osanTyyppi: "tekstikappale"
                    },
                    response => {
                        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                        goToView(response);
                    }
                );
            } else if (YleinenData.isLukiokoulutus(peruste)) {
                LukiokoulutusService.saveOsa(
                    {},
                    {
                        osanTyyppi: "tekstikappale"
                    },
                    response => {
                        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                        goToView(response);
                    }
                );
            }
        };

        this.wasDeleted = function() {
            var ret = deleteDone;
            deleteDone = false;
            return ret;
        };
        this.noDeleteWasDoneYet = function() {
            deleteDone = false;
        };

        this.delete = function(viiteId, isNew, then?) {
            function commonCb(tyyppi) {
                deleteDone = true;
                if (isNew !== true) {
                    Editointikontrollit.cancelEditing();
                    Notifikaatiot.onnistui("poisto-onnistui");
                }
                $state.go("root.perusteprojekti.suoritustapa." + tyyppi, {}, { reload: true });
            }

            let successCb;
            if (peruste && peruste.tyyppi === "opas") {
                successCb = _.partial(commonCb, "opassisalto");
            } else {
                successCb = _.partial(commonCb, YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi].sisaltoTunniste);
            }
            if (YleinenData.isPerusopetus(peruste)) {
                PerusopetusService.deleteOsa({ $url: "dummy", id: viiteId }, successCb, Notifikaatiot.serverCb);
            } else if (YleinenData.isLukiokoulutus(peruste)) {
                LukiokoulutusService.deleteOsa({ $url: "dummy", id: viiteId }, successCb, Notifikaatiot.serverCb);
            } else {
                PerusteenOsaViitteet.delete({ viiteId: viiteId }, {}, successCb, Notifikaatiot.serverCb);
            }
        };

        this.addChild = function(viiteId, suoritustapa) {
            SuoritustapaSisalto.addChild(
                {
                    perusteId: peruste.id,
                    suoritustapa: suoritustapa,
                    perusteenosaViiteId: viiteId
                },
                {},
                function(response) {
                    TutkinnonOsaEditMode.setMode(true);
                    goToView(response);
                },
                Notifikaatiot.varoitus
            );
        };

        this.clone = function(viiteId) {
            if (YleinenData.isPerusopetus(peruste) || YleinenData.isLukiokoulutus(peruste)) {
            } else {
                PerusteenOsaViitteet.kloonaaTekstikappale(
                    {
                        perusteId: peruste.id,
                        suoritustapa: $stateParams.suoritustapa,
                        viiteId: viiteId
                    },
                    function(tk) {
                        TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                        Notifikaatiot.onnistui("tekstikappale-kopioitu-onnistuneesti");
                        goToView(tk, tk.id);
                    }
                );
            }
        };

        function mapSisalto(root) {
            return {
                id: root.id,
                perusteenOsa: null,
                lapset: _.map(root.lapset, mapSisalto)
            };
        }

        this.updateViitteet = function(sisalto, successCb) {
            var success = successCb || angular.noop;
            var mapped = mapSisalto(sisalto);

            if (YleinenData.isPerusopetus(peruste)) {
                PerusopetusService.updateSisaltoViitteet(sisalto, mapped, successCb);
            } else if (YleinenData.isLukiokoulutus(peruste)) {
                LukiokoulutusService.updateSisaltoViitteet(sisalto, mapped, successCb);
            } else {
                PerusteenOsaViitteet.update(
                    {
                        viiteId: sisalto.id
                    },
                    mapped,
                    success,
                    Notifikaatiot.serverCb
                );
            }
        };
    })
    .controller("muokkausTekstikappaleCtrl", async function(
        $location,
        $q,
        $rootScope,
        $scope,
        $state,
        $stateParams,
        Editointikontrollit,
        Kaanna,
        Kommentit,
        KommentitByPerusteenOsa,
        Lukitus,
        MuutProjektitService,
        Notifikaatiot,
        PerusteProjektiSivunavi,
        PerusteenOsanTyoryhmat,
        PerusteenOsat,
        PerusteprojektiTiedotService,
        PerusteprojektiTyoryhmat,
        ProjektinMurupolkuService,
        TEXT_HIERARCHY_MAX_DEPTH,
        TekstikappaleOperations,
        TutkinnonOsaEditMode,
        Tyoryhmat,
        Utils,
        Varmistusdialogi,
        VersionHelper,
        YleinenData,
        perusteprojektiBackLink,
        virheService
    ) {
        $scope.tekstikappale = {};
        $scope.versiot = {};
        const pts = await PerusteprojektiTiedotService;
        $scope.peruste = pts.getPeruste();

        $scope.sisalto = {};
        $scope.viitteet = {};
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

        var tekstikappaleDefer = $q.defer();
        $scope.tekstikappalePromise = tekstikappaleDefer.promise;

        $scope.valitseOsaamisala = function(oa) {
            $scope.editableTekstikappale.osaamisala = oa;
        };

        $scope.kopioiMuokattavaksi = function() {
            Varmistusdialogi.dialogi({
                otsikko: "kopioidaanko-tekstikappale",
                primaryBtn: "kopioi",
                successCb: () => {
                    TekstikappaleOperations.clone($scope.viitteet[$scope.tekstikappale.id].viite);
                }
            })();
        };

        $scope.muokkaa = async () => {
            await MuutProjektitService.varmistusdialogi($scope.tekstikappale.id);
            Editointikontrollit.startEditing(await lukitse());
        };

        $scope.canAddLapsi = function() {
            return (
                $scope.tekstikappale.id &&
                $scope.viitteet[$scope.tekstikappale.id] &&
                $scope.viitteet[$scope.tekstikappale.id].level < TEXT_HIERARCHY_MAX_DEPTH - 1
            );
        };

        $scope.addLapsi = function() {
            TekstikappaleOperations.addChild($scope.viiteId(), $stateParams.suoritustapa);
        };

        $scope.$watch("editEnabled", function(editEnabled) {
            PerusteProjektiSivunavi.setVisible(!editEnabled);
        });

        $scope.fields = [
            {
                path: "nimi",
                hideHeader: false,
                localeKey: "teksikappaleen-nimi",
                type: "editor-header",
                localized: true,
                mandatory: true,
                mandatoryMessage: "mandatory-otsikkoa-ei-asetettu",
                order: 1
            },
            {
                path: "teksti",
                hideHeader: false,
                localeKey: "tekstikappaleen-teksti",
                type: "editor-area",
                localized: true,
                mandatory: false,
                order: 2
            }
        ];

        $scope.poistaTyoryhma = function(tr) {
            Varmistusdialogi.dialogi({
                successCb: function() {
                    var uusi = _.remove(_.clone($scope.tyoryhmat), function(vanha) {
                        return vanha !== tr;
                    });
                    paivitaRyhmat(uusi, function() {
                        $scope.tyoryhmat = uusi;
                    });
                },
                otsikko: "poista-tyoryhma-perusteenosasta",
                teksti: Kaanna.kaanna("poista-tyoryhma-teksti", { nimi: tr })
            })();
        };

        $scope.lisaaTyoryhma = function() {
            Tyoryhmat.valitse(_.clone($scope.kaikkiTyoryhmat), _.clone($scope.tyoryhmat), function(uudet) {
                var uusi = _.clone($scope.tyoryhmat).concat(uudet);
                paivitaRyhmat(uusi, function() {
                    $scope.tyoryhmat = uusi;
                });
            });
        };

        $scope.tree = {
            init: function() {
                updateViitteet();
            },
            get: function() {
                var items: any = [];
                var id = $scope.tekstikappale.id;
                if ($scope.viitteet[id]) {
                    do {
                        items.push({
                            label: $scope.viitteet[id].nimi,
                            url:
                                $scope.tekstikappale.id === id
                                    ? null
                                    : $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                                          perusteenOsaViiteId: $scope.viitteet[id].viite,
                                          versio: ""
                                      })
                        });
                        id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
                    } while (id);
                }
                items.reverse();
                return items.length > 1 ? items : [];
            }
        };

        $scope.vaihdaVersio = function() {
            $scope.versiot.hasChanged = true;
            VersionHelper.setUrl($scope.versiot);
            //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
        };

        $scope.revertCb = function(response) {
            responseFn(response);
            saveCb(response);
        };

        $scope.poista = function() {
            var nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

            Varmistusdialogi.dialogi({
                successCb: doDelete,
                otsikko: "poista-tekstikappale-otsikko",
                teksti: Kaanna.kaanna("poista-tekstikappale-teksti", { nimi: nimi })
            })();
        };

        async function successCb(re) {
            if (re.osanTyyppi !== "tekstikappale") {
                $location.path(perusteprojektiBackLink.substring(1));
            }

            $scope.tekstikappale = re;
            setupTekstikappale($scope.tekstikappale);
            tekstikappaleDefer.resolve($scope.tekstikappale);
            if (TutkinnonOsaEditMode.getMode()) {
                $scope.isNew = true;
                $scope.muokkaa();
            }
            $scope.kaytossaMonessaProjektissa = _.size(await MuutProjektitService.projektitJoissaKaytossa(re.id)) > 1;
        }

        function errorCb() {
            virheService.virhe("virhe-tekstikappaletta-ei-löytynyt");
        }

        var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
        if (versio) {
            VersionHelper.getPerusteenOsaVersionsByViite(
                $scope.versiot,
                { id: $stateParams.perusteenOsaViiteId },
                true,
                function() {
                    var revNumber = VersionHelper.select($scope.versiot, versio);
                    if (!revNumber) {
                        errorCb();
                    } else {
                        PerusteenOsat.getVersioByViite(
                            {
                                viiteId: $stateParams.perusteenOsaViiteId,
                                versioId: revNumber
                            },
                            successCb,
                            errorCb
                        );
                    }
                }
            );
        } else {
            PerusteenOsat.getByViite({ viiteId: $stateParams.perusteenOsaViiteId }, successCb, errorCb);
        }

        TekstikappaleOperations.setPeruste($scope.$parent.peruste);
        $scope.kaikkiTyoryhmat = [];

        function paivitaRyhmat(uudet, cb) {
            PerusteenOsanTyoryhmat.save(
                {
                    projektiId: $stateParams.perusteProjektiId,
                    osaId: $scope.tekstikappale.id
                },
                uudet,
                cb,
                Notifikaatiot.serverCb
            );
        }

        Utils.scrollTo("#ylasivuankkuri");
        Kommentit.haeKommentit(KommentitByPerusteenOsa, {
            id: $stateParams.perusteProjektiId,
            perusteenOsaId: $stateParams.perusteenOsaViiteId
        });

        async function haeSisalto() {
            if ($scope.tiedotService) {
                const res = await $scope.tiedotService.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa);
                $scope.sisalto = res;
                setNavigation();
            }
        }

        if (
            $stateParams.suoritustapa ||
            YleinenData.isPerusopetus($scope.$parent.peruste) ||
            YleinenData.isLukiokoulutus($scope.$parent.peruste) ||
            YleinenData.isAipe($scope.$parent.peruste)
        ) {
            const instance = await PerusteprojektiTiedotService;
            $scope.tiedotService = instance;
            await haeSisalto();
        }

        function lukitse() {
            return $q(resolve => {
                Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, resolve);
            });
        }

        function fetch(cb) {
            PerusteenOsat.get(
                { osanId: $scope.tekstikappale.id },
                (_ as any).setWithCallback($scope, "tekstikappale", cb)
            );
        }

        async function setNavigation() {
            $scope.tree.init();
            ProjektinMurupolkuService.setCustom($scope.tree.get());
            VersionHelper.setUrl($scope.versiot);
        }

        $scope.viiteId = function() {
            return $scope.viitteet[$scope.tekstikappale.id] ? $scope.viitteet[$scope.tekstikappale.id].viite : null;
        };

        async function storeTree(sisalto, level = 0) {
            sisalto = await sisalto;
            _.each(sisalto.lapset, function(lapsi) {
                if (lapsi.perusteenOsa) {
                    if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
                        $scope.viitteet[lapsi.perusteenOsa.id] = {};
                    }
                    $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
                    $scope.viitteet[lapsi.perusteenOsa.id].level = level;
                    $scope.viitteet[lapsi.perusteenOsa.id].nimi = lapsi.perusteenOsa.nimi;
                    if (sisalto.perusteenOsa) {
                        $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
                    }
                    storeTree(lapsi, level + 1);
                }
            });
        }

        function updateViitteet() {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
        }

        function refreshPromise() {
            $scope.editableTekstikappale = angular.copy($scope.tekstikappale);
            tekstikappaleDefer = $q.defer();
            $scope.tekstikappalePromise = tekstikappaleDefer.promise;
            tekstikappaleDefer.resolve($scope.editableTekstikappale);
        }

        async function saveCb(res) {
            // Päivitä versiot
            const versiot = await haeVersiot(true);
            VersionHelper.setUrl($scope.versiot);
            PerusteProjektiSivunavi.refresh();
            Lukitus.vapautaPerusteenosa(res.id);
            Notifikaatiot.onnistui("muokkaus-tekstikappale-tallennettu");
            await haeSisalto();
        }

        function doDelete(isNew) {
            TekstikappaleOperations.delete($scope.viiteId(), isNew);
        }

        async function setupTekstikappale(kappale) {
            try {
                const data = await $q.all([
                    PerusteenOsanTyoryhmat.get({
                        projektiId: $stateParams.perusteProjektiId,
                        osaId: $scope.tekstikappale.id
                    }).$promise,
                    PerusteprojektiTyoryhmat.get({ id: $stateParams.perusteProjektiId }).$promise
                ]);
                $scope.tyoryhmat = data[0];
                $scope.kaikkiTyoryhmat = _.unique(_.map(data[1], "nimi"));
            } catch (error) {
                Notifikaatiot.serverCb(error);
            }

            $scope.editableTekstikappale = angular.copy(kappale);

            Editointikontrollit.registerCallback({
                edit: () => {
                    return $q((resolve, reject) => {
                        TekstikappaleOperations.noDeleteWasDoneYet();
                        lukitse().then(() => {
                            fetch(function() {
                                refreshPromise();
                                resolve();
                            });
                        });
                    });
                },
                save: kommentti => {
                    return $q((resolve, reject) => {
                        $scope.editableTekstikappale.metadata = { kommentti: kommentti };
                        PerusteenOsat.saveTekstikappale(
                            {
                                osanId: $scope.editableTekstikappale.id
                            },
                            $scope.editableTekstikappale,
                            function(res) {
                                saveCb(res);
                                $scope.tekstikappale = angular.copy($scope.editableTekstikappale);
                                $scope.isNew = false;
                                resolve();
                            },
                            Notifikaatiot.serverCb
                        );
                    });
                },
                cancel: () => {
                    return $q((resolve, reject) => {
                        if (!TekstikappaleOperations.wasDeleted()) {
                            Lukitus.vapautaPerusteenosa($scope.tekstikappale.id, function() {
                                if ($scope.isNew) {
                                    doDelete(true);
                                } else {
                                    fetch(function() {
                                        refreshPromise();
                                    });
                                }
                                $scope.isNew = false;
                            });
                        }
                        resolve();
                    });
                },
                notify: mode => {
                    $scope.editEnabled = mode;
                },
                validate: mandatoryValidator => {
                    return mandatoryValidator($scope.fields, $scope.editableTekstikappale);
                }
            });

            await haeVersiot();
            await setNavigation();
            Lukitus.tarkista($scope.tekstikappale.id, $scope);
        }
        async function haeVersiot(force?) {
            return VersionHelper.getPerusteenosaVersions($scope.versiot, { id: $scope.tekstikappale.id }, force);
        }

        function responseFn(response) {
            $scope.tekstikappale = response;
            setupTekstikappale(response);
            tekstikappaleDefer = $q.defer();
            $scope.tekstikappalePromise = tekstikappaleDefer.promise;
            tekstikappaleDefer.resolve($scope.editableTekstikappale);
            VersionHelper.setUrl($scope.versiot);
        }

        // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
        var received = 0;
        $scope.$on("ckEditorInstanceReady", function() {
            if (++received === $scope.fields.length) {
                $rootScope.$broadcast("editointikontrollitRefresh");
            }
        });
    } as any);

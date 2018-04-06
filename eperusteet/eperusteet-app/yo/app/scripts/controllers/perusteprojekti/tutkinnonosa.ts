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
    .factory("TutkinnonOsanKoodiUniqueResource", ($resource, SERVICE_LOC) => {
        return $resource(SERVICE_LOC + "/tutkinnonosat/koodi/uniikki/:tutkinnonosakoodi");
    })
    .service("TutkinnonosanTiedotService", (PerusteenOsat, $q, TutkinnonOsanOsaAlue, Osaamistavoite, YleinenData) => {
        const FIELD_ORDER = {
            tavoitteet: 3,
            ammattitaitovaatimukset: 4,
            ammattitaitovaatimuksetLista: 4,
            ammattitaidonOsoittamistavat: 7,
            arviointi: 5,
            lisatiedot: 5,
            arvioinninKohdealueet: 6
        };

        let tutkinnonOsa;

        function noudaTutkinnonOsa(stateParams) {
            const deferred = $q.defer();

            PerusteenOsat.get({ osanId: stateParams.perusteenOsaId }, function(vastaus) {
                tutkinnonOsa = vastaus;
                if (_.includes(YleinenData.yhteisetTutkinnonOsat, vastaus.tyyppi)) {
                    TutkinnonOsanOsaAlue.list({ osanId: stateParams.perusteenOsaId }, function(osaAlueet) {
                        tutkinnonOsa.osaAlueet = osaAlueet;

                        if (osaAlueet && osaAlueet.length > 0) {
                            const promisesList = [];
                            _.each(osaAlueet, function(osaAlue) {
                                const valmis = Osaamistavoite.list(
                                    {
                                        osanId: stateParams.perusteenOsaId,
                                        osaalueenId: osaAlue.id
                                    },
                                    function(osaamistavoitteet) {
                                        osaAlue.osaamistavoitteet = osaamistavoitteet;
                                    }
                                );
                                promisesList.push(valmis.promise);
                            });
                            $q.all(promisesList).then(
                                function() {
                                    deferred.resolve();
                                },
                                function() {
                                    deferred.reject();
                                }
                            );
                        } else {
                            deferred.resolve();
                        }
                    });
                } else {
                    deferred.resolve();
                }
            });

            return deferred.promise;
        }

        function getTutkinnonOsa() {
            return _.clone(tutkinnonOsa);
        }

        return {
            noudaTutkinnonOsa: noudaTutkinnonOsa,
            getTutkinnonOsa: getTutkinnonOsa,
            order: function(key) {
                return FIELD_ORDER[key] || -1;
            },
            keys: function() {
                return _.keys(FIELD_ORDER);
            }
        };
    })
    .controller(
        "muokkausTutkinnonosaCtrl",
        (
            $scope,
            $state,
            $stateParams,
            $rootScope,
            $q,
            Editointikontrollit,
            PerusteenOsat,
            PerusteenRakenne,
            PerusteTutkinnonosa,
            TutkinnonOsaEditMode,
            $timeout,
            Varmistusdialogi,
            VersionHelper,
            Lukitus,
            MuokkausUtils,
            PerusteenOsaViitteet,
            Utils,
            ArviointiHelper,
            AmmattitaitoHelper,
            PerusteProjektiSivunavi,
            Notifikaatiot,
            Koodisto,
            Tutke2OsaData,
            Kommentit,
            KommentitByPerusteenOsa,
            FieldSplitter,
            Algoritmit,
            TutkinnonosanTiedotService,
            TutkinnonOsaViitteet,
            PerusteenOsaViite,
            virheService,
            ProjektinMurupolkuService,
            localStorageService,
            TutkinnonOsaLeikelautaService,
            MuutProjektitService,
            YleinenData
        ) => {
            Utils.scrollTo("#ylasivuankkuri");

            Kommentit.haeKommentit(KommentitByPerusteenOsa, {
                id: $stateParams.perusteProjektiId,
                perusteenOsaId: $stateParams.tutkinnonOsaViiteId
            });

            $scope.tutkinnonOsaViite = {};
            $scope.versiot = {};

            $scope.suoritustapa = $stateParams.suoritustapa;
            $scope.rakenne = {};
            $scope.test = angular.noop;
            $scope.menuItems = [];
            $scope.editableTutkinnonOsaViite = {};
            $scope.editEnabled = false;
            $scope.editointikontrollit = Editointikontrollit;
            $scope.nimiValidationError = false;

            $scope.isLeikelautaOpen = false;
            if (localStorageService.isSupported) {
                $scope.isLeikelautaOpen = localStorageService.get("leikeautaOpen");
            }
            $scope.toggleLeikelauta = () => {
                if (localStorageService.isSupported) {
                    $scope.isLeikelautaOpen = !$scope.isLeikelautaOpen;
                    localStorageService.set("leikeautaOpen", $scope.isLeikelautaOpen);
                } else {
                    Notifikaatiot.varoitus("selain-ei-tue");
                }
            };

            $scope.isTutke2 = YleinenData.isTutke2;
            $scope.isReformi =
                _.find($scope.peruste.suoritustavat, (st: any) => st.suoritustapakoodi === "reformi") != null;

            let tutkinnonOsaDefer = $q.defer();
            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

            async function successCb(re) {
                setupTutkinnonOsaViite(re);
                tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
                $scope.kaytossaMonessaProjektissa =
                    _.size(
                        await MuutProjektitService.projektitJoissaKaytossa(
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.id
                        )
                    ) > 1;
            }

            function errorCb() {
                virheService.virhe("virhe-tutkinnonosaa-ei-löytynyt");
            }

            const versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
            if (versio) {
                VersionHelper.getTutkinnonOsaViiteVersions(
                    $scope.versiot,
                    { id: $stateParams.tutkinnonOsaViiteId },
                    true,
                    function() {
                        const revNumber = VersionHelper.select($scope.versiot, versio);
                        if (!revNumber) {
                            errorCb();
                        } else {
                            TutkinnonOsaViitteet.getVersio(
                                {
                                    viiteId: $stateParams.tutkinnonOsaViiteId,
                                    versioId: revNumber
                                },
                                successCb,
                                errorCb
                            );
                        }
                    }
                );
            } else {
                PerusteenOsaViite.get(
                    {
                        perusteId: $scope.peruste.id,
                        suoritustapa: $stateParams.suoritustapa,
                        viiteId: $stateParams.tutkinnonOsaViiteId
                    },
                    successCb,
                    errorCb
                );
            }

            $scope.osaAlueAlitila = $state.includes("**.tutkinnonosa.osaalue");

            $rootScope.$on("$stateChangeStart", function(event, toState) {
                $scope.osaAlueAlitila = toState.name === "root.perusteprojekti.suoritustapa.tutkinnonosa.osaalue";
            });

            $scope.$watch(
                "editableTutkinnonOsa.nimi",
                function() {
                    $scope.nimiValidationError = false;
                },
                true
            );

            $scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko($scope.peruste, $scope.suoritustapa);

            async function getRakenne() {
                $scope.rakenne = await PerusteenRakenne.haeByPerusteprojekti(
                    $stateParams.perusteProjektiId,
                    $stateParams.suoritustapa
                );
                if (TutkinnonOsaEditMode.getMode()) {
                    $scope.isNew = true;
                    $scope.muokkaa();
                }
            }

            getRakenne();

            function lukitse(cb) {
                Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, cb);
            }

            function fetch(cb) {
                cb = cb || angular.noop;
                PerusteenOsat.get({ osanId: $scope.tutkinnonOsaViite.tutkinnonOsa.id }, res => {
                    $scope.tutkinnonOsaViite.tutkinnonOsa = res;
                    cb(res);
                });
            }

            $scope.removeByIdx = (vapaatTekstit, idx) => {
                vapaatTekstit.splice(idx, 1);
            };

            $scope.removeVapaaTeksti = function(vapaatTekstit, sisalto) {
                _.remove(vapaatTekstit, sisalto);
            };

            $scope.addVapaaTeksti = vapaatTekstit => {
                vapaatTekstit.push({
                    nimi: {},
                    teksti: {}
                });
                Utils.scrollTo("#vapaatTekstit");
            };

            $scope.sortableOptions = TutkinnonOsaLeikelautaService.createConnectedSortable({
                connectWith: ".container-items, .container-items-leikelauta",
                handle: ".handle",
                cursorAt: { top: 10, left: 10 }
            });

            $scope.fields = [
                {
                    path: "tutkinnonOsa.tavoitteet",
                    localeKey: "tutkinnon-osan-tavoitteet",
                    type: "editor-area",
                    localized: true,
                    collapsible: true
                },
                {
                    path: "tutkinnonOsa.ammattitaitovaatimukset",
                    localeKey: "tutkinnon-osan-ammattitaitovaatimukset-teksti",
                    type: "editor-area",
                    localized: true,
                    collapsible: true
                },
                {
                    path: "tutkinnonOsa.ammattitaitovaatimuksetLista",
                    localeKey: "tutkinnon-osan-ammattitaitovaatimukset-taulukko",
                    type: "ammattitaito",
                    collapsible: true
                },
                {
                    path: "tutkinnonOsa.ammattitaidonOsoittamistavat",
                    localeKey: "tutkinnon-osan-ammattitaidon-osoittamistavat",
                    type: "editor-area",
                    localized: true,
                    collapsible: true
                },
                {
                    path: "tutkinnonOsa.arviointi.lisatiedot",
                    localeKey: "tutkinnon-osan-arviointi-teksti",
                    type: "editor-area",
                    localized: true,
                    collapsible: true
                },
                {
                    path: "tutkinnonOsa.arviointi.arvioinninKohdealueet",
                    localeKey: "tutkinnon-osan-arviointi-taulukko",
                    type: "arviointi",
                    collapsible: true
                }
            ];

            _.each($scope.fields, field => {
                field.order = TutkinnonosanTiedotService.order(_.last(field.path.split(".")));
            });

            $scope.koodistoClick = Koodisto.modaali(
                koodisto => {
                    if (koodisto != null && koodisto.koodisto != null) {
                        $scope.editableTutkinnonOsaViite.tutkinnonOsa.koodi = {
                            uri: koodisto.koodiUri,
                            arvo: koodisto.koodiArvo,
                            versio: koodisto.versio,
                            koodisto: koodisto.koodisto.koodistoUri
                        };
                    }
                },
                {
                    tyyppi: () => {
                        return "tutkinnonosat";
                    },
                    ylarelaatioTyyppi: () => {
                        return "";
                    },
                    tarkista: _.constant(true)
                }
            );

            $scope.cleanKoodi = () => {
                $scope.editableTutkinnonOsaViite.tutkinnonOsa.koodi = null;
                $scope.editableTutkinnonOsaViite.tutkinnonOsa.koodiUri = null;
                $scope.editableTutkinnonOsaViite.tutkinnonOsa.koodiArvo = null;
            };

            $scope.kopioiMuokattavaksi = async () => {
                Varmistusdialogi.dialogi({
                    otsikko: "kopioidaanko-tekstikappale",
                    primaryBtn: "kopioi",
                    successCb: () => {
                        PerusteenOsaViitteet.kloonaaTutkinnonOsa(
                            {
                                viiteId: $scope.tutkinnonOsaViite.id
                            },
                            tk => {
                                TutkinnonOsaEditMode.setMode(true);
                                Notifikaatiot.onnistui("tutkinnonosa-kopioitu-onnistuneesti");
                                $state.go(
                                    "root.perusteprojekti.suoritustapa.tutkinnonosa",
                                    {
                                        perusteenOsaViiteId: tk.id,
                                        versio: ""
                                    },
                                    {
                                        reload: true
                                    }
                                );
                            }
                        );
                    }
                })();
            };

            function refreshPromise() {
                $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus = $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus || {};
                $scope.editableTutkinnonOsaViite = angular.copy($scope.tutkinnonOsaViite);
                tutkinnonOsaDefer = $q.defer();
                $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
            }

            function saveCb(res) {
                Lukitus.vapautaPerusteenosa(res.id);
                ProjektinMurupolkuService.set(
                    "tutkinnonOsaViiteId",
                    $scope.tutkinnonOsaViite.id,
                    $scope.tutkinnonOsaViite.tutkinnonOsa.nimi
                );
                Notifikaatiot.onnistui("muokkaus-tutkinnon-osa-tallennettu");
                $scope.haeVersiot(true, () => {
                    VersionHelper.setUrl($scope.versiot);
                });
                tutke2.fetch();
            }

            function doDelete(osaId) {
                PerusteenRakenne.poistaTutkinnonOsaViite(
                    osaId,
                    $scope.peruste.id,
                    $stateParams.suoritustapa,
                    function() {
                        Notifikaatiot.onnistui("tutkinnon-osa-rakenteesta-poistettu");
                        $state.go("root.perusteprojekti.suoritustapa.tutkinnonosat");
                    }
                );
            }

            const tutke2 = {
                fetch: () => {
                    if (
                        _.includes(
                            YleinenData.yhteisetTutkinnonOsat,
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi
                        )
                    ) {
                        if (Tutke2OsaData.get()) {
                            Tutke2OsaData.get().fetch();
                        }
                    }
                },
                mergeOsaAlueet: tutkinnonOsa => {
                    if (
                        _.includes(
                            YleinenData.yhteisetTutkinnonOsat,
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi
                        )
                    ) {
                        tutkinnonOsa.osaAlueet = _.map(Tutke2OsaData.get().$editing, (osaAlue: any) => {
                            const item: any = { nimi: osaAlue.nimi };
                            if (osaAlue.id) {
                                item.id = osaAlue.id;
                            }
                            return item;
                        });
                    }
                },
                validate: () => {
                    if (
                        _.includes(
                            YleinenData.yhteisetTutkinnonOsat,
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi
                        )
                    ) {
                        return _.all(
                            _.map(Tutke2OsaData.get().$editing, (item: any) => {
                                return Utils.hasLocalizedText(item.nimi);
                            })
                        );
                    } else {
                        return true;
                    }
                }
            };

            const normalCallbacks = {
                edit: () => {
                    tutke2.fetch();
                },
                asyncValidate: cb => {
                    lukitse(() => {
                        cb();
                    });
                },
                save: kommentti => {
                    tutke2.mergeOsaAlueet($scope.editableTutkinnonOsaViite.tutkinnonOsa);
                    $scope.editableTutkinnonOsaViite.metadata = { kommentti: kommentti };
                    if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.id) {
                        PerusteTutkinnonosa.save(
                            {
                                perusteId: $scope.peruste.id,
                                suoritustapa: $stateParams.suoritustapa,
                                osanId: $scope.editableTutkinnonOsaViite.tutkinnonOsa.id
                            },
                            $scope.editableTutkinnonOsaViite,
                            res => {
                                $scope.editableTutkinnonOsaViite = angular.copy(res);
                                $scope.tutkinnonOsaViite = angular.copy(res);
                                Editointikontrollit.lastModified = res;
                                saveCb(res.tutkinnonOsa);
                                getRakenne();

                                tutkinnonOsaDefer = $q.defer();
                                $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                                tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
                            },
                            Notifikaatiot.serverCb
                        );
                    } else {
                        PerusteenOsat.saveTutkinnonOsa(
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa,
                            res => {
                                Editointikontrollit.lastModified = res;
                                saveCb(res);
                                getRakenne();
                            },
                            Notifikaatiot.serverCb
                        );
                    }
                    $scope.isNew = false;
                },
                cancel: () => {
                    if ($scope.isNew) {
                        doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id].id);
                        $scope.isNew = false;
                    } else {
                        tutke2.fetch();
                        fetch(() => {
                            refreshPromise();
                            Lukitus.vapautaPerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id);
                        });
                    }
                },
                notify: mode => {
                    $scope.editEnabled = mode;
                },
                validate: () => {
                    if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
                        $scope.nimiValidationError = true;
                    }
                    return $scope.tutkinnonOsaHeaderForm.$valid && tutke2.validate();
                }
            };

            function setupTutkinnonOsaViite(viite) {
                $scope.tutkinnonOsaViite = viite;
                ProjektinMurupolkuService.set(
                    "tutkinnonOsaViiteId",
                    $scope.tutkinnonOsaViite.id,
                    $scope.tutkinnonOsaViite.tutkinnonOsa.nimi
                );
                $scope.editableTutkinnonOsaViite = angular.copy(viite);
                $scope.isNew = !$scope.editableTutkinnonOsaViite.tutkinnonOsa.id;
                if ($state.current.name === "root.perusteprojekti.suoritustapa.tutkinnonosa") {
                    Editointikontrollit.registerCallback(normalCallbacks);
                }
                $scope.haeVersiot();
                Lukitus.tarkista($scope.tutkinnonOsaViite.tutkinnonOsa.id, $scope);
            }

            $scope.poistaTutkinnonOsa = function(osaId) {
                const onRakenteessa = PerusteenRakenne.validoiRakennetta($scope.rakenne.rakenne, osa => {
                    return (
                        osa._tutkinnonOsaViite &&
                        $scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].id === osaId
                    );
                });
                if (onRakenteessa) {
                    Notifikaatiot.varoitus("tutkinnon-osa-rakenteessa-ei-voi-poistaa");
                } else {
                    Varmistusdialogi.dialogi({
                        otsikko: "poistetaanko-tutkinnonosa",
                        primaryBtn: "poista",
                        successCb: () => {
                            $scope.isNew = false;
                            Editointikontrollit.cancelEditing();
                            doDelete(osaId);
                        }
                    })();
                }
            };

            $scope.muokkaa = async () => {
                await MuutProjektitService.varmistusdialogi($scope.editableTutkinnonOsaViite.tutkinnonOsa.id);
                Editointikontrollit.registerCallback(normalCallbacks);
                lukitse(() => {
                    fetch(() => {
                        Editointikontrollit.startEditing();
                        refreshPromise();
                    });
                });
            };
            $scope.$watch("editEnabled", editEnabled => {
                PerusteProjektiSivunavi.setVisible(!editEnabled);
            });

            $scope.haeVersiot = (force, cb) => {
                VersionHelper.getTutkinnonOsaViiteVersions(
                    $scope.versiot,
                    { id: $scope.tutkinnonOsaViite.id },
                    force,
                    cb
                );
            };

            function responseFn(response) {
                $scope.tutkinnonOsaViite.tutkinnonOsa = response;
                setupTutkinnonOsaViite(response);
                const objDefer = $q.defer();
                $scope.tutkinnonOsaPromise = objDefer.promise;
                objDefer.resolve($scope.editableTutkinnonOsaViite);
                VersionHelper.setUrl($scope.versiot);
            }

            $scope.vaihdaVersio = () => {
                $scope.versiot.hasChanged = true;
                VersionHelper.setUrl($scope.versiot);
            };

            $scope.revertCb = res => {
                responseFn(res);
                saveCb(res);
            };

            $scope.addFieldToVisible = field => {
                field.visible = true;
                // Varmista että menu sulkeutuu klikin jälkeen
                $timeout(function() {
                    angular.element("h1").click();
                    // TODO ei toimi koska localeKey voi olla muu kuin string,
                    //     joku muu tapa yksilöidä/löytää juuri lisätty kenttä?
                    Utils.scrollTo("li." + FieldSplitter.getClass(field));
                });
            };

            // Palauttaa true jos kaikki mahdolliset osiot on jo lisätty
            $scope.allVisible = () => {
                const lisatty = _.all($scope.fields, (field: any) => {
                    return _.contains(field.path, "arviointi.") || !field.inMenu || (field.inMenu && field.visible);
                });
                return lisatty && $scope.arviointiHelper.exists();
            };

            $scope.updateMenu = () => {
                if (!$scope.arviointiHelper) {
                    $scope.arviointiHelper = ArviointiHelper.create();
                }
                if (!$scope.ammattitaitoHelper) {
                    $scope.ammattitaitoHelper = AmmattitaitoHelper.create();
                }

                $scope.arviointiFields = $scope.arviointiHelper.initFromFields($scope.fields);
                $scope.ammattitaitoHelper.initFromFields($scope.fields);

                $scope.menuItems = _.reject($scope.fields, "mandatory");
                if ($scope.arviointiHelper) {
                    $scope.arviointiHelper.setMenu($scope.menuItems);
                }
            };

            $scope.$watch("arviointiFields.teksti.visible", $scope.updateMenu);
            $scope.$watch("arviointiFields.taulukko.visible", $scope.updateMenu);
        }
    );

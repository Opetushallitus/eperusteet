/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
*
* This program is free software: Licensed under the EUPL
* Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
*
* This program is distributed in the hope that it will be usefu
* l
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

import * as angular from "angular";
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .controller("KoulutuksenOsaOsaAlueCtrl", function(
        $scope,
        $state,
        $stateParams,
        Editointikontrollit,
        TutkinnonOsanOsaAlue,
        Lukitus,
        Notifikaatiot,
        Utils
    ) {
        $scope.editEnabled = true;
        $scope.valma = {
            valmaarviointi: { kohde: {}, selite: {}, tavoitetteet: {} },
            valmatavoitteet: { kohde: {}, selite: {}, tavoitetteet: {} }
        };

        function goBack() {
            $state.go("^", {}, { reload: true });
        }

        var osaAlueCallbacks = {
            edit: function() {
                TutkinnonOsanOsaAlue.get(
                    {
                        viiteId: $stateParams.tutkinnonOsaViiteId,
                        osaalueenId: $stateParams.osaAlueId
                    },
                    function(vastaus) {
                        $scope.osaAlue = vastaus;
                        if (vastaus.valmaTelmaSisalto) {
                            $scope.valma = {
                                valmaarviointi: vastaus.valmaTelmaSisalto.osaamisenarviointi,
                                valmatavoitteet: vastaus.valmaTelmaSisalto.osaamistavoite
                            };
                        }

                        //luoOsaamistavoitepuu();
                    },
                    function(virhe) {
                        Notifikaatiot.serverCb(virhe);
                        goBack();
                    }
                );
            },
            cancel: function() {
                Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
                goBack();
            },
            save: function() {
                $scope.osaAlue.valmaTelmaSisalto = angular.copy($scope.valma);

                $scope.osaAlue.valmaTelmaSisalto = {
                    osaamistavoite: _.map($scope.valma.valmatavoitteet, function(item) {
                        return {
                            tavoitteet: _.map(item.tavoitteet, function(tavoite) {
                                return _.omit(tavoite, "jarjestys");
                            }),
                            kohde: item.kohde,
                            selite: item.selite
                        };
                    }),
                    osaamisenarviointi: {
                        tavoitteet: _.map($scope.valma.valmaarviointi.tavoitteet, function(tavoite) {
                            return _.omit(tavoite, "jarjestys");
                        }),
                        kohde: $scope.valma.valmaarviointi.kohde,
                        selite: $scope.valma.valmaarviointi.selite
                    }
                };

                TutkinnonOsanOsaAlue.save(
                    {
                        viiteId: $stateParams.tutkinnonOsaViiteId,
                        osaalueenId: $stateParams.osaAlueId
                    },
                    $scope.osaAlue,
                    function(res) {
                        Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
                        goBack();
                    },
                    function(virhe) {
                        Notifikaatiot.serverCb(virhe);
                        goBack();
                    }
                );
            },
            validate: function() {
                if (!Utils.hasLocalizedText($scope.osaAlue.nimi)) {
                    return false;
                } else {
                    return (
                        $scope.isVaTe ||
                        _.all($scope.osaamistavoitepuu, function(osaamistavoite) {
                            return Utils.hasLocalizedText(osaamistavoite.nimi);
                        })
                    );
                }
            }
        };

        function lukitse(cb) {
            Lukitus.lukitsePerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId, cb);
        }

        Editointikontrollit.registerCallback(osaAlueCallbacks);
        lukitse(function() {
            Editointikontrollit.startEditing();
        });
    });

angular
    .module("eperusteApp")
    .controller("muokkausKoulutuksenosaCtrl", function(
        $scope,
        Utils,
        Kommentit,
        $stateParams,
        KommentitByPerusteenOsa,
        $state,
        Editointikontrollit,
        VersionHelper,
        virheService,
        TutkinnonOsaViitteet,
        Algoritmit,
        rakenne,
        Lukitus,
        PerusteenOsaViite,
        $q,
        Tutke2Service,
        Notifikaatiot,
        PerusteenRakenne,
        TutkinnonOsaEditMode,
        PerusteTutkinnonosa,
        PerusteenOsat,
        ProjektinMurupolkuService,
        Tutke2OsaData,
        $timeout,
        FieldSplitter,
        Varmistusdialogi,
        Koodisto,
        MuokkausUtils,
        PerusteenOsaViitteet
    ) {
        Utils.scrollTo("#ylasivuankkuri");

        $scope.tutke2osa = [];
        $scope.tutkinnonOsa = {};
        $scope.osaAlueAlitila = $state.includes("**.koulutuksenosa.osaalue");

        $scope.fields = [
            {
                path: "tutkinnonOsa.valmaTelmaSisalto.osaamistavoite",
                localeKey: "koulutuksen-osan-osaamisentavoitteet",
                type: "valmaarviointi",
                collapsible: true,
                valmatype: "tavoite",
                visible: false
            },
            {
                path: "tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi",
                localeKey: "koulutuksen-osan-osaamisen-arviointi",
                type: "valmaarviointi",
                collapsible: true,
                valmatype: "arviointi",
                visible: false
            },
            {
                path: "tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointiTekstina",
                localeKey: "koulutuksen-osan-osaamisen-arviointi-tekstina",
                type: "editor-area",
                localized: true,
                collapsible: true,
                visible: false
            }
        ];

        $scope.koodistoClick = Koodisto.modaali(
            function(koodisto) {
                MuokkausUtils.nestedSet(
                    $scope.editableTutkinnonOsaViite.tutkinnonOsa,
                    "koodiUri",
                    ",",
                    koodisto.koodiUri
                );
                MuokkausUtils.nestedSet(
                    $scope.editableTutkinnonOsaViite.tutkinnonOsa,
                    "koodiArvo",
                    ",",
                    koodisto.koodiArvo
                );
            },
            {
                tyyppi: function() {
                    return "tutkinnonosat";
                },
                ylarelaatioTyyppi: function() {
                    return "";
                },
                tarkista: _.constant(true)
            }
        );

        function isVisible(fieldPath) {
            return _.find($scope.fields, { path: fieldPath }).visible;
        }

        $scope.addFieldToVisible = function(field) {
            field.visible = true;
            // Varmista että menu sulkeutuu klikin jälkeen

            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto = $scope.editableTutkinnonOsaViite
                .tutkinnonOsa.valmaTelmaSisalto
                ? $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto
                : {};

            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi = field.path.includes(
                "osaamisenarviointi"
            )
                ? {}
                : $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi;
            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite = field.path.includes(
                "osaamistavoite"
            )
                ? {}
                : $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite;

            $timeout(function() {
                angular.element("h1").click();
                // TODO ei toimi koska localeKey voi olla muu kuin string,
                //     joku muu tapa yksilöidä/löytää juuri lisätty kenttä?
                Utils.scrollTo("li." + FieldSplitter.getClass(field));
            });
        };

        function refreshPromise() {
            $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus = $scope.tutkinnonOsaViite.tutkinnonOsa.kuvaus || {};
            $scope.editableTutkinnonOsaViite = angular.copy($scope.tutkinnonOsaViite);
            tutkinnonOsaDefer = $q.defer();
            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
        }

        function doDelete(osaId) {
            PerusteenRakenne.poistaTutkinnonOsaViite(osaId, $scope.peruste.id, $stateParams.suoritustapa, function() {
                Notifikaatiot.onnistui("koulutuksen-osa-rakenteesta-poistettu");
                $state.go("root.perusteprojekti.suoritustapa.koulutuksenosat");
            });
        }

        $scope.poistaTutkinnonOsa = function(osaId) {
            var onRakenteessa = PerusteenRakenne.validoiRakennetta($scope.rakenne.rakenne, function(osa) {
                return (
                    osa._tutkinnonOsaViite && $scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].id === osaId
                );
            });
            if (onRakenteessa) {
                Notifikaatiot.varoitus("koulutuksen-osa-rakenteessa-ei-voi-poistaa");
            } else {
                Varmistusdialogi.dialogi({
                    otsikko: "poistetaanko-koulutuksenosa",
                    primaryBtn: "poista",
                    successCb: function() {
                        $scope.isNew = false;
                        Editointikontrollit.cancelEditing();
                        doDelete(osaId);
                    }
                })();
            }
        };

        $scope.kopioiMuokattavaksi = function() {
            PerusteenOsaViitteet.kloonaaTutkinnonOsa(
                {
                    viiteId: $scope.tutkinnonOsaViite.id
                },
                function(tk) {
                    TutkinnonOsaEditMode.setMode(true);
                    Notifikaatiot.onnistui("tutkinnonosa-kopioitu-onnistuneesti");
                    $state.go(
                        "root.perusteprojekti.suoritustapa.koulutuksenosa",
                        {
                            perusteenOsaViiteId: tk.id,
                            versio: ""
                        },
                        { reload: true }
                    );
                }
            );
        };

        function saveCb(res) {
            Lukitus.vapautaPerusteenosa(res.id);
            ProjektinMurupolkuService.set(
                "tutkinnonOsaViiteId",
                $scope.tutkinnonOsaViite.id,
                $scope.tutkinnonOsaViite.tutkinnonOsa.nimi
            );
            Notifikaatiot.onnistui("muokkaus-koulutuksen-osa-tallennettu");
            $scope.haeVersiot(true, function() {
                VersionHelper.setUrl($scope.versiot);
            });
            Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
        }

        function lukitse(cb) {
            Lukitus.lukitsePerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id, cb);
        }

        //var fetch = muokkausCtrlCommons.scoped($scope).fetch;
        var tutkinnonOsaDefer = $q.defer();
        $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;

        {
            // Defaults
            //muokkausCtrlCommons.setupScope($scope, doDelete);
            Kommentit.haeKommentit(KommentitByPerusteenOsa, {
                id: $stateParams.perusteProjektiId,
                perusteenOsaId: $stateParams.tutkinnonOsaViiteId
            });
            $scope.$laajuusRangena = false;
            $scope.toggleLaajuusRange = function() {
                $scope.$laajuusRangena = !$scope.$laajuusRangena;
            };
            $scope.tutkinnonOsaViite = {};
            $scope.versiot = {};
            $scope.suoritustapa = $stateParams.suoritustapa;
            $scope.rakenne = rakenne;
            $scope.test = angular.noop;
            $scope.editableTutkinnonOsaViite = {};
            $scope.editEnabled = false;
            $scope.editointikontrollit = Editointikontrollit;
            $scope.nimiValidationError = false;
            $scope.yksikko = Algoritmit.perusteenSuoritustavanYksikko($scope.peruste, $scope.suoritustapa);

            $scope.getTosa = function() {
                return !$scope.rakenne.tutkinnonOsat
                    ? undefined
                    : $scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id];
            };

            {
                // Versionhallinta
                $scope.haeVersiot = function(force, cb) {
                    VersionHelper.getTutkinnonOsaViiteVersions(
                        $scope.versiot,
                        { id: $scope.tutkinnonOsaViite.id },
                        force,
                        cb
                    );
                };

                var errorCb = function() {
                    virheService.virhe("virhe-koulutuksen-ei-löytynyt");
                };

                var successCb = function(re) {
                    re.tyyppi = "tutke2";
                    setupTutkinnonOsaViite(re);
                    tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
                    $scope.$laajuusRangena = $scope.editableTutkinnonOsaViite.laajuusMaksimi > 0;
                    if (TutkinnonOsaEditMode.getMode()) {
                        $scope.isNew = true;
                        $scope.muokkaa();
                    }
                };

                var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
                if (versio) {
                    VersionHelper.getTutkinnonOsaViiteVersions(
                        $scope.versiot,
                        { id: $stateParams.tutkinnonOsaViiteId },
                        true,
                        function() {
                            var revNumber = VersionHelper.select($scope.versiot, versio);
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
            }
        }

        function mergeOsaAlueet(tutkinnonOsa) {
            if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === "tutke2") {
                tutkinnonOsa.osaAlueet = _.map(Tutke2OsaData.get().$editing, function(osaAlue) {
                    var item: any = { nimi: osaAlue.nimi };
                    if (osaAlue.id) {
                        item.id = osaAlue.id;
                    }
                    return item;
                });
            }
        }

        function getSisalto(item) {
            if (item) {
                return {
                    tavoitteet: _.map(item.tavoitteet, function(tavoite) {
                        return _.omit(tavoite, "jarjestys");
                    }),
                    kohde: item.kohde,
                    selite: item.selite,
                    nimi: item.nimi
                };
            }
            return null;
        }

        var editointikontrollit = {
            edit: function() {
                Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
            },
            asyncValidate: function(done) {
                if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
                    $scope.nimiValidationError = true;
                    return false;
                }

                if (
                    $scope.$laajuusRangena &&
                    $scope.editableTutkinnonOsaViite.laajuusMaksimi &&
                    $scope.editableTutkinnonOsaViite.laajuus >= $scope.editableTutkinnonOsaViite.laajuusMaksimi
                ) {
                    Notifikaatiot.varoitus("laajuuden-maksimi-ei-voi-olla-pienempi-tai-sama-kuin-minimi");
                } else {
                    done();
                }
            },
            save: function(kommentti) {
                if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto) {
                    $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto = {
                        osaamistavoite: _.map(
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite,
                            getSisalto
                        ),
                        osaamisenarviointi: getSisalto(
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi
                        ),
                        osaamisenarviointiTekstina:
                            $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointiTekstina
                    };
                }

                if (!$scope.$laajuusRangena || !$scope.editableTutkinnonOsaViite.laajuusMaksimi) {
                    $scope.editableTutkinnonOsaViite.laajuusMaksimi = undefined;
                    $scope.$laajuusRangena = false;
                }

                Tutke2Service.mergeOsaAlueet($scope.editableTutkinnonOsaViite.tutkinnonOsa);
                $scope.editableTutkinnonOsaViite.metadata = { kommentti: kommentti };

                if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.id) {
                    PerusteTutkinnonosa.save(
                        {
                            perusteId: $scope.peruste.id,
                            suoritustapa: $stateParams.suoritustapa,
                            osanId: $scope.editableTutkinnonOsaViite.tutkinnonOsa.id
                        },
                        $scope.editableTutkinnonOsaViite,
                        function(response) {
                            $scope.editableTutkinnonOsaViite = angular.copy(response);
                            $scope.tutkinnonOsaViite = angular.copy(response);
                            Editointikontrollit.lastModified = response;
                            saveCb(response.tutkinnonOsa);
                            //getRakenne();

                            tutkinnonOsaDefer = $q.defer();
                            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
                        },
                        Notifikaatiot.serverCb
                    );
                } else {
                    PerusteenOsat.saveTutkinnonOsa(
                        $scope.editableTutkinnonOsaViite.tutkinnonOsa,
                        function(response) {
                            Editointikontrollit.lastModified = response;
                            saveCb(response);
                            // getRakenne();
                        },
                        Notifikaatiot.serverCb
                    );
                }
                $scope.isNew = false;
            },
            cancel: function() {
                if ($scope.isNew) {
                    doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id].id);
                    $scope.isNew = false;
                } else {
                    Tutke2Service.fetch($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
                    fetch(function() {
                        refreshPromise();
                        Lukitus.vapautaPerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id);
                    });
                }
            },
            notify: function(mode) {
                $scope.editEnabled = mode;
            },
            validate: function() {
                return true;
                // if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
                //   $scope.nimiValidationError = true;
                // }
                // return $scope.tutkinnonOsaHeaderForm.$valid && Tutke2Service.validate($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi);
            }
        };

        function fetch(cb) {
            // NOTE! Pitäisikö hakea tutkinnonosaviite eikä tutkinnonosaa
            cb = cb || angular.noop;
            PerusteenOsat.get({ osanId: $scope.tutkinnonOsaViite.tutkinnonOsa.id }, function(res) {
                if (res.valmaTelmaSisalto) {
                    res.valmaTelmaSisalto.osaamisenarviointi = isEmptyValmaList(
                        res.valmaTelmaSisalto.osaamisenarviointi
                    )
                        ? null
                        : res.valmaTelmaSisalto.osaamisenarviointi;

                    res.valmaTelmaSisalto.osaamistavoite = isEmptyValmaList(res.valmaTelmaSisalto.osaamistavoite)
                        ? null
                        : res.valmaTelmaSisalto.osaamistavoite;
                }

                $scope.tutkinnonOsaViite.tutkinnonOsa = res;
                cb(res);
            });
        }

        $scope.muokkaa = function() {
            Editointikontrollit.registerCallback(editointikontrollit);
            lukitse(function() {
                fetch(function() {
                    Editointikontrollit.startEditing();
                    refreshPromise();
                });
            });
        };

        var tutke2 = {
            fetch: function() {
                if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === "tutke2") {
                    if (Tutke2OsaData.get()) {
                        Tutke2OsaData.get().fetch();
                    }
                }
            },
            mergeOsaAlueet: function(tutkinnonOsa) {
                if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === "tutke2") {
                    tutkinnonOsa.osaAlueet = _.map(Tutke2OsaData.get().$editing, function(osaAlue) {
                        var item: any = { nimi: osaAlue.nimi };
                        if (osaAlue.id) {
                            item.id = osaAlue.id;
                        }
                        return item;
                    });
                }
            },
            validate: function() {
                if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.tyyppi === "tutke2") {
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

        var normalCallbacks = {
            edit: function() {
                tutke2.fetch();
            },
            asyncValidate: function(cb) {
                lukitse(function() {
                    cb();
                });
            },
            save: function(kommentti) {
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
                        function(response) {
                            $scope.editableTutkinnonOsaViite = angular.copy(response);
                            $scope.tutkinnonOsaViite = angular.copy(response);
                            Editointikontrollit.lastModified = response;
                            saveCb(response.tutkinnonOsa);
                            //getRakenne();

                            tutkinnonOsaDefer = $q.defer();
                            $scope.tutkinnonOsaPromise = tutkinnonOsaDefer.promise;
                            tutkinnonOsaDefer.resolve($scope.editableTutkinnonOsaViite);
                        },
                        Notifikaatiot.serverCb
                    );
                } else {
                    PerusteenOsat.saveTutkinnonOsa(
                        $scope.editableTutkinnonOsaViite.tutkinnonOsa,
                        function(response) {
                            Editointikontrollit.lastModified = response;
                            saveCb(response);
                            //getRakenne();
                        },
                        Notifikaatiot.serverCb
                    );
                }
                $scope.isNew = false;
            },
            cancel: function() {
                if ($scope.isNew) {
                    doDelete($scope.rakenne.tutkinnonOsat[$scope.tutkinnonOsaViite.tutkinnonOsa.id].id);
                    $scope.isNew = false;
                } else {
                    tutke2.fetch();
                    fetch(function() {
                        refreshPromise();
                        Lukitus.vapautaPerusteenosa($scope.tutkinnonOsaViite.tutkinnonOsa.id);
                    });
                }
            },
            notify: function(mode) {
                $scope.editEnabled = mode;
            },
            validate: function() {
                if (!Utils.hasLocalizedText($scope.editableTutkinnonOsaViite.tutkinnonOsa.nimi)) {
                    $scope.nimiValidationError = true;
                }
                return $scope.tutkinnonOsaHeaderForm && $scope.tutkinnonOsaHeaderForm.$valid && tutke2.validate();
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

            if ($scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto) {
                $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi = isEmptyValmaList(
                    $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi
                )
                    ? null
                    : $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamisenarviointi;

                $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite = isEmptyValmaList(
                    $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite
                )
                    ? null
                    : $scope.editableTutkinnonOsaViite.tutkinnonOsa.valmaTelmaSisalto.osaamistavoite;
            }

            if ($state.current.name === "root.perusteprojekti.suoritustapa.tutkinnonosa") {
                Editointikontrollit.registerCallback(normalCallbacks);
            }
            $scope.haeVersiot();
            Lukitus.tarkista($scope.tutkinnonOsaViite.tutkinnonOsa.id, $scope);
        }

        function isEmptyValmaList(item) {
            return (
                !item || item === null || (item.kohde === null && item.selite === null && item.tavoitteet.length === 0)
            );
        }
    });

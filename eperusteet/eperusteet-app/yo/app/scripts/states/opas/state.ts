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

angular
    .module("eperusteApp")
    .config($stateProvider => {
        $stateProvider
            .state("root.oppaat", {
                url: "/oppaat",
                template: "<div ui-view></div>",
                abstract: true
            })
            .state("root.perusteprojekti.suoritustapa.opassisalto", {
                url: "/opassisalto",
                template: require("views/partials/perusteprojekti/opas.html"),
                controller: "OpasSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.oppaat.uusi", {
                url: "/uusi",
                template: require("scripts/states/opas/view.pug"),
                resolve: {
                    oppaat: Api => Api.all("oppaat")
                },
                controller: (
                    $scope,
                    $uibModal,
                    $stateParams,
                    $timeout,
                    oppaat,
                    Varmistusdialogi,
                    Kaanna,
                    PerusteprojektiTila,
                    Notifikaatiot: NotifikaatiotI
                ) => {
                    const { lang } = $stateParams;

                    $scope.opas = {};

                    $scope.palauta = opas => {
                        const uusiTila = "laadinta";
                        Varmistusdialogi.dialogi({
                            otsikko: Kaanna.kaanna("vahvista-palautus"),
                            teksti: Kaanna.kaanna("vahvista-palautus-sisältö", {
                                nimi: opas.nimi,
                                tila: Kaanna.kaanna("tila-" + uusiTila)
                            })
                        })(() => {
                            PerusteprojektiTila.save(
                                { id: opas.id, tila: uusiTila },
                                {},
                                vastaus => {
                                    if (vastaus.vaihtoOk) {
                                        opas.tila = uusiTila;
                                    } else {
                                        Notifikaatiot.varoitus("tilan-vaihto-epaonnistui");
                                    }
                                },
                                Notifikaatiot.serverCb
                            );
                        });
                    };

                    $scope.haeRyhma = async () => {
                        const ryhma = await $uibModal.open({
                            template: require("views/modals/tuotyoryhma.html"),
                            controller: "TyoryhmanTuontiModalCtrl"
                        }).result;
                        $scope.$$ryhmaNimi = ryhma.nimi && ryhma.nimi[lang];
                        $scope.opas.ryhmaOid = ryhma.oid;
                    };

                    $scope.tallennaOpas = async opas => {
                        try {
                            const response = await oppaat.customPOST(opas);
                        } catch (ex) {
                            Notifikaatiot.varoitus(ex);
                        }
                    };
                }
            });
    })
    .controller("OpasSisaltoController", function(
        $scope,
        perusteprojektiTiedot,
        Algoritmit,
        $state,
        SuoritustavanSisalto,
        TekstikappaleOperations,
        SuoritustapaSisalto,
        TutkinnonOsaEditMode,
        Notifikaatiot,
        $stateParams,
        Editointikontrollit,
        YleinenData,
        perusteprojektiBackLink
    ) {
        $scope.projekti = perusteprojektiTiedot.getProjekti();
        $scope.peruste = perusteprojektiTiedot.getPeruste();
        TekstikappaleOperations.setPeruste($scope.peruste);
        $scope.rajaus = "";
        $scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();
        // $scope.$esitysurl = $state.href(
        //   "root.selaus." +
        //     (YleinenData.isEsiopetus($scope.peruste) ? "esiopetus" : "lisaopetus"),
        //   {
        //     perusteId: $scope.peruste.id,
        //     suoritustapa: $stateParams.suoritustapa
        //   }
        // );

        $scope.$watch(
            "peruste.sisalto",
            function() {
                Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                    lapsi.$url = $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                        suoritustapa: $stateParams.suoritustapa,
                        perusteenOsaViiteId: lapsi.id,
                        versio: ""
                    });
                });
            },
            true
        );

        $scope.rajaaSisaltoa = function(value) {
            if (_.isUndefined(value)) {
                return;
            }
            var sisaltoFilterer = function(osa, lapsellaOn) {
                osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, "perusteenOsa", "nimi");
                return osa.$filtered;
            };
            Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.peruste.sisalto, sisaltoFilterer);
        };

        $scope.avaaSuljeKaikki = function(value) {
            var open = false;
            Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                open = open || lapsi.$opened;
            });
            Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", function(lapsi) {
                lapsi.$opened = _.isUndefined(value) ? !open : value;
            });
        };

        $scope.addTekstikappale = function() {
            SuoritustapaSisalto.save(
                {
                    perusteId: $scope.projekti._peruste,
                    suoritustapa: $stateParams.suoritustapa
                },
                {},
                function(response) {
                    TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                    $state.go(
                        "root.perusteprojekti.suoritustapa.tekstikappale",
                        {
                            perusteenOsaViiteId: response.id,
                            versio: ""
                        },
                        {
                            reload: true
                        }
                    );
                },
                Notifikaatiot.serverCb
            );
        };

        $scope.edit = function() {
            Editointikontrollit.startEditing();
        };

        Editointikontrollit.registerCallback({
            edit: function() {
                $scope.rajaus = "";
                $scope.avaaSuljeKaikki(true);
            },
            save: function() {
                TekstikappaleOperations.updateViitteet($scope.peruste.sisalto, function() {
                    Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
                });
            },
            cancel: function() {
                $state.go($state.current.name, $stateParams, {
                    reload: true
                });
            },
            validate: function() {
                return true;
            },
            notify: function(value) {
                $scope.editing = value;
            }
        });
    });

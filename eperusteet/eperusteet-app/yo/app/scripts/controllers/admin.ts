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

enum ProjektiTila {
    POISTETTU = "poistettu",
    LAADINTA = "laadinta",
    VIIMEISTELY = "viimeistely",
    KAANNOS = "kaannos",
    VALMIS = "valmis",
    JULKAISTU = "julkaistu"
}

import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .config($stateProvider => {
        $stateProvider
            .state("root.admin", {
                url: "/admin",
                template: require("views/admin/base.html"),
                controller: "AdminBaseController"
            })
            .state("root.admin.perusteprojektit", {
                url: "/perusteprojektit",
                template: require("views/admin/perusteprojektit.html"),
                controller: "AdminPerusteprojektitController"
            })
            .state("root.admin.tiedotteet", {
                url: "/tiedotteet",
                template: require("views/admin/tiedotteet.pug"),
                controller: "TiedotteidenHallintaController"
            })
            .state("root.admin.virheelliset", {
                url: "/virheelliset",
                template: require("views/admin/virheelliset.pug"),
                controller: "VirheellisetHallintaController"
            })
            .state("root.admin.koulutuskoodiongelmat", {
                url: "/koodisto",
                template: require("views/admin/koulutuskoodiongelmat.pug"),
                controller: "KoulutuskoodiOngelmatController"
            })
            .state("root.admin.oppaat", {
                url: "/oppaat",
                template: require("views/admin/oppaat.pug"),
                controller: "OpasHallintaController"
            })
            .state("root.admin.arviointiasteikot", {
                url: "/arviointiasteikot",
                template: require("views/admin/arviointiasteikot.pug"),
                controller: "ArviointiasteikotHallintaController",
                resolve: {
                    arviointiasteikot: function($stateParams, Arviointiasteikot) {
                        return Arviointiasteikot.list({}).$promise;
                    }
                }
            });
    })
    .controller(
        "ArviointiasteikotHallintaController",
        ($scope, arviointiasteikot, Editointikontrollit, Arviointiasteikot, Notifikaatiot) => {
            // Lasketaan osaamistasoille arviointiasteikkojen mukaiset id:eet
            function calculateFakeIds() {
                _.each(arviointiasteikot, (arviointiasteikko: any) => {
                    const minOsaamistasoId = _.min(
                        _.map(arviointiasteikko.osaamistasot, (osaamistaso: any) => osaamistaso.id)
                    );
                    _.each(arviointiasteikko.osaamistasot, osaamistaso => {
                        osaamistaso.__fakeId = osaamistaso.id - minOsaamistasoId + 1;
                    });
                });
            }
            calculateFakeIds();
            $scope.arviointiasteikot = angular.copy(arviointiasteikot);
            $scope.edit = () => {
                Editointikontrollit.startEditing();
            };
            $scope.sortableOptions = {
                handle: ".handle",
                cursor: "move",
                cursorAt: { top: 10, left: 10 },
                forceHelperSize: true,
                placeholder: "sortable-placeholder",
                forcePlaceholderSize: true,
                opacity: ".7",
                delay: 100,
                axis: "y",
                tolerance: "pointer"
            };

            Editointikontrollit.registerCallback({
                edit: () => {},
                save: () => {
                    Arviointiasteikot.save(
                        {},
                        $scope.arviointiasteikot,
                        res => {
                            arviointiasteikot = res;
                            calculateFakeIds();
                            $scope.arviointiasteikot = angular.copy(arviointiasteikot);
                            Notifikaatiot.onnistui("arviointiasteikkojen-päivitys-onnistui");
                        },
                        virhe => {
                            Notifikaatiot.serverCb(virhe);
                        }
                    );
                },
                cancel: () => {
                    $scope.arviointiasteikot = angular.copy(arviointiasteikot);
                },
                validate: () => {
                    return true;
                },
                notify: value => {}
            });
            Editointikontrollit.registerEditModeListener(mode => {
                $scope.editEnabled = mode;
            });
        }
    )
    .controller("VirheellisetHallintaController", ($location, $scope, $state, Api, PerusteProjektiService) => {
        $scope.virheelliset = null;
        $scope.sivu = 0;
        $scope.sivukoko = 10;
        $scope.kokonaismaara = 10;
        $scope.haeVirheelliset = async function() {
            const virheelliset = await Api.all("perusteprojektit").get("virheelliset", {
                sivu: $scope.sivu,
                sivukoko: $scope.sivukoko,
            });
            $scope.sivu = virheelliset.sivu;
            $scope.sivukoko = virheelliset.sivu;
            $scope.kokonaismaara = virheelliset.kokonaismäärä;
            $scope.virheelliset = _(virheelliset.data)
                .map(validointi => {
                    return {
                        ...validointi,
                        $$url: PerusteProjektiService.getUrl(validointi.perusteprojekti, validointi.perusteprojekti.peruste)
                    };
                })
                .value();
        };
        $scope.haeVirheelliset();
    })
    .controller("KoulutuskoodiOngelmatController", ($scope, Api, PerusteProjektiService) => {
        $scope.ongelmalliset = null;
        $scope.sivu = 0;
        $scope.sivukoko = 10;
        $scope.kokonaismaara = 10;
        $scope.haeOngelmalliset = async function() {
            const ongelmalliset = await Api.all("perusteprojektit").get("koodiongelmat", {
                sivu: $scope.sivu,
                sivukoko: $scope.sivukoko,
            });
            $scope.sivu = ongelmalliset.sivu;
            $scope.sivukoko = ongelmalliset.sivu;
            $scope.kokonaismaara = ongelmalliset.kokonaismäärä;
            $scope.ongelmalliset = _(ongelmalliset.data)
                .map(ongelma => {
                    return {
                        ...ongelma,
                        $$url: PerusteProjektiService.getUrl(ongelma.perusteprojekti, ongelma.perusteprojekti.peruste)
                    };
                })
                .value();
        };
        $scope.haeOngelmalliset();
    })
    .controller("OpasHallintaController", ($location, $scope, $state, Api) => {
        const projektitEp = Api.one("oppaat").one("projektit");

        $scope.rajaus = "";
        $scope.nykyinen = 0;
        $scope.kokonaismaara = 0;
        $scope.itemsPerPage = 20;
        $scope.oppaat = [];

        $scope.updateOpaslist = async () => {
            const result = await projektitEp.get({
                nimi: $scope.rajaus,
                sivukoko: $scope.itemsPerPage,
                sivu: $scope.nykyinen
            });

            $scope.oppaat = result.data;
        };

        $scope.valitseSivu = sivu => {
            $scope.nykyinen = sivu;
            $scope.updateOpaslist();
        };

        $scope.luoUusi = () => {
            $state.go("root.oppaat.uusi");
        };

        $scope.updateOpaslist();
    })
    .controller("AdminBaseController", ($scope, $state) => {
        $scope.tabs = [
            { label: "perusteprojektit", state: "root.admin.perusteprojektit" },
            { label: "tiedotteet", state: "root.admin.tiedotteet" },
            { label: "oppaat", state: "root.admin.oppaat" },
            { label: "arviointiasteikot", state: "root.admin.arviointiasteikot" },
            { label: "virheelliset-perusteet", state: "root.admin.virheelliset" },
            { label: "koulutuskoodi-ongelmat", state: "root.admin.koulutuskoodiongelmat" }
        ];

        $scope.chooseTab = $index => {
            _.each($scope.tabs, (item, index) => {
                item.$tabActive = index === $index;
            });
            const state = $scope.tabs[$index];
            if (state) {
                $state.go(state.state);
            }
        };

        if ($state.current.name === "root.admin") {
            $scope.chooseTab(0);
        } else {
            _.each($scope.tabs, item => {
                item.$tabActive = item.state === $state.current.name;
            });
        }
    })
    .controller(
        "AdminPerusteprojektitController",
        (
            $rootScope,
            $scope,
            Api,
            Algoritmit,
            PerusteprojektiTila,
            Notifikaatiot,
            Kaanna,
            YleinenData,
            Varmistusdialogi,
            PerusteProjektiService
        ) => {
            $scope.jarjestysTapa = "nimi";
            $scope.jarjestysOrder = false;
            $scope.tilaRajain = null;
            $scope.koulutustyyppiRajain = null;
            $scope.itemsPerPage = 10;
            $scope.nykyinen = 1;
            $scope.rajaus = "";
            $scope.tilat = _.keys(ProjektiTila).map(t => ProjektiTila[t]);
            $scope.koulutustyypit = YleinenData.koulutustyypit;

            async function updateSearch() {
                let tila;
                if ($scope.tilaRajain) {
                    tila = $scope.tilaRajain.toUpperCase();
                } else {
                    tila = _($scope.tilat)
                        .filter(tila => tila !== "poistettu")
                        .map(tila => tila.toUpperCase())
                        .value();
                }
                const perusteprojektit = await Api.one("perusteprojektit/perusteHaku").get({
                    nimi: $scope.rajaus,
                    tila: tila,
                    koulutustyyppi: $scope.koulutustyyppiRajain,
                    sivu: $scope.nykyinen - 1,
                    sivukoko: $scope.itemsPerPage,
                    jarjestysTapa: $scope.jarjestysTapa,
                    jarjestysOrder: $scope.jarjestysOrder
                });
                $scope.$apply(() => {
                    $scope.perusteprojektit = _.map(perusteprojektit.data, pp => {
                        return {
                            ...pp,
                            suoritustapa: YleinenData.valitseSuoritustapaKoulutustyypille((pp as any).koulutustyyppi),
                            $$url: PerusteProjektiService.getUrl(pp)
                        };
                    });
                    $scope.nykyinen = perusteprojektit.sivu + 1;
                    $scope.kokonaismaara = perusteprojektit.kokonaismäärä;
                });
            }

            updateSearch();

            const debounceUpdateSearch = _.debounce(updateSearch, 300);

            $scope.updateTila = () => {
                updateSearch();
            };

            $scope.updateRajaus = rajaus => {
                $scope.rajaus = rajaus;
                debounceUpdateSearch();
            };

            $scope.valitseSivu = sivu => {
                $scope.nykyinen = sivu;
                updateSearch();
            };

            $scope.asetaJarjestys = tyyppi => {
                if ($scope.jarjestysTapa === tyyppi) {
                    $scope.jarjestysOrder = !$scope.jarjestysOrder;
                } else {
                    $scope.jarjestysOrder = false;
                    $scope.jarjestysTapa = tyyppi;
                }
                updateSearch();
            };

            $scope.palauta = pp => {
                const uusiTila = "laadinta";
                Varmistusdialogi.dialogi({
                    otsikko: Kaanna.kaanna("vahvista-palautus"),
                    teksti: Kaanna.kaanna("vahvista-palautus-sisältö", {
                        nimi: pp.nimi,
                        tila: Kaanna.kaanna("tila-" + uusiTila)
                    })
                })(() => {
                    PerusteprojektiTila.save(
                        { id: pp.id, tila: uusiTila },
                        {},
                        vastaus => {
                            if (vastaus.vaihtoOk) {
                                pp.tila = uusiTila;
                            } else {
                                Notifikaatiot.varoitus("tilan-vaihto-epaonnistui");
                            }
                        },
                        Notifikaatiot.serverCb
                    );
                });
            };
        }
    );

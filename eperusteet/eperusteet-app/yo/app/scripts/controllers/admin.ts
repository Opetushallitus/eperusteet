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
    KOMMENTOINTI = "kommentointi",
    VIIMEISTELY = "viimeistely",
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
            .state("root.admin.geneerinenarviointi", {
                url: "/geneerinenarviointi",
                template: require("views/admin/geneerinenarviointi.pug"),
                controller: "GeneerinenArviointiController",
                resolve: {
                    geneeriset($stateParams, Api) {
                        return Api.all("geneerinenarviointi").getList();
                    },
                    arviointiasteikot($stateParams, Arviointiasteikot) {
                        return Arviointiasteikot.list({}).$promise;
                    }
                }
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
            })
            .state("root.admin.yllapito", {
                url: "/yllapito",
                template: require("views/admin/yllapito.html"),
                controller: "YllapitoController"
            });
    })
    .controller("GeneerinenArviointiController",
        ($timeout, $scope, geneeriset, arviointiasteikot, Editointikontrollit, Arviointiasteikot, Notifikaatiot, Varmistusdialogi, Kaanna, YleinenData) => {

        $scope.koulutustyypit = function(geneerinen) {
            return _.map(YleinenData.koulutustyypit, function(item) {
                return {
                    value: item,
                    label: Kaanna.kaanna(item),
                    selected: geneerinen.koulutustyypit.indexOf(item) > -1
                };
            });
        };

        $scope.geneeriset = _.map(geneeriset, (geneerinen: any) => {
            return {
                ...geneerinen,
                koulutustyyppivalinnat: $scope.koulutustyypit(geneerinen),
            }
        });

        $scope.updateKoulutustyypit = function(kt, geneerinen) {
            if (kt.selected) {
                geneerinen.koulutustyypit.push(kt.value);
            } else {
                geneerinen.koulutustyypit = _.remove(geneerinen.koulutustyypit, kt.value);
            }
        };

        $scope.arviointiasteikot = _(arviointiasteikot)
            .sortBy(aa => _.size(aa.osaamistasot))
            .map(aa => ({
                ...aa,
                $$tasoMap: _.indexBy(aa.osaamistasot, (taso: any) => "" + taso.id),
            }))
            .reverse()
            .value();

        $scope.arviointiasteikotMap = _.indexBy($scope.arviointiasteikot, (asteikko: any) => "" + asteikko.id);
        $scope.uusiArviointi = {
            arviointiAsteikko: $scope.arviointiasteikot[0].id,
        };

        $scope.lisaaKriteeri = (ot) => {
            ot.kriteerit.push({});
        };

        $scope.poistaKriteeri = (ot, kriteeri) => {
            _.remove(ot.kriteerit, kriteeri);
        };

        $scope.lisaa = async () => {
            const geneerinen = await geneeriset.post({
                _arviointiAsteikko: "" + $scope.uusiArviointi.arviointiAsteikko,
                arviointiAsteikko: "" + $scope.uusiArviointi.arviointiAsteikko,
                kohde: {
                    fi: "Opiskelija",
                    sv: "Den studerande"
                },
                valittavissa: true,
            });
            $scope.geneeriset.push(geneerinen);
        };

        $scope.julkaise = (el) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-julkaisu",
                teksti: "haluatko-varmasti-julkaista",
                primaryBtn: "julkaise",
                async successCb() {
                    el.julkaistu = true;
                    try {
                        await $scope.paivita(el);
                        Notifikaatiot.onnistui("julkaisu-onnistui");
                    }
                    catch (err) {
                        Notifikaatiot.serverCb(err);
                    }
                }
            })();
        };

        $scope.avaa = (el) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-avaus",
                teksti: "haluatko-varmasti-avata",
                primaryBtn: "avaa",
                async successCb() {
                    el.julkaistu = false;
                    try {
                        await $scope.paivita(el);
                    }
                    catch (err) {
                        Notifikaatiot.serverCb(err);
                    }
                }
            })();
        };

        $scope.poistaValittavuus = (el) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-kaytosta-poisto",
                teksti: "haluatko-varmasti-poistaa-arvioinnin-kaytosta",
                primaryBtn: "avaa",
                async successCb() {
                    el.valittavissa = false;
                    try {
                        await $scope.paivita(el);
                    }
                    catch (err) {
                        Notifikaatiot.serverCb(err);
                    }
                }
            })();
        };

        $scope.lisaaValittavuus = (el) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-kaytto",
                teksti: "haluatko-varmasti-ottaa-arvioinnin-kayttoon",
                primaryBtn: "avaa",
                async successCb() {
                    el.valittavissa = true;
                    try {
                        await $scope.paivita(el);
                    }
                    catch (err) {
                        Notifikaatiot.serverCb(err);
                    }
                }
            })();
        };

        $scope.poista = (el) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-julkaisematon-arviointiasteikko",
                primaryBtn: "poista",
                async successCb() {
                    try {
                        await el.remove();
                        _.remove($scope.geneeriset, el);
                        Notifikaatiot.onnistui("poisto-onnistui");
                    }
                    catch (err) {
                        Notifikaatiot.serverCb(err);
                    }
                }
            })();
        };

        $scope.paivita = async(el) => {
            try {
                const wat = await geneeriset.customPUT(el, el.id);
                _.merge(el, wat);
                Notifikaatiot.onnistui("paivitys-onnistui");
            }
            catch (err) {
                Notifikaatiot.serverCb(err);
            }
        };

        $scope.kopioi = async (el) => {
            try {
                Varmistusdialogi.dialogi({
                    otsikko: "vahvista-kopiointi",
                    teksti: "kopioidaanko-asteikko-varmasti",
                    primaryBtn: "kopioi",
                    async successCb() {
                        try {
                            $scope.geneeriset.push(await el.customPOST(undefined, "/kopioi"));
                            Notifikaatiot.onnistui("kopiointi-onnistui");
                        }
                        catch (err) {
                            Notifikaatiot.serverCb(err);
                        }
                    }
                })();
            }
            catch (err) {
                Notifikaatiot.serverCb(err);
            }
        };

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
            { label: "geneerinenarviointi", state: "root.admin.geneerinenarviointi" }
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
    )
    .controller("YllapitoController", ($location, $scope, $state, Api, Notifikaatiot) => {
        const yllapito = Api.one("maintenance").one("yllapito");
        $scope.yllapidot = [];

        $scope.updateYllapitoList = async () => {
            $scope.yllapidot = await yllapito.get();
        };

        $scope.updateYllapitoList();

        $scope.kaynnista = async (yllapito) => {
            await Api.one(yllapito.url).get();
            Notifikaatiot.onnistui("ajo-kaynnistetty");
        };
    });

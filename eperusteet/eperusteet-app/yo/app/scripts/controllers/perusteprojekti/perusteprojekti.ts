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
    .config($stateProvider =>
        $stateProvider
            .state("root.perusteprojekti", {
                abstract: true,
                url: "/perusteprojekti/:perusteProjektiId",
                template: require("views/perusteprojekti.html"),
                controller: "PerusteprojektiCtrl",
                resolve: {
                    koulutusalaService: Koulutusalat => Koulutusalat,
                    opintoalaService: Opintoalat => Opintoalat,
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    perusteprojektiAlustus: (perusteprojektiTiedot, $stateParams) =>
                        perusteprojektiTiedot.alustaProjektinTiedot($stateParams),
                    perusteprojektiOikeudet: PerusteprojektiOikeudetService => PerusteprojektiOikeudetService,
                    perusteprojektiOikeudetNouto: (perusteprojektiOikeudet, $stateParams) =>
                        perusteprojektiOikeudet.noudaOikeudet($stateParams),
                    perusteprojekti: async (perusteprojektiOikeudetNouto, perusteprojektiTiedot) => {
                        await perusteprojektiTiedot.projektinTiedotAlustettu();
                        return perusteprojektiTiedot.getProjekti();
                    },
                    peruste: async perusteprojektiTiedot => {
                        await perusteprojektiTiedot.projektinTiedotAlustettu();
                        return perusteprojektiTiedot.getPeruste();
                    },
                    isOpas: async peruste => {
                        return peruste.tyyppi === "opas";
                    },
                    perusteprojektiBackLink: async (
                        $state,
                        $stateParams,
                        $timeout,
                        PerusteProjektiService,
                        perusteprojekti,
                        peruste
                    ) => {
                        return new Promise((resolve, reject) => {
                            let result = "";
                            if (peruste.tyyppi !== "opas") {
                                result = PerusteProjektiService.getUrl(perusteprojekti, peruste);
                            } else {
                                result = $state.href("root.perusteprojekti.suoritustapa.opassisalto", {
                                    ...$stateParams,
                                    perusteProjektiId: perusteprojekti.id,
                                    suoritustapa: "opas"
                                });
                            }
                            return resolve(result);
                        });
                    }
                }
            })
            .state("root.perusteprojekti.suoritustapa.lukioosat", {
                url: "/lukioosat/:osanTyyppi?{versio?:int}",
                template: require("views/partials/lukio/osat/osalistaus.html"),
                controller: "LukioOsalistausController",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    projektinTiedotAlustettu: perusteprojektiTiedot => perusteprojektiTiedot.projektinTiedotAlustettu(),
                    perusteenSisaltoAlustus: (perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) =>
                        perusteprojektiTiedot.alustaPerusteenSisalto($stateParams)
                },
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(true);
                }
            })
            .state("root.perusteprojekti.suoritustapa.lukioosaalue", {
                url: "/lukioosat/:osanTyyppi/:osanId/:tabId/:editEnabled?{versio?:int}",
                template: require("views/partials/lukio/osat/osaalue.html"),
                controller: "LukioOsaAlueController",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    projektinTiedotAlustettu: perusteprojektiTiedot => perusteprojektiTiedot.projektinTiedotAlustettu(),
                    perusteenSisaltoAlustus: (perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) =>
                        perusteprojektiTiedot.alustaPerusteenSisalto($stateParams)
                },
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.osalistaus", {
                url: "/osat/:osanTyyppi",
                template: require("views/partials/perusteprojekti/osalistaus.html"),
                controller: "OsalistausController",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    projektinTiedotAlustettu: perusteprojektiTiedot => perusteprojektiTiedot.projektinTiedotAlustettu(),
                    perusteenSisaltoAlustus: (perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) =>
                        perusteprojektiTiedot.alustaPerusteenSisalto($stateParams)
                },
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.osaalue", {
                url: "/osat/:osanTyyppi/:osanId/:tabId",
                template: require("views/partials/perusteprojekti/osaalue.html"),
                controller: "OsaAlueController",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    projektinTiedotAlustettu: perusteprojektiTiedot => perusteprojektiTiedot.projektinTiedotAlustettu(),
                    perusteenSisaltoAlustus: (perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) =>
                        perusteprojektiTiedot.alustaPerusteenSisalto($stateParams)
                },
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.muokkaus", {
                url: "/muokkaus/:osanTyyppi/:osanId",
                template: require("views/muokkaus.html"),
                controller: "OsanMuokkausController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa", {
                url: "/:suoritustapa",
                template: "<div ui-view></div>",
                navigaationimi: "navi-perusteprojekti",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
                    projektinTiedotAlustettu: async (perusteprojektiTiedot, $log) => {
                        const result = await perusteprojektiTiedot.projektinTiedotAlustettu();
                        return result;
                    },
                    perusteenSisaltoAlustus: async (
                        perusteprojektiTiedot,
                        projektinTiedotAlustettu,
                        $stateParams,
                        $log
                    ) => {
                        const result = await perusteprojektiTiedot.alustaPerusteenSisalto($stateParams);
                        return result;
                    }
                },
                controller: (
                    $timeout,
                    $scope,
                    $state,
                    $stateParams,
                    YleinenData,
                    Kielimapper,
                    perusteprojektiTiedot
                ) => {
                    // !!! Alustetaan kaikkia alitiloja varten !!!!
                    $scope.isVaTe = YleinenData.isValmaTelma(perusteprojektiTiedot.getPeruste());
                    $scope.vateConverter = Kielimapper.mapTutkinnonosatKoulutuksenosat($scope.isVaTe);
                    const hasCurrentSuoritustapa = _.any(
                        $scope.peruste.suoritustavat,
                        (st: any) => st.suoritustapakoodi === $stateParams.suoritustapa
                    );
                    if (!hasCurrentSuoritustapa && !_.isEmpty($scope.peruste.suoritustavat)) {
                        $state.go(
                            $state.current.name,
                            _.merge({
                                suoritustapa: $scope.peruste.suoritustavat[0].suoritustapakoodi
                            })
                        );
                    }
                },
                abstract: true
            })
            .state("root.perusteprojekti.suoritustapa.muodostumissaannot", {
                url: "/rakenne?{versio?:int}",
                template: require("views/partials/perusteprojekti/muodostumissaannot.html"),
                controller: "PerusteprojektiMuodostumissaannotCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.koulutuksenosat", {
                url: "/koulutuksenosat",
                template: require("views/partials/perusteprojekti/tutkinnonosat.html"),
                controller: "PerusteprojektiTutkinnonOsatCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.koulutuksenosa", {
                url: "/koulutuksenosa/{tutkinnonOsaViiteId}?{versio?:int}",
                template: require("views/partials/muokkaus/koulutuksenosa.html"),
                controller: "muokkausKoulutuksenosaCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.tutkinnonosat", {
                url: "/tutkinnonosat",
                template: require("views/partials/perusteprojekti/tutkinnonosat.html"),
                controller: "PerusteprojektiTutkinnonOsatCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.tutkinnonosa", {
                url: "/tutkinnonosa/{tutkinnonOsaViiteId}?{versio?:int}",
                template: require("views/partials/tutkinnonosa/tutkinnonosa2018.pug"),
                // template: require("views/partials/tutkinnonosa/tutkinnonosa2018.pug"),
                controller: "tutkinnonOsa2018Ctrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                },
                resolve: {
                }
            })
            // .state("root.perusteprojekti.suoritustapa.tutkinnonosa", {
            //     url: "/tutkinnonosa/{tutkinnonOsaViiteId}?{versio?:int}",
            //     template: require("views/partials/muokkaus/tutkinnonosa.pug"),
            //     controller: "muokkausTutkinnonosaCtrl",
            //     onEnter: PerusteProjektiSivunavi => {
            //         PerusteProjektiSivunavi.setVisible();
            //     }
            // })
            .state("root.perusteprojekti.suoritustapa.taiteenala", {
                url: "/taiteenala/{perusteenOsaViiteId}?{versio?:int}",
                template: require("views/partials/muokkaus/taiteenala.html"),
                controller: "taiteenalaCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.tekstikappale", {
                url: "/tekstikappale/{perusteenOsaViiteId}?{versio?:int}",
                template: require("views/partials/muokkaus/tekstikappale.html"),
                controller: "muokkausTekstikappaleCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible();
                }
            })
            .state("root.perusteprojekti.suoritustapa.tutkinnonosa.osaalue", {
                url: "/osaalue/{osaAlueId}",
                template: require("views/partials/muokkaus/tutkinnonOsaOsaAlue.html"),
                controller: "TutkinnonOsaOsaAlueCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.koulutuksenosa.osaalue", {
                url: "/osaalue/{osaAlueId}",
                template: require("views/partials/muokkaus/koulutuksenOsaOsaAlue.html"),
                controller: "KoulutuksenOsaOsaAlueCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.sisalto", {
                url: "/sisalto",
                template: require("views/partials/perusteprojekti/sisalto.html"),
                controller: "PerusteprojektisisaltoCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.posisalto", {
                url: "/posisalto",
                template: require("views/partials/perusteprojekti/perusopetus.html"),
                controller: "PerusopetusSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.lukiosisalto", {
                url: "/lukiosisalto",
                template: require("views/partials/perusteprojekti/lukiokoulutus.html"),
                controller: "LukiokoulutussisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.losisalto", {
                url: "/losisalto",
                template: require("views/partials/perusteprojekti/esiopetus.html"),
                controller: "EsiopetusSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.tposisalto", {
                url: "/tposisalto",
                template: require("views/partials/perusteprojekti/tpoopetus.html"),
                controller: "TpoSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.vksisalto", {
                url: "/vksisalto",
                template: require("views/partials/perusteprojekti/esiopetus.html"),
                controller: "EsiopetusSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.suoritustapa.eosisalto", {
                url: "/eosisalto",
                template: require("views/partials/perusteprojekti/esiopetus.html"),
                controller: "EsiopetusSisaltoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.tiedot", {
                url: "/perustiedot",
                template: require("views/partials/perusteprojekti/tiedot.html"),
                controller: "ProjektinTiedotCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.peruste", {
                url: "/peruste",
                template: require("views/partials/perusteprojekti/peruste.pug"),
                controller: "PerusteenTiedotCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                },
                resolve: {
                    valittavatKielet: Perusteet => {
                        return Perusteet.valittavatKielet().$promise;
                    }
                }
            })
            .state("root.perusteprojekti.projektiryhma", {
                url: "/projektiryhma",
                template: require("views/partials/perusteprojekti/projektiryhma.html"),
                controller: "ProjektiryhmaCtrl",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojekti.termisto", {
                url: "/termisto",
                template: require("views/partials/perusteprojekti/termisto.html"),
                controller: "TermistoController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(false);
                }
            })
            .state("root.perusteprojektiwizard", {
                url: "/perusteprojekti",
                template: "<div ui-view></div>",
                abstract: true
            })
            .state("root.perusteprojektiwizard.pohja", {
                url: "/perustepohja",
                template: require("views/partials/perusteprojekti/tiedot.html"),
                controller: "ProjektinTiedotCtrl",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService
                }
            })
            .state("root.perusteprojektiwizard.tiedot", {
                url: "/perustiedot",
                template: require("views/partials/perusteprojekti/tiedot.html"),
                controller: "ProjektinTiedotCtrl",
                resolve: {
                    perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService
                }
            })
            .state("root.perusteprojekti.suoritustapa.lisaaLukioKurssi", {
                url: "/lukiokurssi/luo",
                template: require("views/partials/lukio/lisaaKurssi.html"),
                controller: "LisaaLukioKurssiController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(true);
                }
            })
            .state("root.perusteprojekti.suoritustapa.kurssi", {
                url: "/lukiokurssi/:kurssiId",
                template: require("views/partials/lukio/kurssi.html"),
                controller: "NaytaLukiokurssiController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(true);
                }
            })
            .state("root.perusteprojekti.suoritustapa.muokkaakurssia", {
                url: "/lukiokurssi/:kurssiId/muokkaa",
                template: require("views/partials/lukio/muokkaaKurssia.html"),
                controller: "MuokkaaLukiokurssiaController",
                onEnter: PerusteProjektiSivunavi => {
                    PerusteProjektiSivunavi.setVisible(true);
                }
            })
    )
    .controller(
        "PerusteprojektiCtrl",
        (
            $scope,
            $state,
            $stateParams,
            Kieli,
            Navigaatiopolku,
            Notifikaatiot,
            PdfCreation,
            PerusteProjektiService,
            PerusteProjektiSivunavi,
            ProxyService,
            SuoritustapaSisalto,
            TermistoService,
            TiedoteService,
            TutkinnonOsaEditMode,
            YleinenData,
            isOpas,
            koulutusalaService,
            opintoalaService,
            perusteprojektiBackLink,
            perusteprojektiOikeudet,
            perusteprojektiTiedot
        ) => {
            function init() {
                $scope.projekti = perusteprojektiTiedot.getProjekti();
                $scope.peruste = perusteprojektiTiedot.getPeruste();
                Kieli.setAvailableSisaltokielet($scope.peruste.kielet);
                $scope.pdfEnabled = PerusteProjektiService.isPdfEnabled($scope.peruste);
                TermistoService.setPeruste($scope.peruste);
                ProxyService.set("perusteId", $scope.peruste.id);
            }
            init();

            $scope.isOpas = isOpas; // Käytetään alinäkymissä
            const isAmmatillinen = koulutustyyppi => _.includes(YleinenData.ammatillisetkoulutustyypit, koulutustyyppi);
            $scope.isAmmatillinen = isAmmatillinen($scope.peruste.koulutustyyppi);
            $scope.muokkausEnabled = false;
            $scope.backLink = perusteprojektiBackLink;
            $scope.$$kaannokset = perusteprojektiTiedot.getPerusteprojektiKaannokset($scope.isOpas && "opas");

            $scope.lisaaTiedote = () => {
                TiedoteService.lisaaTiedote(null, $stateParams.perusteProjektiId);
            };

            $scope.luoPdf = () => {
                PdfCreation.setPeruste($scope.peruste);
                PdfCreation.openModal($scope.isOpas, $scope.isAmmatillinen);
            };

            // Generoi uudestaan "Projektin päänäkymä"-linkki kun suoritustapa vaihtuu
            if (_.size($scope.peruste.suoritustavat) > 1) {
                $scope.$watch(
                    () => PerusteProjektiService.getSuoritustapa(),
                    () => ($scope.backLink = PerusteProjektiService.getUrl($scope.projekti, $scope.peruste))
                );
            }

            const amFooter =
                '<button class="btn btn-default"' +
                '                     kaanna="lisaa-tutkintokohtainen-osa"' +
                '                     icon-role="ep-text-add"' +
                '                     ng-click="$parent.lisaaTekstikappale()"' +
                "                     oikeustarkastelu=\"{ target: 'peruste', permission: 'muokkaus' }\"></button>";
            $scope.Koulutusalat = koulutusalaService;
            $scope.Opintoalat = opintoalaService;
            $scope.sivunavi = {
                suoritustapa: PerusteProjektiService.getSuoritustapa(),
                items: [],
                footer: amFooter,
                type: "AM"
            };
            const sivunaviItemsChanged = items => {
                $scope.sivunavi.items = items;
            };
            const sivunaviTypeChanged = type => {
                $scope.sivunavi.type = type;
                switch (type) {
                    case "YL":
                        $scope.sivunavi.suoritustapa = "";
                        $scope.sivunavi.footer = "";
                        break;
                    case "AIPE":
                        $scope.sivunavi.suoritustapa = "";
                        $scope.sivunavi.footer = "";
                        break;
                    default:
                        $scope.sivunavi.footer = amFooter;
                        $scope.sivunavi.suoritustapa = PerusteProjektiService.getSuoritustapa();
                        break;
                }
            };
            PerusteProjektiSivunavi.register("itemsChanged", sivunaviItemsChanged);
            PerusteProjektiSivunavi.register("typeChanged", sivunaviTypeChanged);
            PerusteProjektiSivunavi.refresh(true);

            $scope.$on("$stateChangeSuccess", () => {
                const newSuoritustapa = PerusteProjektiService.getSuoritustapa();
                if (newSuoritustapa !== $scope.sivunavi.suoritustapa) {
                    PerusteProjektiSivunavi.refresh(true);
                }
                $scope.sivunavi.suoritustapa = $scope.sivunavi.type === "AM" ? newSuoritustapa : "";
            });

            Navigaatiopolku.setProject($scope.projekti, $scope.peruste);

            $scope.koulutusalaNimi = koodi => koulutusalaService.haeKoulutusalaNimi(koodi);

            $scope.canChangePerusteprojektiStatus = () =>
                perusteprojektiOikeudet.onkoOikeudet("perusteprojekti", "tilanvaihto");

            $scope.showBackLink = () =>
                !(
                    $state.is("root.perusteprojekti.suoritustapa.sisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.opassisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.posisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.aipesisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.vksisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.eosisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.losisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.tposisalto") ||
                    $state.is("root.perusteprojekti.suoritustapa.lukiosisalto")
                );
            $scope.isNaviVisible = () => PerusteProjektiSivunavi.isVisible();

            $scope.$on("update:perusteprojekti", () => {
                $scope.projekti = perusteprojektiTiedot.getProjekti();
                perusteprojektiTiedot.alustaProjektinTiedot($stateParams).then(() => {
                    init();
                    PerusteProjektiSivunavi.refresh(true);
                });
            });

            $scope.lisaaTekstikappale = () => {
                function lisaaSisalto(method, sisalto, cb) {
                    cb = cb || angular.noop;
                    SuoritustapaSisalto[method](
                        {
                            perusteId: $scope.projekti._peruste,
                            suoritustapa: PerusteProjektiService.getSuoritustapa()
                        },
                        sisalto,
                        cb,
                        Notifikaatiot.serverCb
                    );
                }

                lisaaSisalto("save", {}, res => {
                    TutkinnonOsaEditMode.setMode(true); // Uusi luotu, siirry suoraan muokkaustilaan
                    $state.go("root.perusteprojekti.suoritustapa.tekstikappale", {
                        perusteenOsaViiteId: res.id,
                        versio: ""
                    });
                });
            };

            $scope.$on("enableEditing", () => {
                $scope.muokkausEnabled = true;
            });
            $scope.$on("disableEditing", () => {
                $scope.muokkausEnabled = false;
            });

            $scope.$on("$destroy", () => {
                Kieli.resetSisaltokielet();
            });
        }
    );

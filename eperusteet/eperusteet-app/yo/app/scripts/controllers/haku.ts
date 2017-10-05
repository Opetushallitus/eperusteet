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
    .config(function($stateProvider) {
        var paramList = "?nimi&koulutusala&tyyppi&opintoala";
        $stateProvider
            .state("root.selaus", {
                url: "/selaus",
                template: "<div ui-view></div>"
            })
            .state("root.selaus.ammatillinenperuskoulutus", {
                url: "/ammatillinenperuskoulutus" + paramList,
                template: require("views/haku.html"),
                controller: "HakuCtrl",
                resolve: { koulutusalaService: "Koulutusalat" }
            })
            .state("root.selaus.ammatillinenaikuiskoulutus", {
                url: "/ammatillinenaikuiskoulutus" + paramList,
                template: require("views/haku.html"),
                controller: "HakuCtrl",
                resolve: { koulutusalaService: "Koulutusalat" }
            })
            .state("root.selaus.esiopetuslista", {
                url: "/esiopetus",
                template: require("views/perusopetuslistaus.html"),
                controller: "EsiopetusListaController"
            })
            .state("root.selaus.aikuisperusopetuslista", {
                url: "/aikuisperusopetus",
                template: require("views/aikuisperusopetuslistaus.html"),
                controller: "AikuisperusopetusListaController"
            })
            .state("root.selaus.perusopetuslista", {
                url: "/perusopetus",
                template: require("views/perusopetuslistaus.html"),
                controller: "PerusopetusListaController"
            })
            .state("root.selaus.lukiokoulutuslista", {
                url: "/lukiokoulutuslistaus",
                template: require("views/lukiokoulutuslistaus.html"),
                controller: "LukiokoulutusListaController"
            })
            .state("root.selaus.lisaopetus", {
                url: "/lisaopetus/:perusteId",
                template: require("eperusteet-esitys/views/yksinkertainen.html"),
                controller: "epYksinkertainenPerusteController",
                resolve: {
                    sisalto: function($stateParams, $q, Perusteet, SuoritustapaSisalto) {
                        // TODO lisää uusin peruste jos $stateParams.perusteId on falsey
                        return $q.all([
                            Perusteet.get({ perusteId: $stateParams.perusteId }).$promise,
                            SuoritustapaSisalto.get({ perusteId: $stateParams.perusteId, suoritustapa: "esiopetus" })
                                .$promise
                        ]);
                    },
                    koulutusalaService: "Koulutusalat",
                    opintoalaService: "Opintoalat"
                }
            })
            .state("root.selaus.lisaopetus.tekstikappale", {
                url: "/tekstikappale/:tekstikappaleId",
                template: require("eperusteet-esitys/views/tekstikappale.html"),
                controller: "epEsitysSisaltoController",
                resolve: {
                    tekstikappaleId: function($stateParams) {
                        return $stateParams.tekstikappaleId;
                    },
                    tekstikappale: function(tekstikappaleId, PerusteenOsat) {
                        return PerusteenOsat.getByViite({ viiteId: tekstikappaleId }).$promise;
                    },
                    lapset: function(sisalto, tekstikappaleId, epTekstikappaleChildResolver) {
                        return epTekstikappaleChildResolver.get(sisalto[1], tekstikappaleId);
                    }
                }
            })
            .state("root.selaus.lisaopetus.tiedot", {
                url: "/tiedot",
                template: require("eperusteet-esitys/views/tiedot.html"),
                controller: "epEsitysTiedotController"
            })
            .state("root.selaus.esiopetus", {
                url: "/esiopetus/:perusteId",
                template: require("eperusteet-esitys/views/yksinkertainen.html"),
                controller: "epYksinkertainenPerusteController",
                resolve: {
                    sisalto: function($stateParams, $q, Perusteet, SuoritustapaSisalto) {
                        // TODO lisää uusin peruste jos $stateParams.perusteId on falsey
                        return $q.all([
                            Perusteet.get({ perusteId: $stateParams.perusteId }).$promise,
                            SuoritustapaSisalto.get({ perusteId: $stateParams.perusteId, suoritustapa: "esiopetus" })
                                .$promise
                        ]);
                    },
                    koulutusalaService: "Koulutusalat",
                    opintoalaService: "Opintoalat"
                }
            })
            .state("root.selaus.esiopetus.tekstikappale", {
                url: "/tekstikappale/:tekstikappaleId",
                template: require("eperusteet-esitys/views/tekstikappale.html"),
                controller: "epEsitysSisaltoController",
                resolve: {
                    tekstikappaleId: function($stateParams) {
                        return $stateParams.tekstikappaleId;
                    },
                    tekstikappale: function(tekstikappaleId, PerusteenOsat) {
                        return PerusteenOsat.getByViite({ viiteId: tekstikappaleId }).$promise;
                    },
                    lapset: function(sisalto, tekstikappaleId, epTekstikappaleChildResolver) {
                        return epTekstikappaleChildResolver.get(sisalto[1], tekstikappaleId);
                    }
                }
            })
            .state("root.selaus.esiopetus.tiedot", {
                url: "/tiedot",
                template: require("eperusteet-esitys/views/tiedot.html"),
                controller: "epEsitysTiedotController"
            })
            .state("root.selaus.perusopetus", {
                url: "/perusopetus/:perusteId",
                template: require("eperusteet-esitys/views/perusopetus.html"),
                controller: "epPerusopetusController",
                resolve: {
                    perusteId: function($stateParams) {
                        return $stateParams.perusteId;
                    },
                    peruste: function(perusteId, Perusteet) {
                        return Perusteet.get({ perusteId: perusteId }).$promise;
                    },
                    sisalto: function(
                        peruste,
                        $q,
                        LaajaalaisetOsaamiset,
                        Oppiaineet,
                        Vuosiluokkakokonaisuudet,
                        SuoritustapaSisalto
                    ) {
                        if (_.isArray(peruste.data)) {
                            peruste = peruste.data[0];
                        }
                        var perusteId = peruste.id;
                        return $q.all([
                            peruste,
                            LaajaalaisetOsaamiset.query({ perusteId: perusteId }).$promise,
                            Oppiaineet.query({ perusteId: perusteId }).$promise,
                            Vuosiluokkakokonaisuudet.query({ perusteId: perusteId }).$promise,
                            SuoritustapaSisalto.get({ perusteId: perusteId, suoritustapa: "perusopetus" }).$promise
                        ]);
                    }
                }
            })
            .state("root.selaus.perusopetus.vuosiluokkakokonaisuus", {
                url: "/vuosiluokkakokonaisuus/:vlkId",
                template: require("eperusteet-esitys/views/vuosiluokkakokonaisuus.html"),
                controller: "epPerusopetusVlkController"
            })
            .state("root.selaus.perusopetus.laajaalaiset", {
                url: "/laajaalaisetosaamiset",
                template: require("eperusteet-esitys/views/laajaalaiset.html"),
                controller: "epLaajaalaisetOsaamisetController"
            })
            .state("root.selaus.perusopetus.vlkoppiaine", {
                url: "/vuosiluokkakokonaisuus/:vlkId/oppiaine/:oppiaineId",
                template: require("eperusteet-esitys/views/vlkoppiaine.html"),
                controller: "epPerusopetusVlkOppiaineController",
                resolve: {
                    oppiaineId: function($stateParams) {
                        return $stateParams.oppiaineId;
                    },
                    oppiaine: function(perusteId, Oppiaineet, oppiaineId) {
                        return Oppiaineet.get({ perusteId: perusteId, osanId: oppiaineId }).$promise;
                    }
                }
            })
            .state("root.selaus.perusopetus.sisallot", {
                url: "/sisallot/:oppiaineId?vlk&sisalto&osaaminen&valittu",
                template: require("eperusteet-esitys/views/vlkoppiaine.html"),
                controller: "epPerusopetusSisallotController",
                resolve: {
                    oppiaineId: function($stateParams) {
                        return $stateParams.oppiaineId;
                    },
                    oppiaine: function(perusteId, Oppiaineet, oppiaineId) {
                        return oppiaineId
                            ? Oppiaineet.get({ perusteId: perusteId, osanId: oppiaineId }).$promise
                            : null;
                    }
                }
            })
            .state("root.selaus.lukiokoulutus", {
                url: "/lukiokoulutus/:perusteId",
                template: require("eperusteet-esitys/views/lukiokoulutus.html"),
                controller: "epLukiokoulutusController",
                resolve: {
                    perusteId: function($stateParams) {
                        return $stateParams.perusteId;
                    },
                    peruste: function(perusteId, Perusteet) {
                        return Perusteet.get({ perusteId: perusteId }).$promise;
                    },
                    sisalto: function(peruste, $q, LukionOppiaineet, SuoritustapaSisalto) {
                        //TODO
                        return $q.when([]);
                        /*
            if (_.isArray(peruste.data)) {
              peruste = peruste.data[0];
            }
            var perusteId = peruste.id;
            return $q.all([
              peruste,
              LukionOppiaineet.query({perusteId: perusteId}).$promise,
              SuoritustapaSisalto.get({perusteId: perusteId, suoritustapa: 'lukiokoulutus'}).$promise,
            ]);*/
                    }
                }
            })
            .state("root.selaus.lukiokoulutus.sisallot", {
                url: "/sisallot/:oppiaineId?vlk&sisalto&osaaminen&valittu",
                template: require("eperusteet-esitys/views/vlkoppiaine.html"),
                controller: "epLukiokoulutusSisallotController",
                resolve: {
                    oppiaineId: function($stateParams) {
                        return $stateParams.oppiaineId;
                    },
                    oppiaine: function(perusteId, Oppiaineet, oppiaineId) {
                        return oppiaineId
                            ? Oppiaineet.get({ perusteId: perusteId, osanId: oppiaineId }).$promise
                            : null;
                    }
                }
            })
            .state("root.selaus.perusopetus.tekstikappale", {
                url: "/tekstikappale/:tekstikappaleId",
                template: require("eperusteet-esitys/views/tekstikappale.html"),
                controller: "epPerusopetusTekstikappaleController",
                resolve: {
                    tekstikappaleId: function($stateParams) {
                        return $stateParams.tekstikappaleId;
                    },
                    tekstikappale: function(tekstikappaleId, PerusteenOsat) {
                        return PerusteenOsat.getByViite({ viiteId: tekstikappaleId }).$promise;
                    },
                    lapset: function(sisalto, tekstikappaleId, epTekstikappaleChildResolver) {
                        return epTekstikappaleChildResolver.get(sisalto[4], tekstikappaleId);
                    }
                }
            })
            .state("root.selaus.lukiokoulutus.tekstikappale", {
                url: "/tekstikappale/:tekstikappaleId",
                template: require("eperusteet-esitys/views/tekstikappale.html"),
                controller: "epLukiokoulutusTekstikappaleController",
                resolve: {
                    tekstikappaleId: function($stateParams) {
                        return $stateParams.tekstikappaleId;
                    },
                    tekstikappale: function(tekstikappaleId, PerusteenOsat) {
                        return PerusteenOsat.getByViite({ viiteId: tekstikappaleId }).$promise;
                    },
                    lapset: function(sisalto, tekstikappaleId, epTekstikappaleChildResolver) {
                        return epTekstikappaleChildResolver.get(sisalto[4], tekstikappaleId);
                    }
                }
            });
    })
    .controller("EsiopetusListaController", function($scope, $state, Perusteet, Notifikaatiot) {
        $scope.lista = [];
        Perusteet.get(
            {
                tyyppi: "koulutustyyppi_15"
            },
            function(res) {
                if (res.sivuja > 1) {
                    console.warn("sivutusta ei ole toteutettu, tuloksia yli " + res.sivukoko);
                }
                $scope.lista = _(res.data)
                    .sortBy("voimassaoloLoppuu")
                    .reverse()
                    .each(function(eo: any) {
                        eo.$url = $state.href("root.selaus.esiopetus", { perusteId: eo.id });
                    })
                    .value();
            },
            Notifikaatiot.serverCb
        );
    })
    .controller("AikuisperusopetusListaController", function(
        $scope,
        $state,
        $q,
        Perusteet,
        Notifikaatiot,
        YleinenData
    ) {
        $scope.lista = [];
        $q.all([Perusteet.get({ tyyppi: "koulutustyyppi_17" }).$promise]).then(res => {
            if (res[0].sivuja > 1) {
                console.warn("sivutusta ei ole toteutettu, tuloksia yli " + res.sivukoko);
            }

            $scope.lista = _([].concat(res[0].data))
                .sortBy("voimassaoloLoppuu")
                .reverse()
                .each(peruste => {
                    peruste.$url = YleinenData.getPerusteEsikatseluHost() + "/aipe/" + peruste.id + "/tiedot";
                })
                .value();
        }, Notifikaatiot.serverCb);
    })
    .controller("PerusopetusListaController", function($scope, $state, $q, Perusteet, Notifikaatiot, YleinenData) {
        $scope.lista = [];
        $q
            .all([
                Perusteet.get({ tyyppi: "koulutustyyppi_16" }).$promise,
                Perusteet.get({ tyyppi: "koulutustyyppi_6" }).$promise
            ])
            .then(function(res) {
                if (res[0].sivuja > 1 || res[1].sivuja > 1) {
                    console.warn("sivutusta ei ole toteutettu, tuloksia yli " + res.sivukoko);
                }

                var cresult = [].concat(res[0].data).concat(res[1].data);

                $scope.lista = _(cresult)
                    .sortBy("voimassaoloLoppuu")
                    .reverse()
                    .each(function(peruste) {
                        peruste.$url = $state.href(
                            "root.selaus." + (YleinenData.isLisaopetus(peruste) ? "lisaopetus" : "perusopetus"),
                            {
                                perusteId: peruste.id
                            }
                        );
                    })
                    .value();
            }, Notifikaatiot.serverCb);
    })
    .controller("LukiokoulutusListaController", function($scope, $state, $q, Perusteet, Notifikaatiot, YleinenData) {
        $scope.lista = [];
        Perusteet.get(
            {
                tyyppi: "koulutustyyppi_2"
            },
            function(res) {
                if (res.sivuja > 1) {
                    console.warn("sivutusta ei ole toteutettu, tuloksia yli " + res.sivukoko);
                }
                $scope.lista = _(res.data)
                    .sortBy("voimassaoloLoppuu")
                    .reverse()
                    .each(function(eo: any) {
                        // "Kovakoodatut" Hostit haetaan yleiselta datalta
                        eo.$url = YleinenData.getPerusteEsikatseluHost() + "/lukio/" + eo.id;
                    })
                    .value();
            },
            Notifikaatiot.serverCb
        );
    })
    .controller("HakuCtrl", function(
        $scope,
        $rootScope,
        $state,
        Perusteet,
        Haku,
        $stateParams,
        YleinenData,
        koulutusalaService,
        Kieli,
        Profiili,
        Notifikaatiot
    ) {
        var pat: RegExp;
        // Viive, joka odotetaan, ennen kuin haku nimi muutoksesta lähtee serverille.
        var hakuViive = 300; //ms
        // Huom! Sivu alkaa UI:lla ykkösestä, serverillä nollasta.
        $scope.nykyinenSivu = 1;
        $scope.sivuja = 1;
        $scope.kokonaismaara = 0;
        $scope.koulutusalat = koulutusalaService.haeKoulutusalat();
        $scope.kirjanmerkinNimi = "";

        $scope.updateUrl = function() {
            var newParams = _.merge($stateParams, $scope.hakuparametrit);
            if (!_.isEmpty($scope.kirjanmerkinNimi)) {
                Profiili.asetaSuosikki(
                    $state.current.name,
                    $scope.kirjanmerkinNimi,
                    function() {
                        $scope.kirjanmerkinNimi = "";
                        $state.go($state.current.name, newParams, { reload: false });
                    },
                    newParams
                );
            } else {
                Notifikaatiot.varoitus("kirjanmerkilla-pitaa-olla-nimi");
            }
        };

        function setHakuparametrit() {
            $scope.hakuparametrit = _.merge(_.merge(Haku.getHakuparametrit($state.current.name), $stateParams), {
                kieli: Kieli.getSisaltokieli()
            });
        }
        setHakuparametrit();

        $scope.koulutustyypit = YleinenData.ammatillisetkoulutustyypit;

        $scope.tyhjenna = function() {
            $scope.nykyinenSivu = 1;
            $scope.hakuparametrit = Haku.resetHakuparametrit($state.current.name);
            setHakuparametrit();
            $scope.haePerusteet($scope.nykyinenSivu);
        };

        var hakuVastaus = function(vastaus) {
            $scope.perusteet = vastaus;
            $scope.nykyinenSivu = $scope.perusteet.sivu + 1;
            $scope.hakuparametrit.sivukoko = $scope.perusteet.sivukoko;
            $scope.sivuja = $scope.perusteet.sivuja;
            $scope.kokonaismaara = $scope.perusteet.kokonaismäärä;
            $scope.sivut = _.range(0, $scope.perusteet.sivuja);
            pat = new RegExp("(" + $scope.hakuparametrit.nimi + ")", "i");
        };

        $scope.pageChanged = function() {
            $scope.haePerusteet($scope.nykyinenSivu);
        };

        /**
     * Hakee sivun serveriltä.
     * @param {number} sivu UI:n sivunumero, alkaa ykkösestä.
     */
        $scope.haePerusteet = function(sivu) {
            $scope.hakuparametrit.sivu = sivu - 1;
            Haku.setHakuparametrit($state.current.name, $scope.hakuparametrit);
            Perusteet.get($scope.hakuparametrit, hakuVastaus, function(virhe) {
                if (virhe.status === 404) {
                    hakuVastaus(virhe.data);
                }
            });
        };

        $scope.sivujaYhteensa = function() {
            return Math.max($scope.sivuja, 1);
        };

        $scope.hakuMuuttui = _.debounce(_.bind($scope.haePerusteet, $scope, 1), hakuViive, { leading: false });

        $scope.korosta = function(otsikko) {
            if ($scope.hakuparametrit.nimi === null || $scope.hakuparametrit.nimi.length < 3) {
                return otsikko;
            }
            return otsikko.replace(pat, "<b>$1</b>");
        };

        $scope.valitseKieli = function(nimi) {
            return YleinenData.valitseKieli(nimi);
        };

        $scope.koulutusalaMuuttui = function() {
            $scope.hakuparametrit.opintoala = "";
            if ($scope.hakuparametrit.koulutusala !== "") {
                $scope.opintoalat = (_.findWhere($scope.koulutusalat, {
                    koodi: $scope.hakuparametrit.koulutusala
                }) as any).opintoalat;
            } else {
                $scope.opintoalat = [];
            }
            $scope.hakuMuuttui();
        };
        $scope.koulutusalaMuuttui();

        $scope.koulutusalaNimi = function(koodi) {
            return koulutusalaService.haeKoulutusalaNimi(koodi);
        };

        $scope.piilotaKoulutustyyppi = function() {
            return $state.current.name === "root.selaus.ammatillinenperuskoulutus";
        };

        $scope.$on("changed:sisaltokieli", $scope.tyhjenna);
    });

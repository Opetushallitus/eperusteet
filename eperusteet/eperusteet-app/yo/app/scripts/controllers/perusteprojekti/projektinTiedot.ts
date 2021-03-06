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
import { saveAs } from 'file-saver';


angular
    .module("eperusteApp")
    .controller("ProjektiTiedotSisaltoModalCtrl", function(
        $scope,
        $uibModalInstance,
        YleinenData,
        PerusteprojektiResource,
        Notifikaatiot,
        Perusteet,
        pohja
    ) {
        $scope.ominaisuudet = {};
        $scope.suoritustavat = [];
        $scope.nykyinen = 1;
        $scope.itemsPerPage = YleinenData.defaultItemsInModal;
        $scope.totalItems = 0;

        var dhaku = _.debounce(
            function(haku) {
                Perusteet.internal(
                    {
                        nimi: haku,
                        sivu: $scope.nykyinen - 1,
                        sivukoko: $scope.itemsPerPage,
                        tila: "valmis",
                        perusteTyyppi: pohja ? "pohja" : "normaali",
                        koulutusvienti: "kaikki"
                    },
                    function(perusteet) {
                        $scope.perusteet = perusteet.data;
                        $scope.totalItems = perusteet["kokonaismäärä"];
                        $scope.itemsPerPage = perusteet.sivukoko;
                    }
                );
            },
            300,
            { maxWait: 1000 }
        );

        $scope.haku = function(haku) {
            dhaku(haku);
        };
        $scope.haku("");

        $scope.valitseSivu = function(sivu) {
            if (sivu > 0 && sivu <= Math.ceil($scope.totalItems / $scope.itemsPerPage)) {
                $scope.nykyinen = sivu;
            }
            $scope.haku($scope.syote);
        };

        $scope.takaisin = function() {
            $scope.projekti = null;
            $scope.peruste = null;
            $scope.ominaisuudet = {};
        };
        $scope.ok = function(peruste) {
            $uibModalInstance.close(peruste);
        };
        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };
    })
    .controller("ProjektinTuontiCtrl", function(
        $scope,
        $state,
        Api,
        Notifikaatiot,
    ) {
        $scope.loadFile = () => {
            const files = (document.getElementById("tiedostohaku") as any).files;
            if (files.length === 0) {
                Notifikaatiot.varoitus("tiedosto-puuttuu");
                return;
            }

            const file = files.item(0);

            const formData = new FormData();
            const req = new XMLHttpRequest();
            req.onreadystatechange = () => {
                if (req.readyState === XMLHttpRequest.DONE) {
                    if (req.status === 200) {
                        Notifikaatiot.onnistui("tallennettu");
                        $state.go("root.admin.perusteprojektit");
                    } else {
                        console.error(req.status);
                        Notifikaatiot.fataali("tiedosto-lahetys-epaonnistui");
                    }
                }
            };

            formData.append("file", file);
            req.open("POST", Api.one("maintenance/import").getRequestedUrl());
            req.send(formData);
        };
    })
    .controller("ProjektinTiedotCtrl", function(
        $scope,
        $state,
        $stateParams,
        $timeout,
        $translate,
        $uibModal,
        Api,
        Editointikontrollit,
        Notifikaatiot,
        Organisaatioryhmat,
        PerusteProjektiService,
        Perusteet,
        PerusteprojektiResource,
        YleinenData,
        perusteprojektiTiedot,
    ) {
        PerusteProjektiService.watcher($scope, "projekti");
        $scope.lang = $translate.use() || $translate.preferredLanguage();
        $scope.editEnabled = false;
        $scope.$ryhmaNimi = "";
        $scope.toteutukset = YleinenData.toteutukset;
        var originalProjekti = null;

        var editingCallbacks = {
            edit: function() {
                originalProjekti = PerusteProjektiService.get();
            },
            save: function() {
                $scope.tallennaPerusteprojekti();
            },
            validate: function() {
                return $scope.perusteprojektiForm.$valid;
            },
            cancel: function() {
                $scope.projekti = originalProjekti;
            },
            notify: function(mode) {
                $scope.editEnabled = mode;
            }
        };
        Editointikontrollit.registerCallback(editingCallbacks);

        $scope.pohja = function() {
            return (
                $state.is("root.perusteprojektiwizard.pohja") || ($scope.peruste && $scope.peruste.tyyppi === "pohja")
            );
        };
        $scope.wizardissa = function() {
            return $state.is("root.perusteprojektiwizard.tiedot") || $state.is("root.perusteprojektiwizard.pohja");
        };

        $scope.voiMuokata = function() {
            // TODO Vain omistaja/sihteeri voi muokata
            return true;
        };

        $scope.muokkaa = function() {
            Editointikontrollit.startEditing();
        };

        $scope.lataaProjektiData = async function() {
            location.href = await Api.one("maintenance/export/" + $scope.projekti._peruste).getRequestedUrl();
        };

        $scope.puhdistaValinta = function() {
            PerusteProjektiService.clean();
            if ($scope.wizardissa()) {
                perusteprojektiTiedot.cleanData();
                $scope.peruste = undefined;
                $scope.projekti = {};
            }
        };
        $scope.puhdistaValinta();

        $scope.projekti = perusteprojektiTiedot.getProjekti();
        $scope.projekti.laajuusYksikko = $scope.projekti.laajuusYksikko || "OSAAMISPISTE";
        const tyyppiIsLukio = tyyppi =>
            _.any(["koulutustyyppi_2", "koulutustyyppi_23", "koulutustyyppi_14"], i => i === tyyppi);
        $scope.isLukiokoulutus = () => $scope.peruste && tyyppiIsLukio($scope.peruste.koulutustyyppi);

        $scope.tabs = [
            {
                otsikko: "projekti-perustiedot",
                url: "views/partials/perusteprojekti/perustiedot.html"
            }
        ];
        if (!$scope.pohja() && !$scope.isOpas) {
            $scope.tabs.push({
                otsikko: "projekti-toimikausi",
                url: "views/partials/perusteprojekti/toimikausi.html"
            });
        }

        if ($scope.projekti.ryhmaOid) {
            Organisaatioryhmat.yksi({ oid: $scope.projekti.ryhmaOid }, function(res) {
                $scope.$ryhmaNimi = res.nimi;
            });
        }

        $scope.haeRyhma = function() {
            $uibModal
                .open({
                    template: require("views/modals/tuotyoryhma.html"),
                    controller: "TyoryhmanTuontiModalCtrl"
                })
                .result.then(function(ryhma) {
                    $scope.projekti.ryhmaOid = ryhma.oid;
                    $scope.$ryhmaNimi = ryhma.nimi;
                });
        };

        $scope.mergeProjekti = function(tuoPohja) {
            PerusteProjektiService.mergeProjekti($scope.projekti, tuoPohja).then(function(peruste, projekti) {
                _.merge($scope.projekti, projekti);
                $scope.peruste = peruste;
            });
        };

        $scope.tallennaPerusteprojekti = function() {
            $scope.perusteprojektiForm.$saving = true;
            let projekti = PerusteProjektiService.get();
            if ($scope.isLukiokoulutus()) {
                //Lukiokoulutus
                projekti.laajuusYksikko = "KURSSI";
            }

            if (projekti.id) {
                delete projekti.koulutustyyppi;
                delete projekti.laajuusYksikko;
            } else {
                projekti.id = null;
                if (projekti.isReforminMukainen) {
                    projekti.suoritustavat = ["reformi"];
                }
            }

            if ($scope.pohja()) {
                projekti = _.merge(
                    _.pick(
                        projekti,
                        "id",
                        "nimi",
                        "koulutustyyppi",
                        "ryhmaOid",
                        "perusteId",
                        "reforminMukainen",
                        "esikatseltavissa",
                        "_peruste"
                    ),
                    {
                        tyyppi: "pohja"
                    }
                );
            }

            PerusteprojektiResource.update(
                projekti,
                function(vastaus) {
                    if ($scope.wizardissa()) {
                        PerusteProjektiService.goToProjektiState(vastaus, projekti);
                    } else {
                        Notifikaatiot.onnistui("tallennettu");
                        $scope.projekti = vastaus;
                        perusteprojektiTiedot.setProjekti(vastaus);
                        PerusteProjektiService.update();
                    }
                },
                Notifikaatiot.serverCb
            );
        };
    })
    .controller("TyoryhmanTuontiModalCtrl", function(
        $scope,
        $uibModalInstance,
        $translate,
        Organisaatioryhmat,
        Algoritmit
    ) {
        $scope.haetaan = true;
        $scope.error = false;
        $scope.rajaus = "";
        $scope.lang = "nimi." + $translate.use() || $translate.preferredLanguage();

        $scope.totalItems = 0;
        $scope.itemsPerPage = 10;
        $scope.nykyinen = 1;

        Organisaatioryhmat.get(
            function(res) {
                $scope.haetaan = false;
                $scope.ryhmat = _(res).value();
                $scope.totalItems = _.size($scope.ryhmat);
            },
            function() {
                $scope.haetaan = false;
                $scope.error = true;
            }
        );

        $scope.paivitaRajaus = function(rajaus) {
            $scope.rajaus = rajaus;
        };

        $scope.rajaa = function(ryhma) {
            return Algoritmit.match($scope.rajaus, ryhma.nimi);
        };

        $scope.valitseSivu = function(sivu) {
            if (sivu > 0 && sivu <= Math.ceil($scope.totalItems / $scope.itemsPerPage)) {
                $scope.nykyinen = sivu;
            }
        };

        $scope.valitse = $uibModalInstance.close;
        $scope.peruuta = $uibModalInstance.dismiss;
    });

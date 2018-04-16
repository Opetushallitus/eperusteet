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
    .controller("MuodostumisryhmaModalCtrl", function(
        $scope,
        $uibModalInstance,
        ryhma,
        vanhempi,
        suoritustapa,
        leikelauta,
        Varmistusdialogi,
        YleinenData,
        Koodisto,
        Utils,
        peruste
    ) {
        $scope.vanhempi = vanhempi;
        $scope.leikelauta = leikelauta;
        $scope.suoritustapa = suoritustapa;
        $scope.osaamisalat = [{}].concat(
            _.map(_.clone(peruste.osaamisalat), function(oa: any) {
                return {
                    osaamisalakoodiArvo: oa.arvo,
                    osaamisalakoodiUri: oa.uri,
                    nimi: oa.nimi
                };
            })
        );
        $scope.roolit = _.map(YleinenData.rakenneRyhmaRoolit, function(rooli) {
            return { value: rooli, label: rooli };
        });

        $scope.luonti = !_.isObject(ryhma);

        $scope.Osaamisala = {
            valitse: function(oa) {
                $scope.ryhma.osaamisala = oa;
                if (oa && oa.nimi) {
                    $scope.ryhma.nimi = _.cloneDeep(oa.nimi);
                }
            }
        };

        (function setupRyhma() {
            $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
            $scope.ryhma.rooli = $scope.ryhma.rooli || YleinenData.rakenneRyhmaRoolit[0];
            $scope.ryhma.osaamisala =
                ryhma && ryhma.osaamisala && ryhma.osaamisala.osaamisalakoodiUri ? ryhma.osaamisala : null;
            if (!$scope.ryhma.nimi) {
                $scope.ryhma.nimi = {};
            }
            if (!$scope.ryhma.kuvaus) {
                $scope.ryhma.kuvaus = {};
            }
            $scope.ryhma.pakollinen = ryhma && ryhma.pakollinen ? ryhma.pakollinen : false;
        })();

        $scope.lisaaTutkintoKoodi = Koodisto.modaali(
            function(koodi) {
                $scope.ryhma.vieras = {
                    nimi: koodi.nimi,
                    arvo: koodi.koodiArvo,
                    uri: koodi.koodiUri
                };
            },
            {
                ylarelaatioTyyppi: _.constant(peruste.koulutustyyppi),
                tyyppi: _.constant("koulutus")
            }
        );

        $scope.ok = function(uusiryhma) {
            if (uusiryhma) {
                if (uusiryhma.osat === undefined) {
                    uusiryhma.osat = [];
                }
                if (uusiryhma.muodostumisSaanto && uusiryhma.muodostumisSaanto.laajuus) {
                    var ml = uusiryhma.muodostumisSaanto.laajuus;
                    ml.maksimi = ml.minimi && (!ml.maksimi || ml.minimi > ml.maksimi) ? ml.minimi : ml.maksimi;
                }

                if (!_.isEmpty(uusiryhma.osaamisala)) {
                    uusiryhma.rooli = "osaamisala";
                } else if (uusiryhma.rooli === "osaamisala") {
                    uusiryhma.rooli = "määritelty";
                }
            }
            $uibModalInstance.close(uusiryhma);
        };

        $scope.poista = function() {
            Varmistusdialogi.dialogi({
                otsikko: "poistetaanko-ryhma",
                successCb: function() {
                    $scope.ok(null);
                }
            })();
        };

        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };

        $scope.$watch("ryhma.rooli", function(rooli) {
            if (rooli !== "osaamisala") {
                $scope.ryhma.osaamisala = null;
            }
        });
    });

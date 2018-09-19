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
    .directive("projektiryhmaHenkilot", function() {
        return {
            template: require("views/partials/projektiTyoryhmanKayttajat.html"),
            restrict: "E",
            scope: {
                tyyppi: "=",
                tyoryhmat: "=",
                ryhma: "="
            },
            controller: function($scope, ColorCalculator, kayttajaToiminnot) {
                $scope.nimikirjaimet = kayttajaToiminnot.nimikirjaimet;
                $scope.filterRyhma = function(ryhma) {
                    return _.some(ryhma, $scope.filterJasen);
                };
                $scope.filterJasen = function(jasen) {
                    return $scope.tyyppi === "kaikki" || $scope.tyoryhmat[$scope.tyyppi][jasen.oidHenkilo];
                };
                $scope.styleFor = function(jasen) {
                    return jasen.color
                        ? {
                              "background-color": "#" + jasen.color,
                              color: ColorCalculator.readableTextColorForBg(jasen.color)
                          }
                        : {};
                };
            }
        };
    })
    .controller("ProjektiryhmaModalCtrl", function($scope, $uibModalInstance) {
        $scope.ok = function() {
            $uibModalInstance.close();
        };
        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };
    })
    .controller("ProjektiryhmaCtrl", function(
        $scope,
        $uibModal,
        $stateParams,
        Notifikaatiot,
        Projektiryhma,
        PerusteProjektiService,
        kayttajaToiminnot,
        PerusteprojektiTyoryhmat
    ) {
        PerusteProjektiService.watcher($scope, "projekti");

        $scope.ryhma = {};
        $scope.tyyppi = "kaikki";
        $scope.lataa = true;
        $scope.error = false;

        $scope.vaihdaTyyppi = function(tyyppi) {
            $scope.tyyppi = tyyppi;
        };

        function errorCb(err) {
            Notifikaatiot.serverCb(err);
            $scope.error = true;
            $scope.lataa = false;
        }

        Projektiryhma.jasenetJaTyoryhmat(
            $stateParams.perusteProjektiId,
            function(re) {
                $scope.jasenet = re.jasenet;
                $scope.tyoryhmat = re.tyoryhmat;
                $scope.ryhma = re.ryhma;
                $scope.lataa = false;
            },
            errorCb
        );

        $scope.muokkaaTyoryhmaa = function(ryhma) {
            $uibModal
                .open({
                    template: require("views/modals/lisaaTyoryhma.html"),
                    controller: "LuoTyoryhmaModalCtrl",
                    resolve: {
                        ryhmat: function() {
                            return _.clone($scope.tyoryhmat) || {};
                        },
                        ryhma: function() {
                            return _.clone($scope.tyoryhmat[ryhma]) || null;
                        },
                        jasenet: function() {
                            return _.clone($scope.jasenet);
                        }
                    }
                })
                .result.then(function(muokattu) {
                    if (muokattu) {
                        PerusteprojektiTyoryhmat.save(
                            {
                                id: $stateParams.perusteProjektiId,
                                nimi: muokattu.nimi
                            },
                            muokattu.jasenet,
                            function(lisatyt) {
                                delete $scope.tyoryhmat[ryhma];
                                $scope.tyyppi = (_.first(lisatyt) as any).nimi;
                                $scope.tyoryhmat[$scope.tyyppi] = _.zipObject(_.map(lisatyt, "kayttajaOid"), lisatyt);
                            },
                            errorCb
                        );
                    } else {
                        PerusteprojektiTyoryhmat.delete(
                            {
                                id: $stateParams.perusteProjektiId,
                                nimi: ryhma
                            },
                            function() {
                                delete $scope.tyoryhmat[ryhma];
                                $scope.tyyppi = "kaikki";
                            },
                            Notifikaatiot.serverCb
                        );
                    }
                });
        };
    })
    .controller("LuoTyoryhmaModalCtrl", function($scope, $uibModalInstance, Varmistusdialogi, ryhma, ryhmat, jasenet) {
        $scope.uusi = ryhma ? false : true;

        $scope.ryhma = {
            nimi: ryhma ? (_.first(_.values(ryhma)) as any).nimi : "",
            jasenet: ryhma ? _.keys(ryhma) : []
        };

        $scope.validoiRyhma = function(nimi) {
            $scope.virheellinenNimi = ryhmat[nimi] ? true : false;
        };

        $scope.jasenet = jasenet || [];
        $scope.jasenMap = _.zipObject(_.map($scope.jasenet, "oidHenkilo"), $scope.jasenet);
        $scope.to = [];
        $scope.from = [];

        $scope.lista = [];
        var lisatyt = ryhma || {};

        if (!$scope.ryhma) {
            $scope.ryhma = {};
            $scope.ryhma.jasenet = [];
        }

        $scope.poistaLisatyt = function(t) {
            return !lisatyt[t.oidHenkilo];
        };
        $scope.apulajittelija = function(oid) {
            return $scope.jasenMap[oid].sukunimi;
        };

        $scope.siirraOikealle = function(from) {
            _.forEach(from, function(t) {
                $scope.ryhma.jasenet.push(t.oidHenkilo);
                lisatyt[t.oidHenkilo] = true;
            });
            from = [];
        };

        $scope.siirraVasemmalle = function(to) {
            $scope.ryhma.jasenet = _.remove($scope.ryhma.jasenet, function(j: any) {
                var remove = !_.some(to, function(t: any) {
                    return t === j;
                });
                if (!remove) {
                    delete lisatyt[j];
                }
                return remove;
            });
            to = [];
        };

        $scope.ok = _.partial($uibModalInstance.close, $scope.ryhma);
        $scope.peruuta = $uibModalInstance.dismiss;
        $scope.poista = function() {
            Varmistusdialogi.dialogi({
                otsikko: "poistetaanko-tyoryhma",
                successCb: _.partial($uibModalInstance.close, null)
            })();
        };
    })
    .service("kayttajaToiminnot", function() {
        this.nimikirjaimet = function(nimi) {
            return _.reduce(
                nimi.split(" "),
                function(memo, osa) {
                    return memo + (osa ? osa[0] : "");
                },
                ""
            ).toUpperCase();
        };
    });

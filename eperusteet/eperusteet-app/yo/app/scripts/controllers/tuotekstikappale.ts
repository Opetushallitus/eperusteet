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

"use strict";
/* global _ */

angular
    .module("eperusteApp")
    .controller("TuoTekstikappale", function(
        $q,
        $scope,
        $uibModalInstance,
        Notifikaatiot,
        peruste,
        suoritustapa,
        PerusteenRakenne,
        SuoritustapaSisalto,
        YleinenData,
        Perusteet,
        Algoritmit,
        Kaanna,
        OmatPerusteprojektit,
        Kieli
    ) {
        var sisallot = {};
        $scope.nykyinenPeruste = peruste;
        $scope.perusteet = [];
        $scope.luonnosPerusteet = [];
        $scope.sivuja = 0;
        $scope.sivu = 0;
        $scope.valittuPeruste = null;
        $scope.kaikkiValittu = null;
        $scope.valitut = 0;
        $scope.search = {
            term: "",
            changed: function() {
                $scope.paginate.current = 1;
            },
            filterFn: function(item) {
                return Algoritmit.match($scope.search.term, item.perusteenOsa.nimi);
            }
        };

        $scope.paginate = {
            perPage: 10,
            current: 1
        };

        $scope.fetchOmat = () => {
            if ($scope.luonnokset) {
                OmatPerusteprojektit.query({}, vastaus => {
                    $scope.luonnosPerusteet = _(vastaus)
                        .filter(pp => pp.diaarinumero)
                        .map("peruste")
                        .filter(p => p.nimi)
                        .filter(p => p.id !== $scope.nykyinenPeruste.id)
                        .value();

                    $scope.haku("");
                });
            } else {
                $scope.luonnosPerusteet = [];
                $scope.haku("");
            }
        };

        $scope.orderFn = function(item) {
            return Kaanna.kaanna(item.perusteenOsa.nimi).toLowerCase();
        };

        $scope.updateTotal = function() {
            $scope.valitut = _.size(_.filter($scope.valittuPeruste.$sisalto, "$valittu"));
        };

        $scope.toggleKaikki = function(valinta) {
            _.each($scope.valittuPeruste.$sisalto, function(tulos) {
                tulos.$valittu = false;
                if ($scope.search.term) {
                    if ($scope.search.filterFn(tulos)) {
                        tulos.$valittu = valinta;
                    }
                } else {
                    tulos.$valittu = valinta;
                }
            });
            $scope.updateTotal();
        };

        $scope.haku = function(haku) {
            PerusteenRakenne.haePerusteita(haku, function(res) {
                $scope.perusteet = _($scope.luonnosPerusteet)
                    .filter(p => {
                        return p.nimi[Kieli.getSisaltokieli()].indexOf(haku) !== -1;
                    })
                    .concat(res.data)
                    .uniq(p => p.id)
                    .value();
                $scope.sivuja = res.sivuja;
                $scope.sivu = res.sivu;
            });
        };
        $scope.haku("");

        $scope.vaihdaSuoritustapa = function(valinta) {
            $scope.valitut = 0;
            _.each($scope.valittuPeruste.$sisalto, function(s) {
                s.$valittu = 0;
            });
            $scope.valittuPeruste.$sisalto = sisallot[valinta.suoritustapakoodi];
        };

        $scope.valitse = function(valittuPeruste) {
            Perusteet.get(
                { perusteId: valittuPeruste.id },
                function(peruste) {
                    $scope.valittuPeruste = peruste;

                    var oletusSuoritustapa = YleinenData.koulutustyyppiInfo[peruste.koulutustyyppi].oletusSuoritustapa;
                    if (oletusSuoritustapa !== "ops" && oletusSuoritustapa !== "naytto") {
                        peruste.suoritustavat = peruste.suoritustavat || [];
                        peruste.suoritustavat.push({ suoritustapakoodi: oletusSuoritustapa });
                    }

                    $scope.valittuSuoritustapa = _.first(peruste.suoritustavat).suoritustapakoodi;
                    $q
                        .all(
                            _.map(peruste.suoritustavat, function(st) {
                                return SuoritustapaSisalto.get({
                                    perusteId: valittuPeruste.id,
                                    suoritustapa: st.suoritustapakoodi
                                }).$promise;
                            })
                        )
                        .then(function(res) {
                            sisallot = _.zipObject(
                                _.map(peruste.suoritustavat, "suoritustapakoodi"),
                                _.map(res, function(hst) {
                                    return _.reject(hst.lapset, function(lapsi) {
                                        return lapsi.perusteenOsa.tunniste === "rakenne";
                                    });
                                })
                            );
                            if (_.indexOf(peruste.suoritustavat, suoritustapa) === -1) {
                                suoritustapa = oletusSuoritustapa;
                            }
                            $scope.valittuPeruste.$sisalto = sisallot[suoritustapa];
                        }, Notifikaatiot.serverCb);
                },
                Notifikaatiot.serverCb
            );
        };

        $scope.takaisin = function() {
            $scope.valittuPeruste = null;
            $scope.search.term = "";
            $scope.paginate.current = 1;
            $scope.valitut = 0;
        };
        $scope.peru = function() {
            $uibModalInstance.dismiss();
        };
        $scope.ok = function() {
            $uibModalInstance.close(_.filter($scope.valittuPeruste.$sisalto, "$valittu"));
        };
    });

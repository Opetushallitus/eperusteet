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
    .controller("EsiopetusController", function(
        $scope,
        $state,
        Algoritmit,
        sisalto,
        Notifikaatiot,
        PerusteenOsat,
        YleinenData
    ) {
        $scope.peruste = sisalto[0];
        $scope.otsikko = YleinenData.isEsiopetus($scope.peruste) ? "esiopetus" : "lisaopetus";
        var tekstisisalto = sisalto[1];

        function valitseTekstisisalto(item, section) {
            $scope.valittuTekstisisalto = item.$osa;
            PerusteenOsat.get(
                { osanId: item.$osa._perusteenOsa },
                function(res) {
                    if (section) {
                        _.each(section.items, function(osa) {
                            osa.$selected = false;
                        });
                    }
                    item.$selected = true;
                    $scope.valittuTekstisisalto.teksti = res.teksti;
                },
                Notifikaatiot.serverCb
            );
        }

        function rakennaTekstisisalto() {
            var sisalto = [];
            Algoritmit.kaikilleLapsisolmuille(tekstisisalto, "lapset", function(osa, depth) {
                if (depth >= 0) {
                    sisalto.push({
                        $osa: osa,
                        label: osa.perusteenOsa.nimi,
                        depth: depth
                    });
                }
            });
            if (!_.isEmpty(sisalto)) {
                _.first(sisalto).$selected = true;
                valitseTekstisisalto(_.first(sisalto), undefined);
            }
            return sisalto;
        }

        $scope.navi = {
            header: "perusteen-sisalto",
            showOne: true,
            sections: [
                {
                    $open: true,
                    id: "suunnitelma",
                    include: "views/partials/perusopetustekstisisalto.html",
                    items: rakennaTekstisisalto(),
                    title: "tekstisisalto",
                    update: valitseTekstisisalto
                }
            ]
        };
    });

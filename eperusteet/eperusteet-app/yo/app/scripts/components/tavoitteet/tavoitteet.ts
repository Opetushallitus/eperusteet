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

interface LokalisoituTeksti {
    kieli: string;
    teksti: string;
}

interface TekstiPalanen {
    id: number;
    teksti: LokalisoituTeksti;
    tunniste: any;
}

interface Kohdealue {
    nimi: TekstiPalanen;
    kuvaus: TekstiPalanen;
    oppiaineet: Array<any>;
    opetuksenTavoitteet: Array<any>;
}

import * as angular from "angular";
import * as _ from "lodash";

angular.module("eperusteApp").directive("aipeTavoitteet", () => {
    return {
        templateUrl: require("scripts/components/tavoitteet/tavoitteet.pug"),
        restrict: "E",
        scope: {
            model: "=",
            laajaalaiset: "=",
            vaihe: "=",
            editing: "="
        },
        controller: function($scope, Kaanna) {
            _.each($scope.model, tavoite => {
                tavoite.$accordionOpen = true;

                // Alustetaan valitut kohdealueet
                if (tavoite.kohdealueet && tavoite.kohdealueet.length > 0) {
                    tavoite.$valittuKohdealue = _.find(
                        $scope.vaihe.opetuksenKohdealueet,
                        ka => ka.id.toString() === tavoite.kohdealueet[0]
                    );
                }

                // Alustetaan laaja-alaiset
                tavoite.$osaaminen = _.map($scope.laajaalaiset, generateArraySetter(tavoite.laajattavoitteet));
            });

            function generateArraySetter(findFrom, manipulator = _.noop) {
                return item => {
                    const found = _.find(findFrom, findItem => parseInt(findItem, 10) === item.id);
                    item = _.clone(item);
                    item.$hidden = !found;
                    item.teksti = item.kuvaus;
                    return manipulator(item) || item;
                };
            }

            $scope.toggleAll = () => {
                _.each($scope.model, tavoite => {
                    tavoite.$accordionOpen = !tavoite.$accordionOpen;
                });
            };

            $scope.treeOptions = {};

            $scope.lisaaTavoite = () => {
                $scope.model.push({
                    $accordionOpen: true,
                    $editing: false,
                    sisaltoalueet: [],
                    laajattavoitteet: [],
                    kohdealueet: [],
                    arvioinninkohteet: [],
                    $osaaminen: _.map($scope.laajaalaiset, generateArraySetter([]))
                });
            };

            $scope.poistaTavoite = tavoite => {
                _.remove($scope.model, tavoite);
            };

            $scope.asetaKohdealue = tavoite => {
                const valittu = tavoite.$valittuKohdealue ? [tavoite.$valittuKohdealue] : [];
                tavoite.kohdealueet = _(valittu)
                    .map(item => item.id)
                    .value();
            };

            $scope.kaannaKohdealue = ka => {
                return Kaanna.kaanna(ka.nimi);
            };

            $scope.poistaValittuKohdealue = tavoite => {
                tavoite.$valittuKohdealue = undefined;
            };

            $scope.addArvioinninKohde = tavoite => {
                tavoite.arvioinninkohteet.push({});
            };

            $scope.removeArvioinninKohde = (tavoite, index) => {
                tavoite.arvioinninkohteet.splice(index, 1);
            };
        }
    };
});

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
import * as _ from "lodash";

namespace Controllers {
    export const kaikkiKommentit = ($q, $scope, $location, kommentit) => {
        $scope.kommentit = _(kommentit)
            .sortBy("muokattu")
            .reverse()
            .value();

        const uniqueOids = _(kommentit)
            .map("muokkaaja")
            .uniq()
            .value();

        $scope.kayttajat = {};

        // Ei onnistu vielä koska ei tarpeeksi tietoa tilasta
        $scope.goToKommenttiLocation = Logic.getKommenttiUrl;

        $q.all(_.map(uniqueOids, oid => Endpoints.getKayttajaByOid(oid))).then(users => {
            const oids = {};
            _.each(users, (user, idx) => {
                oids[uniqueOids[idx]] = Logic.getKayttajaNimi(user);
            });
            $scope.kayttajat = oids;
        });
    };
}

angular.module("eperusteApp").config($stateProvider =>
    $stateProvider.state("root.perusteprojekti.kommentit", {
        url: "/kommentit",
        template: require("views/partials/perusteprojekti/kommentit.html"),
        controller: Controllers.kaikkiKommentit,
        resolve: {
            kommentit: ($stateParams, Api) => {
                return Api.all("kommentit")
                    .one("perusteprojekti", $stateParams.perusteProjektiId)
                    .getList();
            }
        },
        onEnter: [
            "PerusteProjektiSivunavi",
            function(PerusteProjektiSivunavi) {
                PerusteProjektiSivunavi.setVisible(false);
            }
        ]
    })
);

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

angular.module("eperusteApp").config($stateProvider => {
    $stateProvider.state("root.tekstihaku", {
        url: "/tekstihaku",
        template: require("scripts/states/tekstihaku/view.pug"),
        resolve: { },
        controller: (
            $scope,
            $q,
            $state,
            $stateParams,
            $timeout,
            Api
        ) => {
            $scope.query = {
                teksti: ""
            };

            $scope.results = {};

            function processResults(query, results) {
                const regex = new RegExp(query.teksti, "g");
                for (const result of results.data) {
                    result.$$tekstit = _((result.teksti || "").split("\\n"))
                        .filter(val => _.includes(val, query.teksti))
                        .map(val => val.replace(regex, "<span class=\"match\">$&</span>"))
                        .value();
                }
                return results;
            }

            $scope.search = _.debounce(async (query, tries = 3) => {
                if (!query.teksti) {
                    return;
                }

                try {
                    const response = await Api.all("experimental").customGET("tekstihaku", query);
                    $scope.results = processResults(query, response.plain());
                }
                catch (err) {
                    if (err.status === 429) {
                        if (tries > 0) {
                            $scope.search(query, tries - 1);
                        }
                    }
                    console.log(err);
                }
            }, 300);

            $scope.updatePage = (sivu) => {
                $scope.search({
                    ...$scope.query,
                    sivu: sivu
                });
            };

            // TODO: poista
            $timeout(() => {
                $scope.query.teksti = "a"
                $scope.search($scope.query);
            }, 300);

        },
    });
});


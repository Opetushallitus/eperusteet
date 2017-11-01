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
    $stateProvider.state("root.perusteprojekti.suoritustapa.aipeosalistaus", {
        url: "/aipeosat/:osanTyyppi",
        template: require("scripts/states/perusteprojekti/suoritustapa/aipeosalistaus/view.pug"),
        resolve: {
            perusteprojektit: Api => Api.all("perusteprojektit"),
            perusteprojekti: (perusteprojektit, $stateParams) =>
                perusteprojektit.one($stateParams.perusteProjektiId).get(),
            perusteet: Api => Api.all("perusteet"),
            peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
            aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
            vaiheet: aipeopetus => aipeopetus.all("vaiheet").getList(),
            laajaalaiset: aipeopetus => aipeopetus.all("laajaalaiset").getList()
        },
        controller: (
            $scope,
            $state,
            $stateParams,
            AIPEService,
            Editointikontrollit,
            virheService,
            laajaalaiset,
            vaiheet,
            Notifikaatiot
        ) => {
            const createUrl = value =>
                $state.href("root.perusteprojekti.suoritustapa.aipeosaalue", {
                    suoritustapa: $stateParams.suoritustapa,
                    osanTyyppi: $stateParams.osanTyyppi,
                    osanId: value.id,
                    tabId: 0
                });

            $scope.isSorting = false;
            $scope.canSort = _.includes(["osaaminen", "vaiheet"], $stateParams.osanTyyppi);

            let backup = laajaalaiset.clone();

            $scope.sisaltoState = _.find(AIPEService.sisallot, {
                tyyppi: $stateParams.osanTyyppi
            });

            if (!$scope.sisaltoState) {
                virheService.virhe("virhe-sivua-ei-löytynyt");
            }

            const osaAlueet = $stateParams.osanTyyppi === AIPEService.OSAAMINEN ? laajaalaiset : vaiheet;
            for (const oa of osaAlueet) {
                oa.$$url = createUrl(oa);
            }
            $scope.osaAlueet = osaAlueet;

            $scope.hasMuokattu = !_.isEmpty($scope.osaAlueet) && !!(_.first($scope.osaAlueet) as any).muokattu;

            $scope.options = {
                extrafilter: null
            };

            $scope.sort = () => {
                Editointikontrollit.registerCallback({
                    async edit() {
                        $scope.isSorting = true;
                    },
                    async save() {
                        $scope.isSorting = false;
                        await $scope.osaAlueet.customPUT();
                        Notifikaatiot.onnistui("tallennus-onnistui");
                    },
                    cancel() {
                        $scope.isSorting = false;
                        $scope.osaAlueet = backup.clone();
                    }
                });
                Editointikontrollit.startEditing();
            };

            $scope.sortableOptions = {
                cursor: "move",
                cursorAt: { top: 2, left: 2 },
                handle: ".handle",
                delay: 100,
                tolerance: "pointer"
            };

            $scope.add = async () => {
                $state.go("root.perusteprojekti.suoritustapa.aipeosaalue", {
                    suoritustapa: $stateParams.suoritustapa,
                    osanTyyppi: $stateParams.osanTyyppi,
                    osanId: "uusi"
                });
            };
        },
        onEnter: PerusteProjektiSivunavi => {
            PerusteProjektiSivunavi.setVisible();
        }
    });
});

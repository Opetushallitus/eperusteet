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

angular.module("eperusteApp").config($stateProvider => {
    $stateProvider.state("root.perusteprojekti.suoritustapa.aipeosaalue", {
        url: "/aipeosat/:osanTyyppi/:osanId?{versio?:int}",
        template: require("scripts/states/perusteprojekti/suoritustapa/aipeosaalue/view.html"),
        resolve: {
            perusteprojektit: Api => Api.all("perusteprojektit"),
            perusteprojekti: (perusteprojektit, $stateParams) =>
                perusteprojektit.one($stateParams.perusteProjektiId).get(),
            perusteet: Api => Api.all("perusteet"),
            peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
            aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
            vaiheet: aipeopetus => aipeopetus.all("vaiheet").getList(),
            isOsaaminen: ($stateParams, AIPEService) => $stateParams.osanTyyppi === AIPEService.OSAAMINEN,
            isVaihe: ($stateParams, AIPEService) => $stateParams.osanTyyppi === AIPEService.VAIHEET,
            isNew: $stateParams => $stateParams.osanId === "uusi",
            versiot: ($stateParams, $q, VersionHelper, peruste, isVaihe) => {
                const deferred = $q.defer();
                if (isVaihe) {
                    const versiot = {};
                    VersionHelper.getAIPEVaiheVersions(versiot,
                        {
                            id: peruste.id,
                            vaiheId: $stateParams.osanId
                        }, true, res => {
                            deferred.resolve(versiot);
                        });
                } else {
                    deferred.resolve();
                }
                return deferred.promise;
            },
            vaihe: ($stateParams, Api, VersionHelper, vaiheet, isVaihe, isNew, versiot) => {
                if (isVaihe && !isNew) {
                    const versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
                    const rev = VersionHelper.select(versiot, versio);
                    const params: any = {};
                    if (rev) {
                        params.rev = rev;
                    }
                    return vaiheet.customGET($stateParams.osanId, params);
                } else {
                    return Api.restangularizeElement(vaiheet, { opetuksenKohdealueet: [] }, "");
                }
            },
            laajaalaiset: aipeopetus => aipeopetus.all("laajaalaiset").getList(),
            laajaalainen: (laajaalaiset, $stateParams, Api, isOsaaminen, isNew) =>
                isOsaaminen && !isNew
                    ? laajaalaiset.get($stateParams.osanId)
                    : Api.restangularizeElement(laajaalaiset, {}, "")
        },
        controller: (
            $scope,
            $q,
            $state,
            $stateParams,
            laajaalaiset,
            laajaalainen,
            Editointikontrollit,
            Notifikaatiot,
            YleinenData,
            ProjektinMurupolkuService,
            vaiheet,
            vaihe,
            isOsaaminen,
            isVaihe,
            isNew,
            AIPEService,
            versiot
        ) => {
            $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
            $scope.isNew = isNew;
            $scope.osanTyyppi = $stateParams.osanTyyppi;
            $scope.versiot = {};
            $scope.$state = $state;
            $scope.aipeService = AIPEService;

            if (isOsaaminen) {
                $scope.isOsaaminen = () => $state.is("root.perusteprojekti.suoritustapa.aipeosaalue");
                $scope.dataObject = laajaalainen;
            } else if (isVaihe) {
                $scope.isVaihe = () => $state.is("root.perusteprojekti.suoritustapa.aipeosaalue");
                $scope.dataObject = vaihe;
                $scope.versiot = versiot;
            }

            $scope.edit = () => {
                Editointikontrollit.startEditing();
            };

            const labels = _.invert(AIPEService.LABELS);
            ProjektinMurupolkuService.set("osanTyyppi", $stateParams.osanTyyppi, labels[$stateParams.osanTyyppi]);
            ProjektinMurupolkuService.set("osanId", $stateParams.osanId, $scope.dataObject.nimi);
        },
        onEnter: PerusteProjektiSivunavi => {
            PerusteProjektiSivunavi.setVisible();
        }
    });
});

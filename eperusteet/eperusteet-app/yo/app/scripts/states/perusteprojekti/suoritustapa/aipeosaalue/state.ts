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

angular.module("eperusteApp")
.config($stateProvider => {
$stateProvider.state("root.perusteprojekti.suoritustapa.aipeosaalue", {
    url: "/aipeosat/:osanTyyppi/:osanId",
    templateUrl: "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/view.html",
    resolve: {
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (perusteprojektit, $stateParams) => perusteprojektit.one($stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
        vaiheet: (aipeopetus) => aipeopetus.all("vaiheet").getList(),
        isOsaaminen: ($stateParams, AIPEService) => $stateParams.osanTyyppi === AIPEService.OSAAMINEN,
        isVaihe: ($stateParams, AIPEService) => $stateParams.osanTyyppi === AIPEService.VAIHEET,
        isNew: ($stateParams) => $stateParams.osanId === "uusi",
        vaihe: (vaiheet, $stateParams, Api, isVaihe, isNew) => isVaihe && !isNew
            ? vaiheet.get($stateParams.osanId)
            : Api.restangularizeElement(vaiheet, { opetuksenKohdealueet: [] }, ""),
        laajaalaiset: (aipeopetus) => aipeopetus.all("laajaalaiset").getList(),
        laajaalainen: (laajaalaiset, $stateParams, Api, isOsaaminen, isNew) => isOsaaminen && !isNew
            ? laajaalaiset.get($stateParams.osanId)
            : Api.restangularizeElement(laajaalaiset, {}, ""),
        oppiaineet: (vaihe, isVaihe, isNew) => isVaihe && !isNew ? vaihe.all("oppiaineet").getList() : null
    },
    controller: ($scope, $q, $state, $stateParams, laajaalaiset, laajaalainen, Editointikontrollit, Notifikaatiot,
                 YleinenData, ProjektinMurupolkuService, vaiheet, vaihe, isOsaaminen, isVaihe, isNew, oppiaineet,
                 AIPEService) => {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.isNew = isNew;
        $scope.osanTyyppi = $stateParams.osanTyyppi;
        $scope.versiot = {
            latest: true
        };
        $scope.$state = $state;
        $scope.aipeService = AIPEService;

        if (isOsaaminen) {
            $scope.isOsaaminen = () => $state.is('root.perusteprojekti.suoritustapa.aipeosaalue');
            $scope.dataObject = laajaalainen;
        } else if (isVaihe) {
            $scope.isVaihe = () => $state.is('root.perusteprojekti.suoritustapa.aipeosaalue');
            $scope.dataObject = vaihe;
            $scope.dataObject.oppiaineet = oppiaineet;
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
})});

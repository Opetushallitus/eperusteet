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

'use strict';

angular.module('eperusteApp')
.config($stateProvider => {
$stateProvider
.state('root.perusteprojekti.suoritustapa.aipeosalistaus', {
    url: '/aipeosat/:osanTyyppi',
    templateUrl: 'views/partials/perusteprojekti/osalistaus.html',
    resolve: {
        perusteprojektiTiedot: PerusteprojektiTiedotService => PerusteprojektiTiedotService,
        projektinTiedotAlustettu: perusteprojektiTiedot => perusteprojektiTiedot.projektinTiedotAlustettu(),
        perusteenSisaltoAlustus: (perusteprojektiTiedot, projektinTiedotAlustettu, $stateParams) => perusteprojektiTiedot.alustaPerusteenSisalto($stateParams),
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (perusteprojektit, $stateParams) => perusteprojektit.one($stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
        vaiheet: (aipeopetus) => aipeopetus.all("vaiheet").getList(),
        laajaalaiset: (aipeopetus) => aipeopetus.all("laajaalaiset").getList()
    },
    controller: ($scope, $state, $stateParams, PerusopetusService, virheService, laajaalaiset, vaiheet) => {
        $scope.sisaltoState = _.find(PerusopetusService.sisallot, {
            tyyppi: $stateParams.osanTyyppi
        });
        if (!$scope.sisaltoState) {
            virheService.virhe('virhe-sivua-ei-löytynyt');
        }

        $scope.osaAlueet = laajaalaiset;

        $scope.options = {
            extrafilter: null
        };

        $scope.createUrl = value => $state.href('root.perusteprojekti.suoritustapa.aipeosaalue', {
            suoritustapa: $stateParams.suoritustapa,
            osanTyyppi: $stateParams.osanTyyppi,
            osanId: value.id,
            tabId: 0
        });

        $scope.add = async () => {
            $state.go('root.perusteprojekti.suoritustapa.aipeosaalue', {
                suoritustapa: $stateParams.suoritustapa,
                osanTyyppi: $stateParams.osanTyyppi,
                osanId: 'uusi'
            });
        };
    },
    onEnter: PerusteProjektiSivunavi => {
        PerusteProjektiSivunavi.setVisible();
    }
})
});

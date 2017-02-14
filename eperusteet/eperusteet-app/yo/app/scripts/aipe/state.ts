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
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.suoritustapa.aipesisalto", {
    url: "/aipesisalto",
    resolve: {
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (perusteprojektit, $stateParams) => perusteprojektit.one($stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste) => peruste.one("aipeopetus"),
        vaiheet: (aipeopetus) => aipeopetus.one("vaiheet").getList(),
        laajaalaiset: (aipeopetus) => aipeopetus.one("laajaalaiset").getList(),
        sisallot: (peruste) => peruste.all("suoritustavat/aipe/sisalto"),
        sisalto: (peruste) => peruste.one("suoritustavat/aipe/sisalto").get()
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/view.html",
            controller: ($scope, $state, $stateParams, peruste, vaiheet, laajaalaiset, sisalto, sisallot,
                         Editointikontrollit, TekstikappaleOperations, Notifikaatiot, SuoritustavanSisalto,
                         Algoritmit) => {
                $scope.peruste = peruste;
                $scope.peruste.sisalto = sisalto;
                $scope.vaiheet = vaiheet;
                $scope.laajaalaiset = laajaalaiset;
                $scope.esitysUrl = $state.href("root.selaus.aikuisperusopetuslista", {
                    perusteId: $scope.peruste.id
                });
                $scope.rajaus = "";

                Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", lapsi => {
                    lapsi.$url = lapsi.perusteenOsa.tunniste === "laajaalainenosaaminen" ?
                        $state.href("root.perusteprojekti.suoritustapa.osalistaus", {
                            suoritustapa: "aipe",
                            osanTyyppi: "osaaminen"
                        }) :
                        $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                            suoritustapa: "aipe",
                            perusteenOsaViiteId: lapsi.id,
                            versio: ""
                        });
                });

                $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();

                $scope.addTekstikappale = () => {
                    sisallot.post({}).then(res => {
                        const params = {
                            perusteenOsaViiteId: res.id,
                            versio: ""
                        };
                        $state.go("root.perusteprojekti.suoritustapa.tekstikappale", params, { reload: true });
                    });
                };

                $scope.edit = () => {
                    Editointikontrollit.startEditing();
                };

                Editointikontrollit.registerCallback({
                    edit: () => {
                        console.log("edit");
                        $scope.rajaus = "";
                        $scope.editing = true;
                    },
                    save: () => {
                        $scope.peruste.sisalto.save().then(() => {
                            Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
                            $scope.editing = false;
                        });
                    },
                    cancel: () => {
                        $scope.editing = false;
                    },
                    validate: () => {
                        return true;
                    },
                    notify: value => {
                    }
                });
            }
        }
    },
    onEnter: (PerusteProjektiSivunavi) => {
        PerusteProjektiSivunavi.setVisible(false);
    }
}));

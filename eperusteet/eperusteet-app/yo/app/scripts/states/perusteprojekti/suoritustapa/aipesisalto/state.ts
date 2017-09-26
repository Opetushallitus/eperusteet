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

angular.module("eperusteApp").config($stateProvider =>
    $stateProvider.state("root.perusteprojekti.suoritustapa.aipesisalto", {
        url: "/aipesisalto",
        resolve: {
            perusteprojektit: Api => Api.all("perusteprojektit"),
            perusteprojekti: (perusteprojektit, $stateParams) =>
                perusteprojektit.one($stateParams.perusteProjektiId).get(),
            perusteet: Api => Api.all("perusteet"),
            peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
            aipeopetus: (peruste, $stateParams) => peruste.one("aipeopetus"),
            vaiheet: aipeopetus => aipeopetus.all("vaiheet").getList(),
            laajaalaiset: aipeopetus => aipeopetus.all("laajaalaiset").getList(),
            sisallot: (peruste, $stateParams) => peruste.all("suoritustavat/" + $stateParams.suoritustapa + "/sisalto"),
            sisalto: (peruste, $stateParams) =>
                peruste.one("suoritustavat/" + $stateParams.suoritustapa + "/sisalto").get(),
            jotain: () => {}
        },
        templateUrl: "scripts/states/perusteprojekti/suoritustapa/aipesisalto/view.html",
        controller: (
            $scope,
            $state,
            $stateParams,
            peruste,
            vaiheet,
            laajaalaiset,
            sisalto,
            sisallot,
            Editointikontrollit,
            TekstikappaleOperations,
            Notifikaatiot,
            SuoritustavanSisalto,
            Algoritmit,
            Utils,
            Api
        ) => {
            $scope.peruste = peruste;
            $scope.sisalto = sisalto;
            $scope.peruste.sisalto = Api.copy(sisalto);
            $scope.vaiheet = vaiheet;
            $scope.opetus = {
                lapset: [
                    {
                        nimi: "laaja-alainen-osaaminen",
                        tyyppi: "osaaminen",
                        lapset: laajaalaiset
                    },
                    {
                        nimi: "vaiheet",
                        tyyppi: "vaiheet",
                        lapset: vaiheet
                    }
                ]
            };
            $scope.esitysUrl = $state.href("root.selaus.aikuisperusopetuslista", {
                perusteId: $scope.peruste.id
            });
            $scope.rajaus = "";
            $scope.editing = false;

            $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();

            $scope.addTekstikappale = async () => {
                const res = await sisallot.post({});
                $state.go(
                    "root.perusteprojekti.suoritustapa.tekstikappale",
                    {
                        perusteenOsaViiteId: res.id,
                        versio: ""
                    },
                    {
                        reload: true
                    }
                );
            };

            $scope.edit = () => {
                Editointikontrollit.startEditing();
            };

            _.each($scope.opetus.lapset, area => {
                area.$type = "ep-parts";
                area.$url = $state.href("root.perusteprojekti.suoritustapa.aipeosalistaus", {
                    suoritustapa: $stateParams.suoritustapa,
                    osanTyyppi: area.tyyppi
                });
                area.$orderFn = area.tyyppi == Utils.nameSort;
                Algoritmit.kaikilleLapsisolmuille(area, "lapset", lapsi => {
                    lapsi.$url = $state.href("root.perusteprojekti.suoritustapa.aipeosaalue", {
                        suoritustapa: $stateParams.suoritustapa,
                        osanTyyppi: area.tyyppi,
                        osanId: lapsi.id,
                        tabId: 0
                    });
                    if (lapsi.koosteinen) {
                        lapsi.lapset = _.sortBy(lapsi.oppimaarat, Utils.nameSort);
                    }
                });
            });

            Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", lapsi => {
                lapsi.$url =
                    lapsi.perusteenOsa.tunniste === "laajaalainenosaaminen"
                        ? $state.href("root.perusteprojekti.suoritustapa.aipeosalistaus", {
                              suoritustapa: "aipe",
                              osanTyyppi: "osaaminen"
                          })
                        : $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                              suoritustapa: "aipe",
                              perusteenOsaViiteId: lapsi.id,
                              versio: ""
                          });
            });

            Editointikontrollit.registerCallback({
                edit: async () => {
                    $scope.sisalto.id = undefined;
                    $scope.sisalto = await $scope.sisalto.get();
                    $scope.peruste.sisalto = Api.copy($scope.sisalto);
                    $scope.rajaus = "";
                    $scope.editing = true;
                },
                save: async () => {
                    await $scope.peruste.sisalto.save();
                    Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
                    $scope.editing = false;
                },
                cancel: () => {
                    $scope.peruste.sisalto = Api.copy($scope.sisalto);
                    $scope.editing = false;
                },
                validate: () => {
                    return true;
                },
                notify: () => {}
            });
        },
        onEnter: PerusteProjektiSivunavi => {
            PerusteProjektiSivunavi.setVisible(false);
        }
    })
);

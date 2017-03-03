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
.state("root.aipeperusteprojekti.suoritustapa.sisalto", {
    url: "/sisalto",
    views: {
        "": {
            templateUrl: "states/aipeperusteprojekti/suoritustapa/sisalto/view.html",
            controller: ($scope, $state, $stateParams, peruste, vaiheet, laajaalaiset, sisalto, sisallot,
                         Editointikontrollit, TekstikappaleOperations, Notifikaatiot, SuoritustavanSisalto,
                         Algoritmit, Utils, Api) => {
                $scope.peruste = peruste;
                $scope.peruste.sisalto = Api.copy(sisalto);
                $scope.opetus = {
                    lapset: [{
                        nimi: "laaja-alainen-osaaminen",
                        tyyppi: "osaaminen",
                        lapset: laajaalaiset,
                        $url: $state.href("root.aipeperusteprojekti.suoritustapa.osalistaus", {
                            osanTyyppi: "osaaminen"
                        }),
                        $type: "ep-parts",
                        $orderFn: "osaaminen" == Utils.nameSort
                    }, {
                        nimi: "vaiheet",
                        tyyppi: "vaiheet",
                        lapset: vaiheet,
                        $url: $state.href("root.aipeperusteprojekti.suoritustapa.osalistaus", {
                            osanTyyppi: "vaiheet"
                        }),
                        $type: "ep-parts",
                        $orderFn: "vaiheet" == Utils.nameSort
                    }, {
                        nimi: "oppiaineet",
                        tyyppi: "oppiaineet",
                        lapset: [],
                        $url: $state.href("root.aipeperusteprojekti.suoritustapa.osalistaus", {
                            osanTyyppi: "oppiaineet"
                        }),
                        $type: "ep-parts",
                        $orderFn: "oppiaineet" == Utils.nameSort
                    }]
                };
                $scope.esitysUrl = $state.href("root", {
                    perusteId: $scope.peruste.id
                });
                $scope.rajaus = "";

                _.each($scope.opetus.lapset, area => {
                    Algoritmit.kaikilleLapsisolmuille(area, "lapset", lapsi => {
                        lapsi.$url = $state.href("root.aipeperusteprojekti.suoritustapa.osaalue", {
                            suoritustapa: $stateParams.suoritustapa,
                            osanTyyppi: area.tyyppi,
                            osanId: lapsi.id,
                            //tabId: 0
                        });
                        if (lapsi.koosteinen) {
                            lapsi.lapset = _.sortBy(lapsi.oppimaarat, Utils.nameSort);
                        }
                    });
                });

                Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, "lapset", lapsi => {
                    lapsi.$url = lapsi.perusteenOsa.tunniste === "laajaalainenosaaminen" ?
                        $state.href("root.aipeperusteprojekti.suoritustapa.osalistaus", {})
                        : $state.href("root.aipeperusteprojekti.suoritustapa.tekstikappale", {
                            suoritustapa: "aipe",
                            perusteenOsaViiteId: lapsi.id,
                            versio: ""
                        });
                });

                $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();

                $scope.addTekstikappale = async () => {
                    const res = await sisallot.post({});
                    $state.go("root.aipeperusteprojekti.suoritustapa.tekstikappale", {
                        perusteenOsaViiteId: res.id,
                        versio: ""
                    }, {
                        reload: true
                    });
                };

                $scope.edit = () => {
                    Editointikontrollit.startEditing();
                };

                Editointikontrollit.registerCallback({
                    edit: async () => {
                        sisalto.id = undefined;
                        sisalto = await sisalto.get();
                        $scope.peruste.sisalto = Api.copy(sisalto);
                        $scope.rajaus = "";
                        $scope.editing = true;
                    },
                    save: async () => {
                        await $scope.peruste.sisalto.save();
                        Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
                        $scope.editing = false;
                    },
                    cancel: () => {
                        $scope.peruste.sisalto = Api.copy(sisalto);
                        $scope.editing = false;
                    },
                    validate: () => {
                        return true;
                    },
                    notify: () => {
                    }
                });
            }
        }
    }
}));

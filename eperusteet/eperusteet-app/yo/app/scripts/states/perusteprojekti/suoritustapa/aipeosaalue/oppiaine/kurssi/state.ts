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

angular.module("eperusteApp").config($stateProvider => {
    $stateProvider.state("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine.kurssi", {
        url: "/kurssit/:kurssiId",
        resolve: {
            kurssi: (oppiaine, $stateParams) => oppiaine.one("kurssit", $stateParams.kurssiId).get()
        },
        views: {
            "aipeosaalue@root.perusteprojekti.suoritustapa.aipeosaalue": {
                templateUrl: "scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/kurssi/view.html",
                controller: (
                    $scope,
                    $state,
                    $stateParams,
                    AIPEService,
                    Editointikontrollit,
                    PerusteProjektiSivunavi,
                    Notifikaatiot,
                    kurssi,
                    Api,
                    Koodisto,
                    MuokkausUtils,
                    Varmistusdialogi,
                    oppiaine
                ) => {
                    $scope.editEnabled = false;
                    $scope.editableModel = Api.copy(kurssi);
                    $scope.muokkaa = () => Editointikontrollit.startEditing();
                    $scope.poista = () => {
                        Varmistusdialogi.dialogi({
                            otsikko: "varmista-poisto",
                            teksti: "poistetaanko-kurssi",
                            primaryBtn: "poista",
                            successCb: async () => {
                                Editointikontrollit.cancelEditing();
                                await $scope.editableModel.remove();
                                await $state.go(
                                    "root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine",
                                    {
                                        oppiaineId: kurssi._oppiaine
                                    },
                                    {
                                        reload: true
                                    }
                                );
                            }
                        })();
                    };
                    $scope.openKoodisto = Koodisto.modaali(
                        koodisto => {
                            if (!$scope.editableModel.koodi) {
                                $scope.editableModel["koodi"] = {};
                            }
                            MuokkausUtils.nestedSet(
                                $scope.editableModel.koodi,
                                "koodisto",
                                ",",
                                koodisto.koodisto.koodistoUri
                            );
                            MuokkausUtils.nestedSet($scope.editableModel.koodi, "uri", ",", koodisto.koodiUri);
                            MuokkausUtils.nestedSet($scope.editableModel.koodi, "arvo", ",", koodisto.koodiArvo);
                        },
                        {
                            tyyppi: () => {
                                return "oppiaineetyleissivistava2";
                            },
                            ylarelaatioTyyppi: () => {
                                return "";
                            },
                            tarkista: _.constant(true)
                        }
                    );
                    $scope.oppiaine = oppiaine;
                    $scope.tavoitteetFilter = item => {
                        return _.includes($scope.editableModel.tavoitteet, item.id + "");
                    };

                    Editointikontrollit.registerCallback({
                        edit: async () => {
                            // Onko tarpeellinen?
                            kurssi = await kurssi.get();
                            $scope.editableModel = Api.copy(kurssi);
                            _.each($scope.editableModel.tavoitteet, tavoiteId => {
                                const tavoite = _.find($scope.oppiaine.tavoitteet, { id: parseInt(tavoiteId) });
                                if (tavoite) {
                                    tavoite.$valittu = true;
                                }
                            });
                        },
                        save: async () => {
                            $scope.editableModel.tavoitteet = [];
                            _.each($scope.oppiaine.tavoitteet, tavoite => {
                                if (tavoite.$valittu) {
                                    $scope.editableModel.tavoitteet.push(tavoite.id);
                                }
                            });
                            $scope.editableModel = await $scope.editableModel.save();
                            Notifikaatiot.onnistui("tallennus-onnistui");
                            kurssi = Api.copy($scope.editableModel);
                        },
                        cancel: () => {
                            $scope.editableModel = Api.copy(kurssi);
                        },
                        notify: value => {
                            $scope.editEnabled = value;
                            PerusteProjektiSivunavi.setVisible(!value);
                        }
                    });
                }
            }
        },
        onEnter: PerusteProjektiSivunavi => {
            PerusteProjektiSivunavi.setVisible();
        }
    });
});

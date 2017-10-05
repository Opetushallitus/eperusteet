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

angular.module("eperusteApp").directive("muokkausVaihe", () => {
    return {
        template: require("views/directives/vaihe.html"),
        restrict: "E",
        scope: {
            model: "=",
            versiot: "="
        },
        controller: (
            $scope,
            YleinenData,
            $stateParams,
            Api,
            Editointikontrollit,
            Notifikaatiot,
            $state,
            PerusteProjektiSivunavi,
            Varmistusdialogi,
            AIPEService,
            Utils,
            $rootScope,
            Kieli,
            Kaanna
        ) => {
            $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
            $scope.isNew = $stateParams.osanId === "uusi";
            $scope.editableModel = Api.copy($scope.model);
            $scope.editEnabled = false;
            $scope.muokkaa = () => Editointikontrollit.startEditing();
            $scope.poista = () => {
                Varmistusdialogi.dialogi({
                    otsikko: "varmista-poisto",
                    teksti: "poistetaanko-vaihe",
                    primaryBtn: "poista",
                    successCb: async () => {
                        Editointikontrollit.cancelEditing();
                        await $scope.editableModel.remove();
                        AIPEService.clearCache();
                        $state.go(
                            "root.perusteprojekti.suoritustapa.aipeosalistaus",
                            {
                                suoritustapa: $stateParams.suoritustapa,
                                osanTyyppi: AIPEService.VAIHEET
                            },
                            {
                                reload: true
                            }
                        );
                    }
                })();
            };

            const createOppiaineUrl = oppiaine => $state.href(".oppiaine", { oppiaineId: oppiaine.id });

            if ($scope.editableModel && _.isArray($scope.editableModel.oppiaineet)) {
                for (const oa of $scope.editableModel.oppiaineet) {
                    oa.$$url = createOppiaineUrl(oa);
                }
            }

            $scope.lisaaOppiaine = async () => {
                const oppiaine = await $scope.model.oppiaineet.post({});
                oppiaine.$$url = createOppiaineUrl(oppiaine);
                await $state.go("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine", {
                    oppiaineId: oppiaine.id
                });
                $scope.editableModel.oppiaineet.push(oppiaine);
            };
            $scope.poistaKohdealue = ka => {
                _.remove($scope.editableModel.opetuksenKohdealueet, ka);
            };
            $scope.lisaaKohdealue = () => {
                $scope.editableModel.opetuksenKohdealueet.push({
                    nimi: {
                        fi: "Uusi tavoitealue"
                    }
                });
            };

            $scope.fields = [
                {
                    path: "siirtymaEdellisesta",
                    localeKey: "siirtyma-edellisesta",
                    order: 1
                },
                {
                    path: "tehtava",
                    localeKey: "vaihe-tehtava",
                    order: 2
                },
                {
                    path: "siirtymaSeuraavaan",
                    localeKey: "siirtyma-seuraavaan",
                    order: 3
                },
                {
                    path: "paikallisestiPaatettavatAsiat",
                    localeKey: "paikallisesti-paatettavat-asiat",
                    order: 4
                }
            ];

            $scope.fieldOps = {
                hasContent: field => {
                    const model = $scope.editableModel[field.path];
                    if (_.isEmpty(model)) {
                        return false;
                    }
                    if (field.type) {
                        return !_.isEmpty(model);
                    } else {
                        const otsikko = model.otsikko;
                        const teksti = model.teksti;
                        return Utils.hasLocalizedText(otsikko) || Utils.hasLocalizedText(teksti);
                    }
                },
                remove: field => {
                    const doRemove = () => {
                        field.visible = false;
                        $scope.editableModel[field.path] = field.type ? [] : null;
                    };

                    if ($scope.fieldOps.hasContent(field)) {
                        Varmistusdialogi.dialogi({
                            otsikko: "varmista-poisto",
                            teksti: "poistetaanko-osio",
                            primaryBtn: "poista",
                            successCb: () => {
                                doRemove();
                            }
                        })();
                    } else {
                        doRemove();
                    }
                },
                edit: field => {
                    field.$editing = true;
                    field.$isCollapsed = false;
                    fieldBackups[field.path] = _.cloneDeep($scope.editableModel[field.path]);
                },
                cancel: field => {
                    field.$editing = false;
                    $scope.editableModel[field.path] = _.cloneDeep(fieldBackups[field.path]);
                    fieldBackups[field.path] = null;
                },
                ok: field => {
                    field.$editing = false;
                    fieldBackups[field.path] = null;
                    $rootScope.$broadcast("notifyCKEditor");
                },
                add: field => {
                    field.visible = true;
                    if (!$scope.editableModel[field.path]) {
                        $scope.editableModel[field.path] = {
                            otsikko: {},
                            teksti: {}
                        };
                        const modelField = $scope.editableModel[field.path];
                        modelField.otsikko[Kieli.getSisaltokieli()] = Kaanna.kaanna(field.localeKey);
                    }
                    field.$editing = true;
                }
            };

            const fieldBackups = {};

            $scope.filterFn = item => {
                return item.visible || _.isUndefined(item.visible);
            };

            function mapModel() {
                _.each($scope.fields, field => {
                    field.visible = $scope.fieldOps.hasContent(field);
                });
            }
            mapModel();

            $scope.updateOppiaineet = async () =>
                $scope.model.all("oppiaineet").customPUT($scope.editableModel.oppiaineet);

            $scope.isSorting = false;

            Editointikontrollit.registerCallback({
                async edit() {},
                async save() {
                    if ($scope.isNew) {
                        $scope.editableModel = await $scope.editableModel.post();
                    } else {
                        $scope.editableModel = await $scope.editableModel.save();
                    }

                    Notifikaatiot.onnistui("tallennus-onnistui");
                    AIPEService.clearCache();
                    $state.go(
                        $state.current,
                        {
                            suoritustapa: $stateParams.suoritustapa,
                            osanTyyppi: $stateParams.osanTyyppi,
                            osanId: $scope.editableModel.id
                        },
                        {
                            reload: true
                        }
                    );
                },
                cancel() {
                    if ($scope.isNew && $scope.data) {
                        $state.go($scope.data.options.backState[0], $scope.data.options.backState[1], {
                            reload: true
                        });
                    } else {
                        $scope.editableModel = Api.copy($scope.model);
                    }
                },
                notify(value) {
                    $scope.editEnabled = value;
                    PerusteProjektiSivunavi.setVisible(!value);
                },
                validate() {
                    return true;
                }
            });
            Editointikontrollit.registerEditModeListener(mode => {
                $scope.editEnabled = mode;
            });

            if ($scope.isNew) {
                $scope.muokkaa();
            }
        }
    };
});

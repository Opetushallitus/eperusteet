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
    $stateProvider.state("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine", {
        url: "/oppiaineet/:oppiaineId",
        resolve: {
            oppiaineet: (vaihe, isVaihe, isNew) => (isVaihe && !isNew ? vaihe.all("oppiaineet").getList() : null),
            versiot: ($stateParams, $q, VersionHelper, peruste, isVaihe) => {
                const deferred = $q.defer();
                if (isVaihe) {
                    const versiot = {};
                    VersionHelper.getAIPEOppiaineVersions(versiot,
                        {
                            perusteId: peruste.id,
                            vaiheId: $stateParams.osanId,
                            oppiaineId: $stateParams.oppiaineId
                        }, true, res => {
                            deferred.resolve(versiot);
                        });
                } else {
                    deferred.resolve();
                }
                return deferred.promise;
            },
            oppiaine: ($stateParams, VersionHelper, vaihe, versiot) => {
                const versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
                const rev = VersionHelper.select(versiot, versio);
                const params: any = {};
                if (rev) {
                    params.rev = rev;
                }
                return vaihe.one("oppiaineet").customGET($stateParams.oppiaineId, params)
            },
            kurssit: (oppiaine, $stateParams) => oppiaine.all("kurssit").getList(),
            oppimaarat: oppiaine => oppiaine.all("oppimaarat").getList()
        },
        views: {
            "aipeosaalue@root.perusteprojekti.suoritustapa.aipeosaalue": {
                template: require("scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/view.pug"),
                controller: (
                    $rootScope,
                    $scope,
                    $state,
                    $stateParams,
                    Editointikontrollit,
                    PerusteProjektiSivunavi,
                    Notifikaatiot,
                    Varmistusdialogi,
                    AIPEService,
                    Api,
                    Utils,
                    Kieli,
                    Kaanna,
                    Koodisto,
                    MuokkausUtils,
                    VersionHelper,
                    oppiaine,
                    kurssit,
                    oppiaineet,
                    oppimaarat,
                    laajaalaiset,
                    vaihe,
                    versiot
                ) => {
                    for (const oa of oppimaarat) {
                        oa.$$url = $state.href("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine", {
                            oppiaineId: oa.id
                        });
                    }

                    for (const kurssi of kurssit) {
                        kurssi.$$url = $state.href("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine.kurssi", {
                            kurssiId: kurssi.id
                        });
                    }

                    $scope.vaihdaVersio = () => {
                        $scope.versiot.hasChanged = true;
                        VersionHelper.setUrl($scope.versiot);
                    };

                    $scope.isSorting = false;
                    $scope.editEnabled = false;
                    $scope.editableModel = Api.copy(oppiaine);

                    $scope.oppimaarat = oppimaarat;
                    $scope.kurssit = kurssit;
                    $scope.isOppimaara = !!oppiaine._oppiaine;
                    $scope.canAddKurssit = _.isEmpty(oppimaarat);
                    $scope.canAddOppimaara = !$scope.isOppimaara && _.isEmpty(kurssit);
                    $scope.versiot = versiot;

                    $scope.revertCb = () => {
                        VersionHelper.getAIPEOppiaineVersions(versiot,
                            {
                                perusteId: $scope.peruste.id,
                                vaiheId: $stateParams.osanId,
                                oppiaineId: $stateParams.oppiaineId
                            }, true, res => {
                                Notifikaatiot.onnistui("aipe-oppiaine-palautettu");
                                VersionHelper.setUrl(res);
                            });
                    };

                    $scope.lisaaOppimaara = async () => {
                        const oppimaara = await oppimaarat.post({});
                        await $state.go("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine", {
                            oppiaineId: oppimaara.id
                        });
                        oppimaarat.push(oppimaara);
                    };
                    $scope.laajaalaiset = laajaalaiset;
                    $scope.vaihe = vaihe;

                    function onKieliKoodi(koodiarvo: string): boolean {
                        return (
                            _.startsWith(koodiarvo, "A") ||
                            _.startsWith(koodiarvo, "B") ||
                            _.startsWith(koodiarvo, "C") ||
                            _.startsWith(koodiarvo, "ENA") ||
                            _.startsWith(koodiarvo, "ENA") ||
                            _.startsWith(koodiarvo, "LA") ||
                            _.startsWith(koodiarvo, "LK") ||
                            _.startsWith(koodiarvo, "MK") ||
                            _.startsWith(koodiarvo, "RU") ||
                            _.startsWith(koodiarvo, "SK") ||
                            _.startsWith(koodiarvo, "TK") ||
                            _.startsWith(koodiarvo, "TK") ||
                            _.startsWith(koodiarvo, "VK")
                        );
                    }

                    $scope.isKieli = () => oppiaine.koodi && onKieliKoodi(oppiaine.koodi.arvo);

                    $scope.lisaaKurssi = async () => {
                        const kurssi = await kurssit.post({});
                        await $state.go("root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine.kurssi", {
                            kurssiId: kurssi.id
                        });
                        kurssit.push(kurssi);
                    };

                    $scope.updateOppimaarat = async oppimaarat => {
                        return oppimaarat.customPUT();
                    };

                    $scope.updateKurssit = async kurssit => {
                        return kurssit.customPUT();
                    };

                    $scope.poista = async () => {
                        Varmistusdialogi.dialogi({
                            otsikko: "varmista-poisto",
                            teksti: "poistetaanko-oppiaine",
                            primaryBtn: "poista",
                            successCb: async () => {
                                await Editointikontrollit.cancelEditing();
                                await $scope.editableModel.remove();

                                if (oppiaine._oppiaine) {
                                    await $state.go(
                                        "root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine",
                                        {
                                            oppiaineId: oppiaine._oppiaine
                                        },
                                        {
                                            reload: true
                                        }
                                    );
                                } else {
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
                            }
                        })();
                    };

                    $scope.fields = [
                        {
                            path: "tehtava",
                            localeKey: "oppiaine-osio-tehtava",
                            order: 1
                        },
                        {
                            path: "tyotavat",
                            localeKey: "oppiaine-osio-tyotavat",
                            order: 2
                        },
                        {
                            path: "ohjaus",
                            localeKey: "oppiaine-osio-ohjaus",
                            order: 3
                        },
                        {
                            path: "arviointi",
                            localeKey: "oppiaine-osio-arviointi",
                            order: 4
                        },
                        {
                            path: "sisaltoalueinfo",
                            localeKey: "oppiaine-osio-sisaltoalueinfo",
                            order: 5
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

                    $scope.muokkaa = () => {
                        Editointikontrollit.registerCallback({
                            edit: async () => {
                                oppiaine = await oppiaine.get();
                                $scope.editableModel = Api.copy(oppiaine);
                            },
                            save: async () => {
                                _.each($scope.editableModel.tavoitteet, tavoite => {
                                    tavoite.laajattavoitteet = _(tavoite.$osaaminen)
                                        .filter(item => !item.$hidden)
                                        .map(t => t.id)
                                        .value();
                                });
                                $scope.editableModel = await $scope.editableModel.save();
                                Notifikaatiot.onnistui("tallennus-onnistui");
                                oppiaine = Api.copy($scope.editableModel);
                            },
                            cancel: () => {
                                $scope.editableModel = Api.copy(oppiaine);
                            },
                            notify: value => {
                                $scope.editEnabled = value;
                                PerusteProjektiSivunavi.setVisible(!value);
                            }
                        });
                        Editointikontrollit.startEditing();
                    };
                }
            }
        },
        onEnter: PerusteProjektiSivunavi => {
            PerusteProjektiSivunavi.setVisible();
        }
    });
});

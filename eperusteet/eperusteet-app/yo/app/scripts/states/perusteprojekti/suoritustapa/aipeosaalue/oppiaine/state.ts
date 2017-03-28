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

angular.module('eperusteApp')
.config($stateProvider => {
$stateProvider.state('root.perusteprojekti.suoritustapa.aipeosaalue.oppiaine', {
    url: '/oppiaineet/:oppiaineId',
    templateUrl: 'scripts/states/perusteprojekti/suoritustapa/aipeosaalue/oppiaine/view.html',
    resolve: {
        oppiaine: (vaihe, $stateParams, Api) => vaihe.one("oppiaineet", $stateParams.oppiaineId).get(),
        kurssit: (oppiaine, $stateParams) => oppiaine.all("kurssit").getList(),
        oppimaarat: (oppiaine) => oppiaine.all("oppimaarat").getList()
    },
    controller: ($scope, oppiaine, Api, kurssit, Editointikontrollit, PerusteProjektiSivunavi, Notifikaatiot,
                 oppiaineet, oppimaarat, Varmistusdialogi, $state, $stateParams, AIPEService, $rootScope,
                 Utils, Kieli, Kaanna, Koodisto, MuokkausUtils) => {
        $scope.editEnabled = false;
        $scope.editableModel = Api.copy(oppiaine);
        $scope.oppimaarat = oppimaarat;
        $scope.kurssit = kurssit;
        $scope.muokkaa = () => Editointikontrollit.startEditing();
        $scope.lisaaOppimaara = async () => {
            const oppimaara = await oppimaarat.post({});
            oppimaarat.push(oppimaara);
        };
        $scope.lisaaKurssi = async () => {
            const kurssi = await kurssit.post({});
            kurssit.push(kurssi);
        };
        $scope.poista = async () => {
            Varmistusdialogi.dialogi({
                otsikko: 'varmista-poisto',
                teksti: 'poistetaanko-oppiaine',
                primaryBtn: 'poista',
                successCb: async () => {
                    Editointikontrollit.cancelEditing();
                    await $scope.editableModel.remove();
                    $state.go('root.perusteprojekti.suoritustapa.aipeosalistaus', {
                        suoritustapa: $stateParams.suoritustapa,
                        osanTyyppi: AIPEService.VAIHEET
                    }, {
                        reload: true
                    });
                }
            })();
        };

        $scope.fields = [
            {
                path: 'tehtava',
                localeKey: 'oppiaine-osio-tehtava',
                order: 1
            },
            {
                path: 'tyotavat',
                localeKey: 'oppiaine-osio-tyotavat',
                order: 2
            },
            {
                path: 'ohjaus',
                localeKey: 'oppiaine-osio-ohjaus',
                order: 3
            },
            {
                path: 'arviointi',
                localeKey: 'oppiaine-osio-arviointi',
                order: 4
            },
            {
                path: 'sisaltoalueinfo',
                localeKey: 'oppiaine-osio-sisaltoalueinfo',
                order: 5
            }
        ];
        $scope.fieldOps = {
            hasContent: (field) => {
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
                        otsikko: 'varmista-poisto',
                        teksti: 'poistetaanko-osio',
                        primaryBtn: 'poista',
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
                $rootScope.$broadcast('notifyCKEditor');
            },
            add: field => {
                field.visible = true;
                if (!$scope.editableModel[field.path]) {
                    $scope.editableModel[field.path] = {
                        otsikko: {},
                        teksti: {}
                    };
                    const modelField = $scope.editableModel[field.path];
                    modelField.otsikko[Kieli.getSisaltokieli()]= Kaanna.kaanna(field.localeKey);
                }
                field.$editing = true;
            }
        };

        const fieldBackups = {};

        $scope.filterFn = item => {
            return item.visible || _.isUndefined(item.visible);
        };

        function mapModel() {
            _.each($scope.fields, (field) => {
                field.visible = $scope.fieldOps.hasContent(field);
            });
        }
        mapModel();

        $scope.openKoodisto = Koodisto.modaali(koodisto => {
            if (!$scope.editableModel.koodi) {
                $scope.editableModel['koodi'] = {};
            }
            MuokkausUtils.nestedSet($scope.editableModel.koodi, 'koodisto', ',', koodisto.koodisto.koodistoUri);
            MuokkausUtils.nestedSet($scope.editableModel.koodi, 'uri', ',', koodisto.koodiUri);
            MuokkausUtils.nestedSet($scope.editableModel.koodi, 'koodiArvo', ',', koodisto.koodiArvo);
        }, {
            tyyppi: () => { return 'oppiaineetyleissivistava2'; },
            ylarelaatioTyyppi: () => { return ''; },
            tarkista: _.constant(true)
        });

        Editointikontrollit.registerCallback({
            edit: async () => {
                oppiaine = await oppiaine.get();
                $scope.editableModel= Api.copy(oppiaine);
            },
            save: async () => {
                $scope.editableModel = await $scope.editableModel.save();
                Notifikaatiot.onnistui('tallennus-onnistui');
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
    },
    onEnter: PerusteProjektiSivunavi => {
        PerusteProjektiSivunavi.setVisible();
    }
})});

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
                 oppiaineet, oppimaarat, Varmistusdialogi, $state, $stateParams, AIPEService) => {
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

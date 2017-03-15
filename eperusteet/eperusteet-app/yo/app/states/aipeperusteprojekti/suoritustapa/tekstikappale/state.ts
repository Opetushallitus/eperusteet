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
.state("root.aipeperusteprojekti.suoritustapa.tekstikappale", {
    url: "/tekstikappale/{perusteenOsaViiteId}{versio:(?:/[^/]+)?}",
    resolve: {
        perusteenosat: (Api) => Api.all("perusteenosat"),
        perusteenosa: (perusteenosat, $stateParams) => perusteenosat.one("viite", $stateParams.perusteenOsaViiteId).get()
    },
    templateUrl: 'states/aipeperusteprojekti/suoritustapa/tekstikappale/view.html',
    controller: ($scope, $q, Editointikontrollit, PerusteenOsat, Notifikaatiot, VersionHelper, Lukitus,
                 TutkinnonOsaEditMode, Varmistusdialogi, Kaanna, PerusteprojektiTiedotService, $stateParams, Utils,
                 PerusteProjektiSivunavi, YleinenData, $rootScope, Kommentit, KommentitByPerusteenOsa, sisalto,
                 PerusteenOsanTyoryhmat, Tyoryhmat, PerusteprojektiTyoryhmat, TEXT_HIERARCHY_MAX_DEPTH, Api,
                 TekstikappaleOperations, virheService, ProjektinMurupolkuService, $state, perusteenosa, peruste) => {

        $scope.peruste = peruste;
        $scope.tekstikappale = perusteenosa;
        $scope.editableTekstikappale = Api.copy(perusteenosa);
        $scope.versiot = {
            list: {
                $resolved: true
            },
            latest: true
        };
        $scope.sisalto = sisalto;
        $scope.viitteet = {};
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.kaikkiTyoryhmat = [];
        $scope.editEnabled = false;

        function storeTree(sisalto, level = 0) {
            _.each(sisalto.lapset, lapsi => {
                if (lapsi.perusteenOsa) {
                    if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
                        $scope.viitteet[lapsi.perusteenOsa.id] = {};
                    }
                    $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
                    $scope.viitteet[lapsi.perusteenOsa.id].level = level;
                    $scope.viitteet[lapsi.perusteenOsa.id].nimi = lapsi.perusteenOsa.nimi;
                    if (sisalto.perusteenOsa) {
                        $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
                    }
                    storeTree(lapsi, level + 1);
                }
            });
        }

        function updateViitteet() {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
        }

        updateViitteet();

        $scope.canAddLapsi = () => {
            return $scope.tekstikappale.id &&
                $scope.viitteet[$scope.tekstikappale.id] &&
                $scope.viitteet[$scope.tekstikappale.id].level < (TEXT_HIERARCHY_MAX_DEPTH - 1);
        };

        const versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
        if (versio) {
            VersionHelper.getPerusteenOsaVersionsByViite($scope.versiot, {id: $stateParams.perusteenOsaViiteId}, true, function () {
                const revNumber = VersionHelper.select($scope.versiot, versio);
                if (!revNumber) {
                    console.error("Revision number is missing")
                } else {
                    PerusteenOsat.getVersioByViite({
                        viiteId: $stateParams.perusteenOsaViiteId,
                        versioId: revNumber
                    }, (rev) => console.log(rev), (err) => console.error(err));
                }
            });
        } else {
            PerusteenOsat.getByViite({ viiteId: $stateParams.perusteenOsaViiteId },
                (rev) => console.log(rev),
                (err) => console.error(err));
        }

        /*

        $scope.valitseOsaamisala = oa => {
            $scope.editableTekstikappale.osaamisala = oa;
        };

        $scope.kopioiMuokattavaksi = () => {
            TekstikappaleOperations.clone($scope.viitteet[$scope.tekstikappale.id].viite);
        };

        $scope.muokkaa = () => {
            $q(resolve => {
                Lukitus.lukitsePerusteenosa($scope.tekstikappale.id, resolve);
            })()
                .then(Editointikontrollit.startEditing)
                .catch(err => {
                    console.error(err);
                });
        };

        $scope.addLapsi = () => {
            //TekstikappaleOperations.addChild($scope.viiteId(), $stateParams.suoritustapa);
        };

        $scope.haeVersiot = force => {
            //VersionHelper.getPerusteenosaVersions($scope.versiot, {id: $scope.tekstikappale.id}, force);
        };


        $scope.vaihdaVersio = () => {
            $scope.versiot.hasChanged = true;
            VersionHelper.setUrl($scope.versiot);
            //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tekstikappale.id}, responseFn);
        };

        $scope.poista = () => {
            const nimi = Kaanna.kaanna($scope.tekstikappale.nimi);

            Varmistusdialogi.dialogi({
                successCb: doDelete,
                otsikko: 'poista-tekstikappale-otsikko',
                teksti: Kaanna.kaanna('poista-tekstikappale-teksti', { nimi: nimi })
            })();
        };

        function successCb(re) {
            $scope.tekstikappale = re;
            setupTekstikappale($scope.tekstikappale);
            if (TutkinnonOsaEditMode.getMode()) {
                $scope.isNew = true;
                $scope.muokkaa();
            }
        }

        let versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
        if (versio) {
            VersionHelper.getPerusteenOsaVersionsByViite($scope.versiot, {id: $stateParams.perusteenOsaViiteId}, true, () => {
                let revNumber = VersionHelper.select($scope.versiot, versio);
                if (!revNumber) {
                    virheService.virhe('virhe-tekstikappaletta-ei-löytynyt');
                } else {
                    /!*PerusteenOsat.getVersioByViite({
                        viiteId: $stateParams.perusteenOsaViiteId,
                        versioId: revNumber
                    }, successCb, errorCb);*!/
                }
            });
        }

        function paivitaRyhmat(uudet, cb) {
            PerusteenOsanTyoryhmat.save({
                projektiId: $stateParams.perusteProjektiId,
                osaId: $scope.tekstikappale.id
            }, uudet, cb, Notifikaatiot.serverCb);
        }

        $scope.poistaTyoryhma = function (tr) {
            Varmistusdialogi.dialogi({
                successCb: function () {
                    let uusi = _.remove(_.clone($scope.tyoryhmat), function (vanha) {
                        return vanha !== tr;
                    });
                    paivitaRyhmat(uusi, function () {
                        $scope.tyoryhmat = uusi;
                    });
                },
                otsikko: 'poista-tyoryhma-perusteenosasta',
                teksti: Kaanna.kaanna('poista-tyoryhma-teksti', {nimi: tr})
            })();
        };

        $scope.lisaaTyoryhma = function () {
            Tyoryhmat.valitse(_.clone($scope.kaikkiTyoryhmat), _.clone($scope.tyoryhmat), function (uudet) {
                let uusi = _.clone($scope.tyoryhmat).concat(uudet);
                paivitaRyhmat(uusi, function () {
                    $scope.tyoryhmat = uusi;
                });
            });
        };

        Utils.scrollTo('#ylasivuankkuri');
        Kommentit.haeKommentit(KommentitByPerusteenOsa, {
            id: $stateParams.perusteProjektiId,
            perusteenOsaId: $stateParams.perusteenOsaViiteId
        });

        function haeSisalto(cb = _.noop) {
            if ($scope.tiedotService) {
                $scope.tiedotService.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa).then(function (res) {
                    $scope.sisalto = res[0];
                    cb();
                });
            }
        }

        if ($stateParams.suoritustapa || YleinenData.isPerusopetus($scope.$parent.peruste) ||
            YleinenData.isLukiokoulutus($scope.$parent.peruste)) {
            PerusteprojektiTiedotService.then(function (instance) {
                $scope.tiedotService = instance;
                haeSisalto();
            });
        }

        function fetch(cb) {
            PerusteenOsat.get({osanId: $scope.tekstikappale.id}, _.setWithCallback($scope, 'tekstikappale', cb));
        }

        function storeTree(sisalto, level = 0) {
            _.each(sisalto.lapset, function (lapsi) {
                if (lapsi.perusteenOsa) {
                    if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
                        $scope.viitteet[lapsi.perusteenOsa.id] = {};
                    }
                    $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
                    $scope.viitteet[lapsi.perusteenOsa.id].level = level;
                    $scope.viitteet[lapsi.perusteenOsa.id].nimi = lapsi.perusteenOsa.nimi;
                    if (sisalto.perusteenOsa) {
                        $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
                    }
                    storeTree(lapsi, level + 1);
                }
            });
        }
        $scope.tree = {
            init: function () {
                updateViitteet();
            },
            get: function () {
                let items = [];
                let id = $scope.tekstikappale.id;
                if ($scope.viitteet[id]) {
                    do {
                        items.push({
                            label: $scope.viitteet[id].nimi,
                            url: $scope.tekstikappale.id === id ? null : $state.href('root.perusteprojekti.suoritustapa.tekstikappale', {
                                    perusteenOsaViiteId: $scope.viitteet[id].viite,
                                    versio: ''
                                })
                        });
                        id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
                    } while (id);
                }
                items.reverse();
                return items.length > 1 ? items : [];
            }
        };
        $scope.viiteId = () => {
            return $scope.viitteet[$scope.tekstikappale.id] ? $scope.viitteet[$scope.tekstikappale.id].viite : null;
        };

        $scope.fields = [{
            path: 'nimi',
            hideHeader: false,
            localeKey: 'teksikappaleen-nimi',
            type: 'editor-header',
            localized: true,
            mandatory: true,
            mandatoryMessage: 'mandatory-otsikkoa-ei-asetettu',
            order: 1
        }, {
            path: 'teksti',
            hideHeader: false,
            localeKey: 'tekstikappaleen-teksti',
            type: 'editor-area',
            localized: true,
            mandatory: false,
            order: 2
        }];
        function updateViitteet() {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
        }
        function saveCb(res) {
            // Päivitä versiot
            $scope.haeVersiot(true, function () {
                VersionHelper.setUrl($scope.versiot);
            });
            PerusteProjektiSivunavi.refresh();
            Lukitus.vapautaPerusteenosa(res.id);
            Notifikaatiot.onnistui('muokkaus-tekstikappale-tallennettu');
            haeSisalto();
        }

        function doDelete(isNew) {
            TekstikappaleOperations.delete($scope.viiteId(), isNew);
        }

        function setupTekstikappale(kappale) {

            $q.all([PerusteenOsanTyoryhmat.get({
                projektiId: $stateParams.perusteProjektiId,
                osaId: $scope.tekstikappale.id
            }).$promise,
                PerusteprojektiTyoryhmat.get({id: $stateParams.perusteProjektiId}).$promise]).then(function (data) {
                $scope.tyoryhmat = data[0];
                $scope.kaikkiTyoryhmat = _.unique(_.map(data[1], 'nimi'));
            }, Notifikaatiot.serverCb);

            $scope.haeVersiot();
        }*/
        Editointikontrollit.registerCallback({
            edit: () => {
            },
            save: (kommentti) => {
            },
            cancel: () => {

            },
            notify: (mode) => {
                $scope.editEnabled = mode;
            },
            validate: (mandatoryValidator) => {
                return mandatoryValidator($scope.fields, $scope.editableTekstikappale);
            }
        });
    }
}));

import * as angular from "angular";
import _ from "lodash";
import { Lokalisointi } from "scripts/services/utils";

angular
.module("eperusteApp")
.controller("Lops2019Controller", function(
    $scope,
    $state,
    $stateParams,
    Api,
    YleinenData,
    Algoritmit,
    perusteprojektiTiedot,
    TekstikappaleOperations,
    Lops2019Service,
    Editointikontrollit,
    Notifikaatiot
) {
    const manipulateSisaltoUrls = sisalto => {
        Algoritmit.kaikilleLapsisolmuille(sisalto, "lapset", lapsi => {
            switch (_.get(lapsi, "perusteenOsa.osanTyyppi")) {
                case "tekstikappale":
                    lapsi.$url = $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                        suoritustapa: "lukiokoulutus2019",
                        perusteenOsaViiteId: lapsi.id,
                        versio: ""
                    });
                    break;
            }
        });
    };

    const getSisalto = () => {
        const sisalto = perusteprojektiTiedot.getYlTiedot().sisalto;
        manipulateSisaltoUrls(sisalto); // Mutatoi sisältöä
        return sisalto;
    };

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.sisalto = getSisalto();
    $scope.esitysurl = YleinenData.getPerusteEsikatseluLink($scope.projekti, $scope.peruste);
    $scope.rajaus = "";

    $scope.opetus = Lops2019Service.getOpetus();

    TekstikappaleOperations.setPeruste($scope.peruste);

    $scope.addTekstikappale = () => {
        TekstikappaleOperations.add();
    };

    $scope.edit = () => {
        Editointikontrollit.startEditing();
    };

    Editointikontrollit.registerCallback({
        edit: () => {
            $scope.rajaus = "";
        },
        save: () => {
            TekstikappaleOperations.updateViitteet($scope.sisalto, () => {
                Notifikaatiot.onnistui("osien-rakenteen-päivitys-onnistui");
            });
        },
        cancel: () => {
            $state.go($state.current.name, $stateParams, {
                reload: true
            });
        },
        validate: () => {
            return true;
        },
        notify: value => {
            $scope.editing = value;
        }
    });
})
.controller("Lops2019LaajaalaisetController", function (
    $scope,
    Api,
    Editointikontrollit,
    Notifikaatiot,
    laajaalaiset,
    Koodisto
) {
    $scope.laajaalaiset = laajaalaiset.clone();

    $scope.add = (parent, field) => {
        if (!_.has(parent, field)) {
            parent[field] = [];
        }
        parent[field].push({});
    };

    $scope.remove = (target, el) => {
        _.remove(target, el);
    };

    $scope.edit = () => {
        Editointikontrollit.startEditing();
    };

    $scope.openKoodisto = (target, koodisto, isArray) => {
        Koodisto.modaali(
            koodi => {
                const valittu = {
                    arvo: koodi.koodiArvo,
                    uri: koodi.koodiUri,
                    nimi: koodi.nimi,
                    koodisto: koodi.koodisto.koodistoUri,
                    versio: koodi.versio
                };

                if (isArray) {
                    if (!target) {
                        target = [];
                    }
                    target.push(valittu);
                } else {
                    target.koodi = valittu;
                }

                // Muutetaan lao nimi
                target.nimi = Lokalisointi.merge(target.nimi, koodi.nimi);
            },
            {
                tyyppi: () => {
                    return koodisto; // Todo: uusi koodisto hahtuvalla
                },
                ylarelaatioTyyppi: () => {
                    return "";
                },
                tarkista: _.constant(true)
            }
        )();
    };

    Editointikontrollit.registerCallback({
        edit: () => {
            // Todo: Hae uusin versio
        },
        save: async () => {
            try {
                laajaalaiset = await $scope.laajaalaiset.save();
                $scope.laajaalaiset = laajaalaiset.clone();
                Notifikaatiot.onnistui("tallennus-onnistui");
            } catch (e) {
                Notifikaatiot.serverCb(e);
                $scope.laajaalaiset = laajaalaiset.clone();
            }
        },
        cancel: () => {
            $scope.laajaalaiset = laajaalaiset.clone();
        },
        notify: value => {
            $scope.editEnabled = value;
        }
    });
})
.controller("Lops2019OppiaineetController", function (
    $scope,
    $uibModal,
    Api,
    Editointikontrollit,
    Notifikaatiot,
    oppiaineet
) {
    $scope.oppiaineet = oppiaineet.clone();

    $scope.add = async () => {
        $uibModal.open({
            template:
                '' +
                '<div class="modal-header"><h2 kaanna="\'lisaa-oppiaine\'"></h2></div>' +
                '<div class="modal-body">' +
                '  <label for="oppiaine-nimi-input" class="header">{{\'nimi\' | kaanna}}</label>\n' +
                '  <input id="oppiaine-nimi-input" class="form-control" ng-model="oppiaine.nimi" slocalized>' +
                '</div>' +
                '<div class="modal-footer">' +
                '  <button class="btn btn-warning" type="button" ng-click="cancel()" kaanna="\'peruuta\'"></button>' +
                '  <button class="btn btn-primary" type="button" ng-click="ok()" kaanna="\'lisaa\'"></button>' +
                '</div>',
            controller: function($scope, $uibModalInstance) {
                $scope.oppiaine = {};
                $scope.ok = () => {
                    $uibModalInstance.close($scope.oppiaine);
                };
                $scope.cancel = () => {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        }).result.then(async uusi => {
            try {
                const oppiaine = await $scope.oppiaineet.all("uusi").post(uusi);
                $scope.oppiaineet.push(oppiaine);
            } catch (e) {
                Notifikaatiot.serverCb(e);
            }
        });
    };

    $scope.sortableOptions = {
        cursor: "move",
        cursorAt: { top: 2, left: 2 },
        handle: ".handle",
        delay: 100,
        tolerance: "pointer",
        axis: "y"
    };

    $scope.sort = () => {
        Editointikontrollit.startEditing();
    };

    Editointikontrollit.registerCallback({
        edit: () => {
            // Todo: Hae uusin versio
        },
        save: async () => {
            try {
                oppiaineet = await $scope.oppiaineet.post($scope.oppiaineet.plain());
                $scope.oppiaineet = oppiaineet.clone();
                Notifikaatiot.onnistui("tallennus-onnistui");
            } catch (e) {
                Notifikaatiot.serverCb(e);
                $scope.oppiaineet = oppiaineet.clone();
            }
        },
        cancel: () => {
            $scope.oppiaineet = oppiaineet.clone();
        },
        notify: value => {
            $scope.editEnabled = value;
        }
    });
})
.controller('Lops2019OppiaineController', function (
    $scope,
    $state,
    $uibModal,
    Editointikontrollit,
    Varmistusdialogi,
    Koodisto,
    Notifikaatiot,
    oppiaine
) {
    $scope.oppiaine = oppiaine.clone();
    $scope.oppiaine.tavoitteet = $scope.oppiaine.tavoitteet || {};

    $scope.add = (parent, field, obj) => {
        if (!_.has(parent, field)) {
            parent[field] = [];
        }
        if (obj) {
            parent[field].push(obj);
        } else {
            parent[field].push({});
        }
    };

    $scope.remove = (target, element) => {
        _.remove(target, element);
    };

    $scope.addModuuli = async () => {
        $uibModal.open({
            template:
                '' +
                '<div class="modal-header"><h2 kaanna="\'lisaa-moduuli\'"></h2></div>' +
                '<div class="modal-body">' +
                '  <label for="moduuli-nimi-input" class="header">{{\'nimi\' | kaanna}}</label>\n' +
                '  <input id="moduuli-nimi-input" class="form-control" ng-model="moduuli.nimi" slocalized>' +
                '  <div class="checkbox">' +
                '    <label>' +
                '      <input type="checkbox" ng-model="moduuli.pakollinen"> {{\'pakollinen\' | kaanna}}' +
                '    </label>' +
                '  </div>' +
                '</div>' +
                '<div class="modal-footer">' +
                '  <button class="btn btn-warning" type="button" ng-click="cancel()" kaanna="\'peruuta\'"></button>' +
                '  <button class="btn btn-primary" type="button" ng-click="ok()" kaanna="\'lisaa\'"></button>' +
                '</div>',
            controller: function($scope, $uibModalInstance) {
                $scope.moduuli = {
                    pakollinen: true
                };
                $scope.ok = () => {
                    $uibModalInstance.close($scope.moduuli);
                };
                $scope.cancel = () => {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        }).result.then(async moduuli => {
            if (!_.has($scope.oppiaine, 'moduulit')) {
                $scope.oppiaine['moduulit'] = [];
            }
            $scope.oppiaine.moduulit.push(moduuli);
        });
    };

    $scope.addOppimaara = async () => {
        $uibModal.open({
            template:
                '' +
                '<div class="modal-header"><h2 kaanna="\'lisaa-oppimaara\'"></h2></div>' +
                '<div class="modal-body">' +
                '  <label for="oppimaara-nimi-input" class="header">{{\'nimi\' | kaanna}}</label>\n' +
                '  <input id="oppimaara-nimi-input" class="form-control" ng-model="oppimaara.nimi" slocalized>' +
                '</div>' +
                '<div class="modal-footer">' +
                '  <button class="btn btn-warning" type="button" ng-click="cancel()" kaanna="\'peruuta\'"></button>' +
                '  <button class="btn btn-primary" type="button" ng-click="ok()" kaanna="\'lisaa\'"></button>' +
                '</div>',
            controller: function($scope, $uibModalInstance) {
                $scope.oppimaara = {};
                $scope.ok = () => {
                    $uibModalInstance.close($scope.oppimaara);
                };
                $scope.cancel = () => {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        }).result.then(async oppimaara => {
            if (!_.has($scope.oppiaine, 'oppimaarat')) {
                $scope.oppiaine['oppimaarat'] = [];
            }
            $scope.oppiaine.oppimaarat.push(oppimaara);
        });
    };

    $scope.removeOppiaine = () => {
        Varmistusdialogi.dialogi({
            otsikko: "poistetaanko-oppiaine",
            primaryBtn: "poista",
            successCb: async () => {
                try {
                    await $scope.oppiaine.remove();
                    await Editointikontrollit.cancelEditing();

                    if ($scope.oppiaine._oppiaine) {
                        $state.go("root.perusteprojekti.suoritustapa.lops2019oppiaine", {
                            oppiaineId: $scope.oppiaine._oppiaine
                        });
                    } else {
                        $state.go("root.perusteprojekti.suoritustapa.lops2019oppiaineet");
                    }
                } catch (e) {
                    Notifikaatiot.serverCb(e);
                }
            }
        })();
    };

    $scope.sortableOptions = {
        cursor: "move",
        //cursorAt: { top: 2, left: 2 },
        handle: ".handle",
        delay: 100,
        tolerance: "pointer",
        axis: "y"
    };

    $scope.edit = () => {
        Editointikontrollit.startEditing();
    };

    $scope.openKoodisto = (target, koodisto, isArray) => {
        Koodisto.modaali(
            koodi => {
                const valittu = {
                    arvo: koodi.koodiArvo,
                    uri: koodi.koodiUri,
                    nimi: koodi.nimi,
                    koodisto: koodi.koodisto.koodistoUri,
                    versio: koodi.versio
                };

                if (isArray) {
                    if (!target) {
                        target = [];
                    }
                    target.push(valittu);
                } else {
                    target.koodi = valittu;
                }

                // Muutetaan oppiaineen nimi
                target.nimi = Lokalisointi.merge(target.nimi, koodi.nimi);
            },
            {
                tyyppi: () => {
                    return koodisto; // Todo: uusi koodisto hahtuvalla
                },
                ylarelaatioTyyppi: () => {
                    return "";
                },
                tarkista: _.constant(true)
            }
        )();
    };

    Editointikontrollit.registerCallback({
        edit: () => {
            // Todo: Hae uusin versio
        },
        save: async () => {
            try {
                oppiaine = await $scope.oppiaine.save();
                $scope.oppiaine = oppiaine.clone();
                $scope.oppiaine.tavoitteet = $scope.oppiaine.tavoitteet || {};
                Notifikaatiot.onnistui("tallennus-onnistui");
            } catch (e) {
                Notifikaatiot.serverCb(e);
                $scope.oppiaine = oppiaine.clone();
                $scope.oppiaine.tavoitteet = $scope.oppiaine.tavoitteet || {};
            }
        },
        cancel: () => {
            $scope.oppiaine = oppiaine.clone();
            $scope.oppiaine.tavoitteet = $scope.oppiaine.tavoitteet || {};
        },
        notify: value => {
            $scope.editEnabled = value;
        }
    });
})
.controller("Lops2019ModuuliController", function (
    $scope,
    $state,
    $stateParams,
    Editointikontrollit,
    Varmistusdialogi,
    Koodisto,
    Notifikaatiot,
    moduuli
) {
    $scope.moduuli = moduuli.clone();
    $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};

    $scope.edit = () => {
        Editointikontrollit.startEditing();
    };

    $scope.add = (parent, field, obj) => {
        if (!_.has(parent, field) || !parent[field]) {
            parent[field] = [];
        }

        if (obj) {
            parent[field].push(obj);
        } else {
            parent[field].push({});
        }
    };

    $scope.remove = (target, el) => {
        _.remove(target, el);
    };

    $scope.removeModuuli = () => {
        Varmistusdialogi.dialogi({
            otsikko: "poistetaanko-moduuli",
            primaryBtn: "poista",
            successCb: async () => {
                try {
                    await $scope.moduuli.remove();
                    await Editointikontrollit.cancelEditing();
                    $state.go("root.perusteprojekti.suoritustapa.lops2019oppiaine", {
                        oppiaineId: $stateParams.oppiaineId
                    });
                } catch (e) {
                    Notifikaatiot.serverCb(e);
                }
            }
        })();
    };

    $scope.openKoodisto = (target, koodisto) => {
        Koodisto.modaali(
            koodi => {
                target.koodi = {
                    arvo: koodi.koodiArvo,
                    uri: koodi.koodiUri,
                    nimi: koodi.nimi,
                    koodisto: koodi.koodisto.koodistoUri,
                    versio: koodi.versio
                };

                // Muutetaan moduulin nimi
                target.nimi = Lokalisointi.merge(target.nimi, koodi.nimi);
            },
            {
                tyyppi: () => {
                    return koodisto; // Todo: uusi koodisto hahtuvalla
                },
                ylarelaatioTyyppi: () => {
                    return "";
                },
                tarkista: _.constant(true)
            }
        )();
    };

    Editointikontrollit.registerCallback({
        edit: () => {
            // Todo: Hae uusin versio
        },
        save: async () => {
            try {
                moduuli = await $scope.moduuli.save();
                $scope.moduuli = moduuli.clone();
                $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};
                Notifikaatiot.onnistui("tallennus-onnistui");
            } catch (e) {
                Notifikaatiot.serverCb(e);
                $scope.moduuli = moduuli.clone();
                $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};
            }
        },
        cancel: () => {
            $scope.moduuli = moduuli.clone();
            $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};
        },
        notify: value => {
            $scope.editEnabled = value;
        }
    });
});

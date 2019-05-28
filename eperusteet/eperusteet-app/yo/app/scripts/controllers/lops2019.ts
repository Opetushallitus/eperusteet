import * as angular from "angular";
import _ from "lodash";

angular
.module("eperusteApp")
.controller("Lops2019Controller", function(
    $scope,
    $state,
    Api,
    YleinenData,
    Algoritmit,
    perusteprojektiTiedot,
    TekstikappaleOperations,
    Lops2019Service
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

})
.controller("Lops2019LaajaalaisetController", function (
    $scope,
    Api,
    Editointikontrollit,
    laajaalaiset
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

    Editointikontrollit.registerCallback({
        edit: () => {
            // Todo: Hae uusin versio
        },
        save: async () => {
            laajaalaiset = (await $scope.laajaalaiset.save());
            $scope.laajaalaiset = laajaalaiset.clone();
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
            const oppiaine = await $scope.oppiaineet.post(uusi);
            $scope.oppiaineet.push(oppiaine);
        });
    };
})
.controller('Lops2019OppiaineController', function (
    $scope,
    $state,
    $uibModal,
    Editointikontrollit,
    Varmistusdialogi,
    Koodisto,
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
                await $scope.oppiaine.remove();
                await Editointikontrollit.cancelEditing();
                $state.go("root.perusteprojekti.suoritustapa.lops2019oppiaineet");
            }
        })();
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
            oppiaine = await $scope.oppiaine.save();
            $scope.oppiaine = oppiaine.clone();
            $scope.oppiaine.tavoitteet = $scope.oppiaine.tavoitteet || {};
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
    Editointikontrollit,
    Koodisto,
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
            moduuli = await $scope.moduuli.save();
            $scope.moduuli = moduuli.clone();
            $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};
        },
        cancel: () => {
            $scope.oppiaine = moduuli.clone();
            $scope.moduuli.tavoitteet = $scope.moduuli.tavoitteet || {};
        },
        notify: value => {
            $scope.editEnabled = value;
        }
    });
});

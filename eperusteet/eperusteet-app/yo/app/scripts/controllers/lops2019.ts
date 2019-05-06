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
        if (!parent.hasOwnProperty(field)) {
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
    oppiaineet
) {
    $scope.oppiaineet = oppiaineet;

    $scope.add = async () => {

    };
});

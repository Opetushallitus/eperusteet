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
.controller("Lops2019LaajaalaisetController", function () {
    console.log("Lops2019LaajaalaisetController");
})
.controller("Lops2019OppiaineetController", function () {
    console.log("Lops2019OppiaineetController");
});

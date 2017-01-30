angular.module('eperusteApp')
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.aipe", {
    url: "/aipe",
    resolve: {
        perusteprojektit: (Api) => Api.all("perusteprojektit"),
        perusteprojekti: (perusteprojektit, $stateParams) => perusteprojektit.one($stateParams.perusteProjektiId).get(),
        perusteet: (Api) => Api.all("perusteet"),
        peruste: (perusteprojekti, perusteet) => perusteet.get(perusteprojekti._peruste),
        aipeopetus: (peruste) => peruste.one("aipeopetus"),
        vaiheet: (aipeopetus) => aipeopetus.one("vaiheet").getList(),
        laajaalaiset: (aipeopetus) => aipeopetus.one("laajaalaiset").getList(),
        sisalto: (peruste) => peruste.one("suoritustavat/aipe/sisalto").get()
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/view.html",
            controller: ($scope, $state, $stateParams, peruste, vaiheet, laajaalaiset, sisalto) => {
                console.log("Hello from aipe", $stateParams, sisalto);
                $scope.peruste = peruste;
                $scope.vaiheet = vaiheet;
                $scope.laajaalaiset = laajaalaiset;
            }
        }
    }
}));

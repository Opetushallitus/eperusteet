angular.module('eperusteApp')
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.aipe.vaihe.oppiaine.kurssi", {
    url: "/kurssit/:kurssiId",
    resolve: {
        kurssi: (oppiaine, $stateParams) => oppiaine.get($stateParams.kurssiId),
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/vaihe/oppiaine/kurssi/view.html",
            controller: ($scope, $state, $stateParams, kurssi) => {
                $scope.kurssi = kurssi;
            }
        }
    }
}));

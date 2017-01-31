angular.module('eperusteApp')
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.aipe.laajaalainen", {
    url: "/laajaalainen/:laajaalainenId",
    resolve: {
        laajaalainen: (laajaalaiset, $stateParams) => laajaalaiset.get($stateParams.laajaalainenId),
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/laajaalainen/view.html",
            controller: ($scope, $state, $stateParams, laajaalainen) => {
                $scope.laajaalainen = laajaalainen;
            }
        }
    }
}));

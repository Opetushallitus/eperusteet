angular.module('eperusteApp')
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.aipe.vaihe", {
    url: "/vaihe/:vaiheId",
    resolve: {
        vaihe: (vaiheet, $stateParams) => vaiheet.get($stateParams.vaiheId),
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/vaihe/view.html",
            controller: ($scope, $state, $stateParams, vaihe) => {
                $scope.vaihe = vaihe;
            }
        }
    }
}));

angular.module('eperusteApp')
.config($stateProvider => $stateProvider
.state("root.perusteprojekti.aipe.vaihe.oppiaine", {
    url: "/oppiaineet/:oppiaineId",
    resolve: {
        oppiaine: (vaihe, $stateParams) => vaihe.get($stateParams.oppiaineId),
    },
    views: {
        "": {
            templateUrl: "scripts/aipe/vaihe/oppiaine/view.html",
            controller: ($scope, $state, $stateParams, oppiaine) => {
                $scope.oppiaine = oppiaine;
            }
        }
    }
}));

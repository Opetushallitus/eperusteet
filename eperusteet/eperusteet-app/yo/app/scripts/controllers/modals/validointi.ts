import * as angular from "angular";
import _ from "lodash";


angular.module("eperusteApp")
.service("ValidointivirheService", ($uibModal, Api) => {
    return {
        async validoiProjekti(projektiId: number) {
            return $uibModal
                .open({
                    template: require("views/modals/validointivirheet.html"),
                    controller: "ValidointiCtrl",
                    size: "lg",
                    resolve: {
                        async validointidata(Api) {
                            const validointi = await Api.one(`/perusteprojektit/${projektiId}/validoi`).get();
                            return validointi;
                        },
                    },
                })
                .result;
        }
    };
})
.controller("ValidointiCtrl", function($scope, $uibModalInstance, validointidata) {
    $scope.data = validointidata;

    $scope.ok = function() {
        $uibModalInstance.close();
    };

    $scope.peruuta = function() {
        $uibModalInstance.dismiss();
    };
});

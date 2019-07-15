import * as angular from "angular";
import _ from "lodash";

angular.module("eperusteApp").controller("AikakatkoDialogiCtrl", function($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close();
    };
    $scope.peruuta = function() {
        $uibModalInstance.dismiss();
    };
});

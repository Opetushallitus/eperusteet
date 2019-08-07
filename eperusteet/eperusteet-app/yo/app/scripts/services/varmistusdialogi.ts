import * as angular from "angular";
import _ from "lodash";


angular
    .module("eperusteApp")
    .service("Varmistusdialogi", function($uibModal) {
        function dialogi(options) {
            return async function(success, failure) {
                var resolve = {
                    opts: function() {
                        return {
                            primaryBtn: options.primaryBtn || "ok",
                            secondaryBtn: options.secondaryBtn || "peruuta"
                        };
                    },
                    data: function() {
                        return options.data || null;
                    },
                    otsikko: function() {
                        return options.otsikko || "";
                    },
                    teksti: function() {
                        return options.teksti || "";
                    },
                    lisaTeksti: function() {
                        return options.lisaTeksti || "";
                    },
                    comment: function() {
                        return options.comment || {};
                    }
                };
                var successCb = success || options.successCb || angular.noop;
                var failureCb = failure || options.failureCb || angular.noop;

                return $uibModal
                    .open({
                        template: require("views/modals/varmistusdialogi.html"),
                        controller: "VarmistusDialogiCtrl",
                        resolve: resolve
                    })
                    .result.then(successCb, failureCb);
            };
        }

        return {
            dialogi: dialogi
        };
    })
    .controller("VarmistusDialogiCtrl", function(
        $scope,
        $uibModalInstance,
        opts,
        data,
        otsikko,
        teksti,
        lisaTeksti,
        comment
    ) {
        $scope.opts = opts;
        $scope.otsikko = otsikko;
        $scope.teksti = teksti;
        $scope.lisaTeksti = lisaTeksti;
        $scope.comment = comment;

        $scope.ok = function() {
            if (data !== null) {
                $uibModalInstance.close(data);
            } else {
                $uibModalInstance.close();
            }
        };

        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };
    });

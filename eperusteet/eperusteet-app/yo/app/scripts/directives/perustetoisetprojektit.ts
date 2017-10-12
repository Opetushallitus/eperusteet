import * as angular from "angular";
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .service("MuutProjektitService", (Api, Varmistusdialogi, $uibModal) => {
        let osa;
        let awaiter;

        async function projektitJoissaKaytossa(perusteId: number | string) {
            if (!osa || !osa.id || !perusteId === osa) {
                if (!awaiter) {
                    awaiter = new Promise(async (resolve, reject) => {
                        const result = await Api.one("perusteenosat", "" + perusteId)
                            .all("projektit")
                            .getList();
                        osa = result.plain();
                        resolve(_.clone(osa));
                    });
                    await awaiter;
                    awaiter = undefined;
                } else {
                    await awaiter;
                }
            }
            return _.clone(osa);
        }

        async function varmistusdialogi(perusteenId) {
            return new Promise(async (resolve, reject) => {
                const projektit = await projektitJoissaKaytossa(perusteenId);
                if (_.size(projektit) > 1) {
                    return $uibModal
                        .open({
                            size: "lg",
                            template: require("views/modals/perusteenosaprojektidialogi.html"),
                            controller: ($scope, $uibModalInstance, projektit) => {
                                $scope.projektit = projektit;
                                $scope.ok = () => $uibModalInstance.close();
                                $scope.peruuta = () => $uibModalInstance.dismiss();
                            },
                            resolve: {
                                projektit() {
                                    return projektit;
                                }
                            }
                        })
                        .result.then(resolve, reject);
                } else {
                    resolve();
                }
            });
        }

        return {
            projektitJoissaKaytossa,
            varmistusdialogi
        };
    })
    .directive("muutProjektit", function($timeout, $compile, $document, MuutProjektitService) {
        return {
            template: require("views/partials/muutperusteenosat.html"),
            restrict: "E",
            transclude: true,
            scope: {
                perusteid: "="
            },
            controller($scope, $stateParams) {
                $scope.kaytetyt = [];
                async function update(perusteId: number) {
                    const result = await MuutProjektitService.projektitJoissaKaytossa(perusteId);
                    $scope.kaytetyt = _.filter(result, (pp: any) => pp.id + "" !== $stateParams.perusteProjektiId);
                }

                $scope.$watch("perusteid", (o, v) => {
                    if ($scope.perusteid) {
                        update($scope.perusteid);
                    }
                });
            }
        };
    });

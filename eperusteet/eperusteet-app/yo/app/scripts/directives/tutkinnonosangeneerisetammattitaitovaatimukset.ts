import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .controller("tutkinnonosangeneerisetammattitaitovaatimuksetCtrl", function(ArviointiasteikkoHelper, Api, $scope, YleinenData, $timeout, Utils, Varmistusdialogi) {
        $scope.state = {};
        let geneeriset: any[] = [];

        async function init() {
            geneeriset = (await Api.all("geneerinenarviointi").getList()).plain();
            $scope.arviointiasteikot = await ArviointiasteikkoHelper.getMappedArviointiasteikot();
            $scope.geneeriset = _.filter(geneeriset, "julkaistu");

            if($scope.geneerinen) {
                $scope.valittuId = _.parseInt($scope.geneerinen.id);
                $scope.valittu = _(geneeriset)
                    .filter({ id: $scope.valittuId })
                    .first();
            }
        }

        $scope.changeGeneerinen = (id) => {
            $scope.valittuId = id;
            $scope.valittu = _(geneeriset)
                .filter({ id: _.parseInt(id) })
                .first();
            $scope.geneerinen = $scope.valittu;
        };
        init();
    })
    .directive("tutkinnonosangeneerisetammattitaitovaatimukset", function(YleinenData, $timeout) {
        return {
            template: require("views/partials/geneerisetammattitaitovaatimukset.pug"),
            restrict: "E",
            scope: {
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                geneerinen: "=",
                type: "@"
            },
            controller: "tutkinnonosangeneerisetammattitaitovaatimuksetCtrl",
            link(scope: any) {
            },
        };
    });

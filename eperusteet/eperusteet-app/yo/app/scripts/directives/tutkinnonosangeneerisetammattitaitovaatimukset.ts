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
            const asteikkoId = _.parseInt($scope.geneerinen);
            $scope.valittu = _(geneeriset)
                .filter({ id: asteikkoId })
                .first();
            $scope.state.valittu = asteikkoId;
        }

        $scope.changeGeneerinen = (id) => {
            $scope.geneerinen = id;
            $scope.valittu = _(geneeriset)
                .filter({ id: _.parseInt(id) })
                .first();
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

import * as angular from "angular";
import _ from "lodash";


angular
    .module("eperusteApp")
    .controller("tutkinnonosanAmmattitaitovaatimukset2019Ctrl", function(
        $scope,
        $timeout,
        Api,
        Utils,
        Varmistusdialogi,
        YleinenData,
        Kieli,
        $translate,
    ) {

        $scope.ammattitaitovaatimukset = $scope.ammattitaitovaatimukset ||
        {
            kohde: Kieli.SISALTOKIELET.reduce( (current, lang) => {
                current[lang] = $translate.instant("opiskelija", {}, undefined, lang);
                return current;
              }, {})
        };

        $scope.sortableOptionsKohdealue = {
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            handle: ".handle",
            delay: 100,
            tolerance: "pointer"
        };

        $scope.sortableOptionsAmmattitaitovaatimukset = {
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            handle: ".handle",
            delay: 100,
            tolerance: "pointer"
        };

        $scope.koodiSelector = (koodi, vaatimus) => {
            vaatimus.koodi = {
                arvo: koodi.koodiArvo,
                uri: koodi.koodiUri,
                koodisto: koodi.koodisto.koodistoUri,
            };
            vaatimus.vaatimus = koodi.nimi;
        };

        $scope.addVaatimus = (kohdealue?) => {
            if (kohdealue) {
                if (!kohdealue.vaatimukset) {
                    kohdealue.vaatimukset = [];
                }
                kohdealue.vaatimukset.push({});
            }
            else {
                if (!$scope.ammattitaitovaatimukset.vaatimukset) {
                    $scope.ammattitaitovaatimukset.vaatimukset = [];
                }
                $scope.ammattitaitovaatimukset.vaatimukset.push({});
            }
        };

        $scope.addKohdealue = () => {
            if (!$scope.ammattitaitovaatimukset.kohdealueet) {
                $scope.ammattitaitovaatimukset.kohdealueet = [];
            }
            $scope.ammattitaitovaatimukset.kohdealueet.push({
            });
        };

        $scope.poistaVaatimus = (vaatimus, kohdealue?) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-vaatimus",
                primaryBtn: "poista",
                successCb() {
                    if (kohdealue) {
                        _.remove(kohdealue.vaatimukset, vaatimus);
                    }
                    else {
                        _.remove($scope.ammattitaitovaatimukset.vaatimukset, vaatimus);
                    }
                }
            })();
        };

        $scope.poistaKohdealue = (kohdealue) => {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-kohdealue",
                primaryBtn: "poista",
                successCb() {
                    _.remove($scope.ammattitaitovaatimukset.kohdealueet, kohdealue);
                }
            })();
        };

        $scope.toggleVaatimus = (vaatimus) => {
            vaatimus.$$open = !vaatimus.$$open;
        };
    })
    .directive("tutkinnonosanAmmattitaitovaatimukset2019", function(YleinenData, $timeout) {
        return {
            template: require("views/partials/ammattitaitovaatimukset2019.pug"),
            restrict: "E",
            scope: {
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                ammattitaitovaatimukset: "=",
                type: "@"
            },
            controller: "tutkinnonosanAmmattitaitovaatimukset2019Ctrl",
            link(scope: any) {
            },
        };
    });

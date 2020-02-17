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
        Koodisto,
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

            if (koodi.nimi) {
                vaatimus.vaatimus = koodi.nimi;
            } else if (!_.isEmpty(koodi.metadata)) {

                const nimi = {
                    fi: "",
                    sv: "",
                    en: ""
                };
                _.forEach(koodi.metadata, function(obj) {
                    nimi[obj.kieli.toLowerCase()] = obj.nimi;
                });

                vaatimus.vaatimus = nimi;
            }
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

        $scope.tarkistaKoodistosta = _.debounce(async (vaatimus) => {

            if(_.size(vaatimus.vaatimus[Kieli.getSisaltokieli()]) > 3 && !vaatimus.koodi) {
                vaatimus.haku = true;
                const sivutettuData = await Koodisto.haeSivutettu('ammattitaitovaatimukset', () => {}, 0, 9999, vaatimus.vaatimus[Kieli.getSisaltokieli()], false);
                vaatimus.koodivastaavuus = (_.chain(sivutettuData.data) as any)
                    .filter((koodi: any) => _.size(_.filter(koodi.metadata, (metadata: any) => metadata.nimi === vaatimus.vaatimus[Kieli.getSisaltokieli()])) > 0)
                    .head()
                    .value();

                vaatimus.haku = false;
            }

        }, 500);

        $scope.kaytaKoodistosta = (vaatimus) => {
            $scope.koodiSelector(vaatimus.koodivastaavuus, vaatimus);
            vaatimus.koodivastaavuus = null;
        }
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

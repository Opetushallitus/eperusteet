import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .directive("kommentit", function(Kommentit, $timeout, $location, kayttajaToiminnot, Varmistusdialogi, YleinenData) {
        return {
            restrict: "AE",
            template: require("views/kommentit.html"),
            scope: {},
            controller: function($scope) {
                $scope.nayta = false;
                $scope.editointi = false;
                $scope.editoitava = "";
                $scope.editoi = false;
                $scope.sisalto = false;
                $scope.onLataaja = false;
                $scope.urlit = {};
                $scope.nimikirjaimet = kayttajaToiminnot.nimikirjaimet;

                $scope.$kommenttiMaxLength = {
                    maara: YleinenData.kommenttiMaxLength
                };

                const lataaKommentit = _.debounce(url => {
                    var lataaja = $scope.urlit[url];
                    if (lataaja) {
                        lataaja(function(kommentit) {
                            $scope.sisalto = kommentit;
                            $scope.nayta = true;
                        });
                    }
                }, 300);

                function naytaKommentit() {
                    lataaKommentit($location.url());
                }

                $scope.$on("$stateChangeStart", function() {
                    $scope.nayta = false;
                    $scope.onLataaja = false;
                });

                function lataajaCb(url, lataaja) {
                    if (!$scope.urlit[url]) {
                        $scope.onLataaja = true;
                        $scope.urlit[url] = lataaja;
                    }
                    naytaKommentit();
                }

                var stored = Kommentit.stored();
                if (!_.isEmpty(stored)) {
                    lataajaCb(stored.url, stored.lataaja);
                }

                $scope.$on("update:kommentit", function(event, url, lataaja) {
                    lataajaCb(url, lataaja);
                });

                $scope.muokkaaKommenttia = function(kommentti, uusikommentti, cb) {
                    Kommentit.muokkaaKommenttia(kommentti, uusikommentti, cb);
                };

                $scope.poistaKommentti = function(kommentti) {
                    Varmistusdialogi.dialogi({
                        otsikko: "vahvista-poisto",
                        teksti: "poistetaanko-kommentti",
                        primaryBtn: "poista",
                        successCb: function() {
                            Kommentit.poistaKommentti(kommentti);
                        }
                    })();
                };
                $scope.lisaaKommentti = function(parent, kommentti, cb) {
                    Kommentit.lisaaKommentti(parent, kommentti, function() {
                        $scope.sisalto.$yhteensa += 1;
                        (cb || angular.noop)();
                    });
                };

                $scope.$on("enableEditing", function() {
                    $scope.editointi = true;
                });
                $scope.$on("disableEditing", function() {
                    $scope.editointi = false;
                });

                $timeout(function() {
                    naytaKommentit();
                });
            }
        };
    });

import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .service("Koodisto", function($http, $uibModal, SERVICE_LOC, $resource, Kaanna, Notifikaatiot, Utils) {
        var taydennykset = [];
        var koodistoVaihtoehdot = [
            "ammattitaitovaatimukset",
            "tutkinnonosat",
            "tutkintonimikkeet",
            "koulutus",
            "osaamisala",
        ];
        var nykyinenKoodisto = _.first(koodistoVaihtoehdot);
        var lisaFiltteri = function() {
            return true;
        };

        function hae(koodisto, cb) {
            if (!_.isEmpty(taydennykset) && koodisto === nykyinenKoodisto) {
                cb();
                return;
            }
            $http.get(SERVICE_LOC + "/koodisto/" + koodisto).then(res => {
                taydennykset = koodistoMapping(res.data);
                nykyinenKoodisto = koodisto;
                taydennykset = _.sortBy(taydennykset, Utils.nameSort);
                cb();
            }, Notifikaatiot.serverCb);
        }

        function haeAlarelaatiot(koodi, cb) {
            var resource = $resource(SERVICE_LOC + "/koodisto/relaatio/sisaltyy-alakoodit/:koodi");
            resource.query({ koodi: koodi }, function(vastaus) {
                var relaatiot = koodistoMapping(vastaus);
                cb(relaatiot);
            });
        }

        function haeYlarelaatiot(koodi, tyyppi, cb) {
            if (!_.isEmpty(taydennykset) && koodi === nykyinenKoodisto) {
                cb();
                return;
            }
            var resource = $resource(SERVICE_LOC + "/koodisto/relaatio/sisaltyy-ylakoodit/:koodi");
            resource.query({ koodi: koodi }, function(re) {
                taydennykset = suodataTyypinMukaan(re, tyyppi);
                taydennykset = koodistoMapping(taydennykset);
                taydennykset = _.sortBy(taydennykset, Utils.nameSort);
                nykyinenKoodisto = koodi;
                cb();
            });
        }

        function suodataTyypinMukaan(koodistodata, tyyppi) {
            return _.filter(koodistodata, function(data) {
                return (data as any).koodiUri.substr(0, tyyppi.length) === tyyppi;
            });
        }

        function koodistoMapping(koodistoData) {
            return _(koodistoData)
                .map(function(kd) {
                    const nimi = {
                        fi: "",
                        sv: "",
                        en: ""
                    };
                    _.forEach(kd.metadata, function(obj) {
                        nimi[obj.kieli.toLowerCase()] = obj.nimi;
                    });

                    const haku = _.reduce(_.values(nimi), (result, v: string) => (result + v).toLowerCase());
                    return {
                        nimi,
                        haku,
                        koodiArvo: kd.koodiArvo,
                        koodiUri: kd.koodiUri,
                        koodisto: kd.koodisto,
                        voimassaAlkuPvm: kd.voimassaAlkuPvm
                    };
                })
                .value();
        }

        function filtteri(haku) {
            haku = haku.toLowerCase();
            return _.filter(taydennykset, function(t) {
                return t.koodiUri.indexOf(haku) !== -1 || t.haku.indexOf(haku) !== -1;
            });
        }

        function modaali(successCb, resolve, failureCb, lisaf, payload?) {
            if (filtteri) {
                lisaFiltteri = lisaf;
            }
            return function() {
                resolve = _.merge(
                    {
                        tarkista: _.constant(false)
                    },
                    resolve || {}
                );
                failureCb = failureCb || angular.noop;
                $uibModal
                    .open({
                        template: require("views/modals/koodistoModal.html"),
                        controller: "KoodistoModalCtrl",
                        resolve: resolve
                    })
                    .result.then((...args) => successCb(...args, payload), failureCb);
            };
        }

        return {
            hae: hae,
            filtteri: filtteri,
            vaihtoehdot: _.clone(koodistoVaihtoehdot),
            modaali: modaali,
            haeAlarelaatiot: haeAlarelaatiot,
            haeYlarelaatiot: haeYlarelaatiot
        };
    })
    .controller("KoodistoModalCtrl", function(
        $scope,
        $uibModalInstance,
        $timeout,
        Koodisto,
        tyyppi,
        ylarelaatioTyyppi,
        TutkinnonOsanKoodiUniqueResource,
        Notifikaatiot,
        tarkista
    ) {
        $scope.koodistoVaihtoehdot = Koodisto.vaihtoehdot;
        $scope.tyyppi = tyyppi;
        $scope.ylarelaatioTyyppi = ylarelaatioTyyppi;
        $scope.loydetyt = [];
        $scope.totalItems = 0;
        $scope.itemsPerPage = 10;
        $scope.nykyinen = 1;
        $scope.lataa = true;
        $scope.syote = "";

        $scope.valitseSivu = function(sivu) {
            if (sivu > 0 && sivu <= Math.ceil($scope.totalItems / $scope.itemsPerPage)) {
                $scope.nykyinen = sivu;
            }
        };

        $scope.haku = function(rajaus) {
            $scope.loydetyt = Koodisto.filtteri(rajaus);
            $scope.totalItems = _.size($scope.loydetyt);
            $scope.valitseSivu(1);
        };

        function hakuCb() {
            $scope.lataa = false;
            $scope.haku("");
            $timeout(function() {
                $("#koodisto_modal_autofocus").focus();
            }, 0);
        }

        if ($scope.ylarelaatioTyyppi === "") {
            Koodisto.hae($scope.tyyppi, hakuCb);
        } else {
            Koodisto.haeYlarelaatiot($scope.ylarelaatioTyyppi, $scope.tyyppi, hakuCb);
        }

        $scope.ok = function(koodi) {
            if (tarkista) {
                TutkinnonOsanKoodiUniqueResource.get({ tutkinnonosakoodi: koodi.koodiUri }, function(res) {
                    if (res.vastaus) {
                        $uibModalInstance.close(koodi);
                    } else {
                        Notifikaatiot.varoitus("tutkinnon-osan-koodi-kaytossa");
                    }
                });
            } else {
                $uibModalInstance.close(koodi);
            }
        };
        $scope.peruuta = function() {
            $uibModalInstance.dismiss();
        };
    })
    .directive("koodistoSelect", function(Koodisto) {
        return {
            template:
                '<button class="btn btn-default" type="text" ng-click="activate()">{{ "hae-koodistosta" | kaanna }}</button>',
            restrict: "E",
            scope: {
                valmis: "=",
                filtteri: "=",
                tyyppi: "@",
                ylarelaatioTyyppi: "=?",
                payload: "=?",
            },
            controller: function($scope) {
                $scope.tyyppi = $scope.tyyppi;
                $scope.ylarelaatioTyyppi = $scope.ylarelaatiotyyppi || "";

                if (!$scope.valmis) {
                    console.error("koodisto-select: valmis-callback puuttuu");
                    return;
                } else if (_.indexOf(Koodisto.vaihtoehdot, $scope.tyyppi) === -1) {
                    console.error(
                        "koodisto-select:",
                        $scope.tyyppi,
                        "ei vastaa mitään mitään vaihtoehtoa:",
                        Koodisto.vaihtoehdot
                    );
                    return;
                }
            },
            link: function($scope: any, el, attrs: any) {
                attrs.$observe("ylarelaatiotyyppi", function() {
                    $scope.ylarelaatioTyyppi = attrs.ylarelaatiotyyppi || "";
                });

                $scope.activate = function() {
                    Koodisto.modaali($scope.valmis, {
                            tyyppi: function() {
                                return $scope.tyyppi;
                            },
                            ylarelaatioTyyppi: function() {
                                return $scope.ylarelaatioTyyppi;
                            }
                        },
                        angular.noop,
                        $scope.filtteri,
                        $scope.payload,
                    )();
                };
            }
        };
    });

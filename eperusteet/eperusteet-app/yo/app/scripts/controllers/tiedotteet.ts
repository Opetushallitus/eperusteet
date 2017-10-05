/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

angular
    .module("eperusteApp")
    .factory("TiedotteetCRUD", function($resource, SERVICE_LOC) {
        return $resource(SERVICE_LOC + "/tiedotteet/:tiedoteId", {
            tiedoteId: "@id"
        });
    })
    .controller("SivupalkkiTiedotteetController", function(
        $scope,
        Algoritmit,
        $uibModal,
        Varmistusdialogi,
        TiedotteetCRUD,
        Notifikaatiot
    ) {
        $scope.tiedotteet = [];
        $scope.naytto = { limit: 5, shown: 5 };

        function fetch() {
            // Hae tiedotteet viimeisen 6 kuukauden ajalta
            var MONTH_OFFSET = 6;
            var tempDate = new Date();
            tempDate.setMonth(tempDate.getMonth() - MONTH_OFFSET);
            var alkaen = tempDate.getTime();

            TiedotteetCRUD.query(
                { alkaen: alkaen },
                function(res) {
                    $scope.tiedotteet = res;
                },
                Notifikaatiot.serverCb
            );
        }
        fetch();

        $scope.orderFn = function(item) {
            return -1 * item.luotu;
        };
    })
    .controller("TiedotteidenHallintaController", function(
        $scope,
        Algoritmit,
        $uibModal,
        Varmistusdialogi,
        TiedotteetCRUD,
        Notifikaatiot,
        Utils,
        TiedoteService
    ) {
        $scope.tiedotteet = [];
        $scope.jarjestysTapa = "muokattu";
        $scope.jarjestysOrder = false;

        $scope.paginate = {
            perPage: 10,
            current: 1
        };

        function fetch() {
            TiedotteetCRUD.query(
                {},
                function(res) {
                    $scope.tiedotteet = res;
                },
                Notifikaatiot.serverCb
            );
        }
        fetch();

        $scope.search = {
            term: "",
            changed: function() {
                $scope.paginate.current = 1;
            },
            filterFn: function(item) {
                return $scope.search.term ? Algoritmit.match($scope.search.term, item.otsikko) : true;
            }
        };

        $scope.setOrderBy = function(key) {
            if ($scope.jarjestysTapa === key) {
                $scope.jarjestysOrder = !$scope.jarjestysOrder;
            } else {
                $scope.jarjestysOrder = false;
                $scope.jarjestysTapa = key;
            }
        };

        $scope.orderFn = function(item) {
            switch ($scope.jarjestysTapa) {
                case "nimi":
                    return Utils.nameSort(item, "otsikko");
                case "luotu":
                    return -1 * item.luotu;
                case "muokattu":
                    return -1 * item.muokattu;
                case "julkinen":
                    return "" + item.julkinen;
                default:
                    break;
            }
        };

        $scope.delete = function(model) {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-tiedote"
            })(function() {
                TiedoteService.delete(model, function() {
                    _.remove($scope.tiedotteet, model);
                });
            });
        };

        $scope.edit = function(tiedote) {
            TiedoteService.lisaaTiedote(tiedote, null, fetch, fetch);
        };
    })
    .service("TiedoteService", function($uibModal, TiedotteetCRUD, Notifikaatiot, Utils) {
        function doDelete(item, cb) {
            cb = cb || _.noop;
            TiedotteetCRUD.delete(
                {},
                item,
                function() {
                    Notifikaatiot.onnistui("poisto-onnistui");
                    cb();
                },
                Notifikaatiot.serverCb
            );
        }

        function doSave(item, cb) {
            cb = cb || _.noop;
            TiedotteetCRUD.save(
                {},
                Utils.presaveStrip(item),
                function() {
                    Notifikaatiot.onnistui("tallennus-onnistui");
                    cb();
                },
                Notifikaatiot.serverCb
            );
        }

        function lisaaTiedote(tiedote, perusteprojektiId, saveCb, deleteCb) {
            saveCb = saveCb || _.noop;
            deleteCb = deleteCb || _.noop;

            $uibModal
                .open({
                    template: require("views/modals/tiedotteenmuokkaus.html"),
                    controller: "TiedotteenMuokkausController",
                    size: "lg",
                    resolve: {
                        model: _.constant(tiedote),
                        perusteprojektiId: _.constant(perusteprojektiId)
                    }
                })
                .result.then(function(data) {
                    if (data.$dodelete) {
                        doDelete(data, deleteCb);
                    } else {
                        doSave(data, saveCb);
                    }
                });
        }

        return {
            delete: doDelete,
            lisaaTiedote: lisaaTiedote
        };
    })
    .controller("TiedotteenMuokkausController", function(
        $scope,
        model,
        perusteprojektiId,
        Varmistusdialogi,
        $uibModalInstance,
        $rootScope
    ) {
        $scope.model = _.cloneDeep(model);
        $scope.creating = !model;
        $scope.perusteprojektiId = perusteprojektiId;

        if ($scope.creating) {
            $scope.model = {
                otsikko: {},
                sisalto: {},
                id: null
            };
        }

        $scope.model.$liitaPerusteprojekti = true;

        $scope.ok = function() {
            $rootScope.$broadcast("notifyCKEditor");
            if ($scope.model.$liitaPerusteprojekti) {
                $scope.model._perusteprojekti = perusteprojektiId;
            }
            $uibModalInstance.close($scope.model);
        };

        $scope.delete = function() {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-tiedote"
            })(function() {
                $uibModalInstance.close(_.extend($scope.model, { $dodelete: true }));
            });
        };
    })
    .config(function($stateProvider) {
        $stateProvider.state("root.tiedote", {
            url: "/tiedote/:tiedoteId",
            template: require("views/tiedote.html"),
            controller: "TiedoteViewController"
        });
    })
    .controller("TiedoteViewController", function(
        $rootScope,
        $state,
        $scope,
        $stateParams,
        TiedotteetCRUD,
        Notifikaatiot,
        PerusteprojektiResource,
        PerusteProjektiService,
        Perusteet,
        YleinenData,
        perusteprojektiBackLink
    ) {
        if ($rootScope.lastState.state.name === "root.admin.tiedotteet") {
            $scope.$backurl = $state.href($rootScope.lastState.state.name, $rootScope.lastState.params);
            $scope.$backurlHeader = "takaisin-tiedotteiden-hallintaan";
        }

        $scope.tiedote = null;
        TiedotteetCRUD.get(
            { tiedoteId: $stateParams.tiedoteId },
            function(tiedote) {
                $scope.tiedote = tiedote;
                if (tiedote._perusteprojekti) {
                    PerusteprojektiResource.get(
                        {
                            id: tiedote._perusteprojekti
                        },
                        function(perusteprojekti) {
                            Perusteet.get(
                                {
                                    perusteId: perusteprojekti._peruste
                                },
                                function(peruste) {
                                    $scope.perusteprojekti = perusteprojekti;
                                    $scope.perusteprojekti.$url = $state.href(
                                        "root.perusteprojekti.suoritustapa." +
                                            PerusteProjektiService.getSisaltoTunniste(perusteprojekti),
                                        {
                                            perusteProjektiId: perusteprojekti.id,
                                            suoritustapa: YleinenData.valitseSuoritustapaKoulutustyypille(
                                                peruste.koulutustyyppi
                                            )
                                        }
                                    );
                                }
                            );
                        }
                    );
                }
            },
            Notifikaatiot.serverCb
        );
    });

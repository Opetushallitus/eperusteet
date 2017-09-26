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
    .directive("tutke2kentat", function() {
        return {
            restrict: "AE",
            templateUrl: "views/partials/muokkaus/tutke2kentat.html",
            scope: {
                mainLevelEditing: "=editEnabled",
                tutkinnonosaViite: "=",
                kontrollit: "=",
                yksikko: "=",
                isKoulutuksenOsa: "="
            },
            controller: "Tutke2KentatController"
        };
    })
    .service("Tutke2OsaData", function() {
        this.data = null;
        this.set = function(data) {
            this.data = data;
        };
        this.get = function() {
            return this.data;
        };
    })
    .controller("Tutke2KentatController", function(
        $scope,
        $state,
        Tutke2Osa,
        Tutke2OsaData,
        Varmistusdialogi,
        Utils,
        Lukitus,
        $q,
        YleinenData
    ) {
        var tutke2osaDefer = $q.defer();

        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

        $scope.viewOptions = {
            oneAtATime: false
        };

        $scope.originalViite = null;

        $scope.tutkinnonosaViite.then(function(res) {
            $scope.originalViite = res;
            $scope.tutke2osa = Tutke2Osa.init(res.tutkinnonOsa.id);

            $scope.tutke2osa.fetch().then(function() {
                tutke2osaDefer.resolve();
            });
            console.log("set Tutke2OsaData", $scope.tutke2osa);
            Tutke2OsaData.set($scope.tutke2osa);
            editModeCallback($scope.mainLevelEditing);
        });

        $scope.isEditingInProgress = function() {
            return $scope.osaAlue.$editing;
        };

        function stopEvent(event) {
            if (event) {
                event.stopPropagation();
            }
        }

        function verifyRemove(cb) {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko",
                primaryBtn: "poista",
                successCb: cb
            })();
        }

        $scope.osaAlue = {
            $editing: null,
            jumpTo: function(alue) {
                Utils.scrollTo("#" + alue.$uniqueId);
            },
            add: function() {
                var newAlue = {
                    nimi: {}
                };
                $scope.tutke2osa.$editing = $scope.tutke2osa.$editing || [];
                $scope.tutke2osa.$editing.push(newAlue);
            },
            edit: function(alue, $event) {
                $state.go(
                    "root.perusteprojekti.suoritustapa." +
                        ($scope.isKoulutuksenOsa ? "koulutuksenosa" : "tutkinnonosa") +
                        ".osaalue",
                    { osaAlueId: alue.id },
                    { reload: $scope.isKoulutuksenOsa }
                );
            },
            remove: function(alue) {
                if (alue.id) {
                    verifyRemove(function() {
                        _.remove($scope.tutke2osa.$editing, alue);
                    });
                } else {
                    _.remove($scope.tutke2osa.$editing, alue);
                }
            }
        };

        function editModeCallback(editing) {
            if (!$scope.tutke2osa) {
                return;
            }
            if (editing) {
                tutke2osaDefer.promise.then(function() {
                    $scope.tutke2osa.$editing = angular.copy($scope.tutke2osa.osaAlueet);
                });
            } else {
                $scope.tutke2osa.$editing = null;
            }
        }

        $scope.$watch("mainLevelEditing", editModeCallback);

        $scope.getTavoitteet = function(alue, pakollinen) {
            if (pakollinen) {
                return alue.$groups ? _.filter(alue.$groups.grouped[alue.$chosen], "pakollinen") : [];
            }
            var grouped = alue.$groups ? _.reject(alue.$groups.grouped[alue.$chosen], "pakollinen") : [];
            var ungrouped = alue.$groups ? alue.$groups.ungrouped : [];
            return grouped.concat(ungrouped);
        };
    })
    .factory("Tutke2Osa", function($q, TutkinnonOsanOsaAlue, $stateParams, VersionHelper, Kieli) {
        var unique = 0;
        function Tutke2OsaImpl(tutkinnonOsaId) {
            this.tutkinnonOsaId = tutkinnonOsaId;
            this.params = { osanId: tutkinnonOsaId };
            this.tavoiteMap = {};
            this.versiot = {};

            VersionHelper.getPerusteenosaVersions(this.versiot, { id: tutkinnonOsaId }, true, angular.noop);
        }

        Tutke2OsaImpl.prototype.fetch = function() {
            var that = this;
            var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, "") : null;
            var deferred = $q.defer();

            if (versio) {
                that.versiot = {};
                VersionHelper.getTutkinnonOsaViiteVersions(
                    that.versiot,
                    { id: $stateParams.tutkinnonOsaViiteId },
                    true,
                    function() {
                        var revNumber = VersionHelper.select(that.versiot, versio);
                        that.params.versioId = revNumber;
                        TutkinnonOsanOsaAlue.versioList(
                            that.params,
                            function(data) {
                                that.osaAlueet = data;
                                kasitteleOsaAlueet(that);
                                deferred.resolve();
                            },
                            function() {
                                deferred.reject();
                            }
                        );
                    }
                );
            } else {
                TutkinnonOsanOsaAlue.list(
                    this.params,
                    function(data) {
                        that.osaAlueet = data;
                        kasitteleOsaAlueet(that);
                        deferred.resolve();
                    },
                    function() {
                        deferred.reject();
                    }
                );
            }

            return deferred.promise;
        };

        function collectKielet(field, kielet) {
            _.each(field, function(value, key) {
                if (!_.isEmpty(value) && Kieli.isValidKielikoodi(key) && !_.contains(kielet, key)) {
                    kielet.push(key);
                }
            });
        }

        function kasitteleOsaAlueet(that) {
            _.each(that.osaAlueet, function(alue) {
                if (alue.nimi === null) {
                    alue.nimi = {};
                }
                // Tarkasta onko osa-alue vain yhdellä kielellä kirjoitettu
                alue.$kielet = [];
                collectKielet(alue.nimi, alue.$kielet);
                alue.$open = true;
                alue.$uniqueId = "osa-alue-" + unique++;
                kasitteleTavoitteet(alue.osaamistavoitteet, that, alue, alue);
            });
        }

        function kasitteleTavoitteet(tavoitteet, that, osaAlue, arr) {
            _.each(tavoitteet, function(tavoite) {
                fixTavoite(tavoite);
                if (that.tavoiteMap) {
                    that.tavoiteMap[tavoite.id] = tavoite;
                }
                tavoite.$kielet = [];
                collectKielet(tavoite.tavoitteet, tavoite.$kielet);
                collectKielet(tavoite.tunnustaminen, tavoite.$kielet);
            });
            arr.osaamistavoitteet = tavoitteet;
            osaAlue.$groups = groupTavoitteet(tavoitteet, that.tavoiteMap);
            var pakollinenIds = _.keys(osaAlue.$groups.grouped);
            osaAlue.$esitietoOptions = _.map(pakollinenIds, function(id) {
                return { value: id, label: osaAlue.$groups.grouped[id][0].nimi };
            });
            osaAlue.$esitietoOptions.unshift({ value: null, label: "<ei asetettu>" });
            osaAlue.$chosen = _.first(pakollinenIds);
        }

        function fixTavoite(tavoite) {
            _.each(["nimi", "tunnustaminen", "tavoitteet"], function(key) {
                if (_.isEmpty(tavoite[key])) {
                    tavoite[key] = {};
                }
            });

            if (_.isEmpty(tavoite.arviointi)) {
                tavoite.arviointi = {
                    lisatiedot: null,
                    arvioinninKohdealueet: []
                };
            }
        }

        function groupTavoitteet(tavoitteet, tavoiteMap) {
            var groups: any = {
                grouped: {},
                ungrouped: {}
            };
            var processed = [];
            _.each(tavoitteet, function(tavoite) {
                if (tavoite.pakollinen) {
                    groups.grouped[tavoite.id] = [tavoite];
                    processed.push(tavoite);
                }
            });
            _.each(tavoitteet, function(tavoite) {
                if (!tavoite.pakollinen && tavoite._esitieto && _.has(groups.grouped, tavoite._esitieto)) {
                    groups.grouped[tavoite._esitieto].push(tavoite);
                    processed.push(tavoite);
                }
            });
            groups.ungrouped = _.difference(tavoitteet, processed);
            groups.$size = _.size(groups.grouped);
            groups.$options = _.map(_.keys(groups.grouped), function(key) {
                return { label: tavoiteMap && tavoiteMap[key] ? tavoiteMap[key].nimi : "", value: key };
            });
            return groups;
        }

        return {
            init: function(tutkinnonOsaId) {
                return new Tutke2OsaImpl(tutkinnonOsaId);
            },
            fixTavoite: fixTavoite,
            kasitteleOsaAlueet: kasitteleOsaAlueet
        };
    });

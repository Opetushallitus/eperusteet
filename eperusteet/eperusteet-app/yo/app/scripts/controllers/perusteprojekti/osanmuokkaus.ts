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

import * as angular from "angular";
import _ from "lodash";

angular
    .module("eperusteApp")
    .service("OsanMuokkausHelper", ($q, $stateParams, PerusopetusService, LukiokoulutusService, $state, Lukitus) => {
        var vuosiluokat = [];
        var model = null;
        var isLocked = false;
        var backState = null;
        var vuosiluokka = null;
        var path = null;
        var oppiaine = null;
        var osaamiset = null;
        var service = PerusopetusService;
        var oppiaineLukitus = {
            lukitse: function(and) {
                Lukitus.lukitseOppiaine(oppiaine.id, function() {
                    isLocked = true;
                    if (and) {
                        and();
                    }
                });
            },
            vapauta: function(and?) {
                Lukitus.vapautaOppiaine(oppiaine.id, function() {
                    isLocked = false;
                    if (and) {
                        and();
                    }
                });
            }
        };

        function reset() {
            backState = null;
            vuosiluokka = null;
            osaamiset = null;
            path = null;
            oppiaine = null;
        }

        function getModel() {
            return path ? model[path] : model;
        }

        function setup(uusiModel, uusiPath, uusiOppiaine, cb) {
            cb = cb || angular.noop;
            oppiaine =
                $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ||
                $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT
                    ? uusiModel
                    : null;
            if (uusiOppiaine) {
                oppiaine = uusiOppiaine;
            }
            model = uusiModel;
            path = uusiPath;
            isLocked = false;
            backState = [$state.current.name, _.clone($stateParams)];

            var promises = [];
            var usesVuosiluokkas = false,
                usesOsaamiset = false;
            if ($stateParams.suoritustapa === "lukiokoulutus") {
                oppiaineLukitus = {
                    lukitse: function(and) {
                        Lukitus.lukitseLukioOppiaine(oppiaine.id, function() {
                            isLocked = true;
                            if (and) {
                                and();
                            }
                        });
                    },
                    vapauta: function(and) {
                        Lukitus.vapautaLukioOppiaine(oppiaine.id, function() {
                            isLocked = false;
                            if (and) {
                                and();
                            }
                        });
                    }
                };
                service = LukiokoulutusService;
            } else {
                usesVuosiluokkas = true;
                usesOsaamiset = true;
                promises = [
                    PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT, true),
                    PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true)
                ];
            }

            $q.all(promises).then(function(res) {
                if (usesVuosiluokkas) {
                    vuosiluokat = res[0];
                }
                if (usesOsaamiset) {
                    osaamiset = res[1];
                }
                if (usesVuosiluokkas) {
                    vuosiluokka = uusiModel._vuosiluokkaKokonaisuus
                        ? _.find(vuosiluokat, function(vl) {
                              return vl.id === parseInt(model._vuosiluokkaKokonaisuus, 10);
                          })
                        : null;
                }
                if (isVuosiluokkakokonaisuudenOsa()) {
                    Lukitus.lukitseOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function() {
                        isLocked = true;
                        cb();
                    });
                } else if (oppiaine) {
                    oppiaineLukitus.lukitse(cb);
                }
            });
        }

        function save() {
            if (isVuosiluokkakokonaisuudenOsa()) {
                PerusopetusService.saveVuosiluokkakokonaisuudenOsa(model, oppiaine, function() {
                    Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function() {
                        isLocked = false;
                        goBack();
                    });
                });
            } else if (path) {
                var payload: any = _.pick(model, ["id", path]);
                if ($stateParams.suoritustapa === "lukiokoulutus") {
                    payload.partial = true;
                }
                service.saveOsa(payload, backState[1], function() {
                    if (isLocked && oppiaine) {
                        oppiaineLukitus.vapauta(goBack);
                    }
                });
            }
        }

        function goBack() {
            if (!backState) {
                return;
            }

            if (isLocked) {
                if (isVuosiluokkakokonaisuudenOsa()) {
                    Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function() {
                        isLocked = false;
                    });
                } else if (oppiaine) {
                    oppiaineLukitus.vapauta();
                }
            }
            var params = _.clone(backState);
            reset();
            $state.go.apply($state, params, { reload: true });
        }

        function isVuosiluokkakokonaisuudenOsa() {
            return !!vuosiluokka;
        }

        function getOsaamiset() {
            return osaamiset;
        }

        function getOppiaine() {
            return oppiaine;
        }

        function getOppiaineenVuosiluokkakokonaisuus() {
            if (oppiaine && vuosiluokka) {
                return _.find(oppiaine.vuosiluokkakokonaisuudet, function(ovlk: any) {
                    return vuosiluokka.id === _.parseInt(ovlk._vuosiluokkaKokonaisuus);
                });
            }
        }

        function getVuosiluokkakokonaisuus() {
            return vuosiluokka;
        }

        return {
            reset: reset,
            getModel: getModel,
            setup: setup,
            save: save,
            goBack: goBack,
            getOppiaine: getOppiaine,
            getOsaamiset: getOsaamiset,
            getVuosiluokkakokonaisuus: getVuosiluokkakokonaisuus,
            getOppiaineenVuosiluokkakokonaisuus: getOppiaineenVuosiluokkakokonaisuus,
            isVuosiluokkakokonaisuudenOsa: isVuosiluokkakokonaisuudenOsa
        };
    })
    .controller("OsanMuokkausController", function(
        $scope,
        $stateParams,
        $compile,
        OsanMuokkausHelper,
        Editointikontrollit,
        $rootScope
    ) {
        $scope.objekti = OsanMuokkausHelper.getModel();

        if (!$scope.objekti) {
            return;
        }

        var MAPPING = {
            tekstikappale: {
                directive: "osanmuokkaus-tekstikappale",
                attrs: {
                    model: "objekti"
                },
                callbacks: {
                    save: function() {
                        $rootScope.$broadcast("notifyCKEditor");
                        OsanMuokkausHelper.save();
                    },
                    edit: function() {},
                    cancel: function() {
                        OsanMuokkausHelper.goBack();
                    }
                }
            },
            sisaltoalueet: {
                directive: "osanmuokkaus-sisaltoalueet",
                attrs: {
                    model: "objekti"
                },
                callbacks: {
                    save: function() {
                        $rootScope.$broadcast("notifyCKEditor");
                        OsanMuokkausHelper.save();
                    },
                    edit: function() {},
                    cancel: function() {
                        OsanMuokkausHelper.goBack();
                    }
                }
            },
            kohdealueet: {
                directive: "osanmuokkaus-kohdealueet",
                attrs: {
                    model: "objekti"
                },
                callbacks: {
                    save: function() {
                        OsanMuokkausHelper.save();
                    },
                    edit: function() {},
                    cancel: function() {
                        OsanMuokkausHelper.goBack();
                    }
                }
            },
            tavoitteet: {
                directive: "osanmuokkaus-tavoitteet",
                attrs: {
                    model: "objekti"
                },
                callbacks: {
                    save: function() {
                        OsanMuokkausHelper.save();
                    },
                    edit: function() {},
                    cancel: function() {
                        OsanMuokkausHelper.goBack();
                    }
                }
            }
        };

        var config = MAPPING[$stateParams.osanTyyppi];
        $scope.config = config;
        var muokkausDirective = angular.element("<" + config.directive + ">").attr("config", "config");
        _.each(config.attrs, function(value, key) {
            muokkausDirective.attr(key, value);
        });
        var el = $compile(muokkausDirective)($scope);

        angular.element("#muokkaus-elementti-placeholder").replaceWith(el);

        Editointikontrollit.registerCallback(config.callbacks);
        Editointikontrollit.startEditing();
        // TODO muokkauksesta poistumisvaroitus
    })
    .directive("osanmuokkausTekstikappale", function() {
        return {
            template: require("views/directives/perusopetus/osanmuokkaustekstikappale.html"),
            restrict: "E",
            scope: {
                model: "=",
                config: "=",
                editMode: "@"
            },
            controller: "OsanmuokkausTekstikappaleController",
            link: function(scope: any, element, attrs: any) {
                attrs.$observe("editMode", function(val) {
                    scope.editMode = val !== "false";
                });
                scope.editMode = attrs.editMode !== "false";
            }
        };
    })
    .controller("OsanmuokkausTekstikappaleController", function(
        $scope,
        OsanMuokkausHelper,
        $rootScope,
        YleinenData,
        $state,
        $stateParams
    ) {
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
        $scope.getTitle = function() {
            return OsanMuokkausHelper.isVuosiluokkakokonaisuudenOsa()
                ? "muokkaus-vuosiluokkakokonaisuuden-osa"
                : $scope.model.$isNew ? "luonti-tekstikappale" : "muokkaus-tekstikappale";
        };
        // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
        var received = 0;
        $scope.$on("ckEditorInstanceReady", function() {
            if (++received === 2) {
                $rootScope.$broadcast("editointikontrollitRefresh");
            }
        });

        $scope.edit = function() {
            OsanMuokkausHelper.setup($scope.model);
            $state.go("root.perusteprojekti.suoritustapa.muokkaus", $stateParams);
        };
    })
    .directive("osanmuokkausKohdealueet", function() {
        return {
            template: require("views/directives/perusopetus/osanmuokkauskohdealueet.html"),
            restrict: "E",
            scope: {
                model: "=",
                config: "="
            },
            controller: function($scope) {
                $scope.model = $scope.model || [];
                $scope.poistaKohdealue = _.partial(_.remove, $scope.model);
                $scope.lisaaKohdealue = function() {
                    $scope.model.push({ nimi: { fi: "" } });
                };
            }
        };
    })
    .directive("osanmuokkausTavoitteet", function() {
        return {
            template: require("views/directives/perusopetus/osanmuokkaustavoitteet.html"),
            restrict: "E",
            scope: {
                model: "=",
                config: "="
            },
            controller: function(
                $rootScope,
                $scope,
                $uibModal,
                ProxyService,
                $q,
                Notifikaatiot,
                OsanMuokkausHelper,
                $document
            ) {
                function uudetKohdealueetCb(kohdealueet) {
                    OsanMuokkausHelper.getOppiaine().kohdealueet = kohdealueet;
                    $rootScope.$broadcast("update:oppiaineenkohdealueet");
                }

                function clickHandler(event) {
                    var ohjeEl = angular.element(event.target).closest(".popover, .popover-element");
                    if (ohjeEl.length === 0) {
                        $rootScope.$broadcast("ohje:closeAll");
                    }
                }

                $document.on("click", clickHandler);
                $scope.$on("$destroy", function() {
                    $document.off("click", clickHandler);
                });

                $scope.muokkaaKohdealueita = function() {
                    $uibModal
                        .open({
                            template: require("views/directives/perusopetus/osanmuokkauskohdealueet.html"),
                            controller: function($scope, $uibModalInstance, Oppiaineet, OsanMuokkausHelper) {
                                $scope.kohdealueet = _.map(
                                    _.clone(OsanMuokkausHelper.getOppiaine().kohdealueet) || [],
                                    function(ka: any) {
                                        ka.$vanhaNimi = _.clone(ka.nimi);
                                        return ka;
                                    }
                                );
                                $scope.poistaKohdealue = function(ka) {
                                    Oppiaineet.poistaKohdealue(
                                        {
                                            perusteId: ProxyService.get("perusteId"),
                                            osanId: OsanMuokkausHelper.getOppiaine().id,
                                            kohdealueId: ka.id
                                        },
                                        function() {
                                            _.remove($scope.kohdealueet, ka);
                                        },
                                        Notifikaatiot.serverCb
                                    );
                                };
                                $scope.lisaaKohdealue = function() {
                                    Oppiaineet.lisaaKohdealue(
                                        {
                                            perusteId: ProxyService.get("perusteId"),
                                            osanId: OsanMuokkausHelper.getOppiaine().id
                                        },
                                        { nimi: { fi: "Uusi tavoitealue" } },
                                        function(res) {
                                            $scope.kohdealueet.push(res);
                                        }
                                    );
                                };
                                $scope.ok = function(kohdealueet) {
                                    $q
                                        .all(
                                            _(kohdealueet)
                                                .reject(function(ka) {
                                                    return _.isEqual(ka.nimi, ka.$vanhaNimi);
                                                })
                                                .map(function(ka) {
                                                    return Oppiaineet.lisaaKohdealue(
                                                        {
                                                            perusteId: ProxyService.get("perusteId"),
                                                            osanId: OsanMuokkausHelper.getOppiaine().id
                                                        },
                                                        ka
                                                    ).$promise;
                                                })
                                                .value()
                                        )
                                        .then(
                                            Oppiaineet.kohdealueet(
                                                {
                                                    perusteId: ProxyService.get("perusteId"),
                                                    osanId: OsanMuokkausHelper.getOppiaine().id
                                                },
                                                $uibModalInstance.close
                                            )
                                        );
                                };
                            }
                        })
                        .result.then(uudetKohdealueetCb);
                };
            }
        };
    })
    .directive("perusopetusMuokkausInfo", function(OsanMuokkausHelper) {
        return {
            template: require("views/directives/perusopetus/muokkausinfo.html"),
            restrict: "AE",
            link: function(scope: any, element, attrs: any) {
                scope.muokkausinfoOsa = attrs.osa || "";
            },
            controller: function($scope) {
                $scope.getOppiaine = function() {
                    var oppiaine = OsanMuokkausHelper.getOppiaine();
                    return oppiaine ? oppiaine.nimi : "";
                };
                $scope.getVuosiluokkakokonaisuus = function() {
                    var vlk = OsanMuokkausHelper.getVuosiluokkakokonaisuus();
                    return vlk ? vlk.nimi : "";
                };
            }
        };
    });

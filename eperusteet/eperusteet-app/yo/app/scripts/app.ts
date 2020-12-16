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
import moment from "moment";

if (process.env.NODE_ENV === "development") {
    console.log("Running in development mode");
}

angular
    .module("eperusteApp", [
        "ngSanitize",
        "ui.router",
        "ngResource",
        "restangular",
        "pascalprecht.translate",
        "ui.bootstrap",
        "angular-cache",
        "ui.sortable",
        "monospaced.elastic",
        "ui.tree",
        "ui.select",
        "ngFileUpload",
        "eGenericTree",
        "eMathDisplay",
        "LocalStorageModule",
        "angular-loading-bar"
    ])
    .constant("SERVICE_LOC", "/eperusteet-service/api")
    .constant("ORGANISATION_SERVICE_LOC", "/lokalisointi/cxf/rest/v1/localisation")
    .constant("LOKALISOINTI_SERVICE_LOC", "")
    .constant("AUTHENTICATION_SERVICE_LOC", "/authentication-service/resources")
    .constant("REQUEST_TIMEOUT", 10000)
    .constant("SPINNER_WAIT", 100)
    .constant("NOTIFICATION_DELAY_SUCCESS", 4000)
    .constant("NOTIFICATION_DELAY_WARNING", 10000)
    .constant("LUKITSIN_MINIMI", 5000)
    .constant("LUKITSIN_MAKSIMI", 20000)
    .constant("TEXT_HIERARCHY_MAX_DEPTH", 8)
    .constant("SHOW_VERSION_FOOTER", true)
    .constant("DEVELOPMENT", process.env.NODE_ENV === "development")
    .config(
        (
            $sceProvider,
            $urlRouterProvider,
            $translateProvider,
            $urlMatcherFactoryProvider,
            $locationProvider,
            cfpLoadingBarProvider
        ) => {
            const preferred = "fi";

            $sceProvider.enabled(true);

            $urlRouterProvider.when("", "/" + preferred);
            $urlRouterProvider.when("/", "/" + preferred);
            $urlRouterProvider.otherwise(($injector, $location) => {
                $injector.get("virheService").setData({ path: $location.path() });
                $injector.get("$state").go("root.virhe");
            });

            $urlMatcherFactoryProvider.caseInsensitive(true);
            $urlMatcherFactoryProvider.strictMode(false);

            $translateProvider.useLoader("LokalisointiLoader");
            $translateProvider.preferredLanguage(preferred);
            $translateProvider.useSanitizeValueStrategy("escaped");

            $locationProvider.hashPrefix("");

            cfpLoadingBarProvider.parentSelector = "#loading-bar-container";
            cfpLoadingBarProvider.includeSpinner = true;
            cfpLoadingBarProvider.latencyThreshold = 100;

            moment.locale(preferred);
        }
    )
    .config($qProvider => {
        $qProvider.errorOnUnhandledRejections(false);
    })
    .config($rootScopeProvider => {
        $rootScopeProvider.digestTtl(20);
    })
    .config($httpProvider => {
        $httpProvider.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";
        $httpProvider.defaults.headers.common["Caller-Id"] = "1.2.246.562.10.00000000001.eperusteet";
        $httpProvider.defaults.xsrfHeaderName = "CSRF";
        $httpProvider.defaults.xsrfCookieName = "CSRF";

        $httpProvider.interceptors.push([
            "UiKieli",
            kieli => {
                return {
                    request: config => {
                        if (kieli && kieli.kielikoodi) {
                            config.headers["Accept-Language"] = kieli.kielikoodi;
                        }
                        return config;
                    }
                };
            }
        ]);

        $httpProvider.interceptors.push([
            "$rootScope",
            "$q",
            "SpinnerService",
            ($rootScope, $q, Spinner) => {
                return {
                    request: request => {
                        Spinner.enable();
                        return request;
                    },
                    response: response => {
                        Spinner.disable();
                        return response || $q.when(response);
                    },
                    responseError: error => {
                        Spinner.disable();
                        return $q.reject(error);
                    }
                };
            }
        ]);
    })
    .config(($httpProvider: angular.IHttpProvider) => {
        $httpProvider.interceptors.push([
            "$rootScope",
            "$q",
            ($rootScope: angular.IScope, $q: angular.IQService) => {
                return {
                    response: response => {
                        const uudelleenohjausStatuskoodit = [401, 412, 500];
                        const fail = _.indexOf(uudelleenohjausStatuskoodit, response.status) !== -1;

                        if (fail) {
                            $rootScope.$emit("event:uudelleenohjattava", response.status);
                        }
                        return response || $q.when(response);
                    },
                    responseError: err => {
                        return $q.reject(err);
                    }
                };
            }
        ]);
    })
    .config(localStorageServiceProvider => {
        localStorageServiceProvider
            .setPrefix("eperusteApp")
            .setStorageType("localStorage")
            .setNotify(true, true);
    })
    .run(() => {
        _.mixin({
            arraySwap: function(array, a, b) {
                if (_.isArray(array) && _.size(array) > a && _.size(array) > b) {
                    const temp = array[a];
                    array[a] = array[b];
                    array[b] = temp;
                }
                return array;
            }
        });
        _.mixin({
            zipBy: function(array, kfield, vfield) {
                if (_.isArray(array) && kfield) {
                    if (vfield) {
                        return _.zipObject(_.map(array, kfield), _.map(array, vfield));
                    } else {
                        return _.zipObject(_.map(array, kfield), array);
                    }
                } else {
                    return {};
                }
            }
        });
        _.mixin({
            set: function(obj, field) {
                return function(value) {
                    obj[field] = value;
                };
            }
        });
        _.mixin({
            setWithCallback: function(obj, field, cb) {
                return function(value) {
                    cb = cb || angular.noop;
                    obj[field] = value;
                    cb(value);
                };
            }
        });
        _.mixin({
            flattenTree: function flattenTree(obj, extractChildren) {
                if (!_.isArray(obj) && obj) {
                    obj = [obj];
                }
                if (_.isEmpty(obj)) {
                    return [];
                }
                return _.union(
                    obj,
                    _(obj)
                        .map(function(o) {
                            return flattenTree(extractChildren(o), extractChildren);
                        })
                        .flatten()
                        .value()
                );
            }
        });
        _.mixin({
            reducedIndexOf: function(obj, extractor, combinator) {
                if (!_.isArray(obj) && obj) {
                    obj = [obj];
                }
                let results = {};
                _.each(obj, function(o) {
                    const index = extractor(o);
                    if (results[index]) {
                        results[index] = combinator(results[index], o);
                    } else {
                        results[index] = o;
                    }
                });
                return results;
            }
        });
    })
    .run(($rootScope: angular.IScope, $window: angular.IWindowService) => {
        const f = _.debounce(
            function() {
                $rootScope.$broadcast("poll:mousemove");
            },
            10000,
            {
                leading: true,
                maxWait: 60000
            }
        );

        angular.element($window).on("mousemove", f);
    })
    .run(
        (
            $rootScope,
            $uibModal,
            $location,
            $window,
            $state,
            $http,
            uibPaginationConfig,
            Editointikontrollit,
            Varmistusdialogi,
            Kaanna,
            virheService,
            $log
        ) => {
            uibPaginationConfig.firstText = "";
            uibPaginationConfig.previousText = "";
            uibPaginationConfig.nextText = "";
            uibPaginationConfig.lastText = "";
            uibPaginationConfig.maxSize = 5;
            uibPaginationConfig.rotate = false;

            let onAvattuna = false;

            $rootScope.$on("event:uudelleenohjattava", (event, status) => {
                if (onAvattuna) {
                    return;
                }
                onAvattuna = true;

                function getCasURL() {
                    const host = $location.host();
                    const port = $location.port();
                    const protocol = $location.protocol();
                    const cas = "/cas/login";
                    const redirectURL = encodeURIComponent($location.absUrl());
                    let url = protocol + "://" + host;

                    if (port !== 443 && port !== 80) {
                        url += ":" + port;
                    }

                    url += cas + "?service=" + redirectURL;
                    return url;
                }

                const casurl = getCasURL();

                if (status === 401) {
                    $window.location.href = casurl;
                    return;
                }

                const uudelleenohjausModaali = $uibModal.open({
                    template: require("views/modals/uudelleenohjaus.html"),
                    controller: "UudelleenohjausModalCtrl",
                    resolve: {
                        status: function() {
                            return status;
                        },
                        redirect: function() {
                            return casurl;
                        }
                    }
                });

                uudelleenohjausModaali.result
                    .then(angular.noop)
                    .catch(angular.noop)
                    .finally(function() {
                        onAvattuna = false;
                        switch (status) {
                            case 500:
                                $location.path("/");
                                break;
                            case 412:
                                $window.location.href = casurl;
                                break;
                        }
                    });
            });

            $rootScope.$on("$stateChangeStart", (event, toState, toParams, fromState, fromParams) => {
                $rootScope.lastState = {
                    state: _.clone(fromState),
                    params: _.clone(fromParams)
                };

                // Todo: Why exclude some states?
                if (
                    Editointikontrollit.getEditMode() &&
                    fromState.name !== "root.perusteprojekti.suoritustapa.tutkinnonosat" &&
                    fromState.name !== "root.perusteprojekti.suoritustapa.koulutuksenosa"
                ) {
                    event.preventDefault();

                    Varmistusdialogi.dialogi({
                        successCb: data => {
                            Editointikontrollit.unregisterCallback();
                            $state.go(data.toState, data.toParams);
                        },
                        data: {
                            toState: toState,
                            toParams: toParams
                        },
                        otsikko: "vahvista-liikkuminen",
                        teksti: "tallentamattomia-muutoksia",
                        lisaTeksti: "haluatko-jatkaa",
                        primaryBtn: "poistu-sivulta"
                    })();
                } else {
                    Editointikontrollit.unregisterCallback();
                }
            });

            $rootScope.$on("$stateChangeError", function(event, toState, toParams, fromState, fromParams, error) {
                $log.error(error);
                virheService.virhe({ state: toState.name });
            });

            $rootScope.$on("$stateNotFound", function(event, toState, toParams, fromState, fromParams, error) {
                $log.error(error);
                virheService.virhe({ state: toState.to });
            });

            // Jos käyttäjä editoi dokumenttia ja koittaa poistua palvelusta (reload, iltalehti...),
            // niin varoitetaan, että hän menettää muutoksensa jos jatkaa.
            $window.addEventListener("beforeunload", function(event) {
                if (Editointikontrollit.getEditMode()) {
                    const confirmationMessage = Kaanna.kaanna("tallentamattomia-muutoksia");
                    (event || window.event).returnValue = confirmationMessage;
                    return confirmationMessage;
                }
            });
        }
    )
    .run(($rootScope, DEVELOPMENT) => {
        if (DEVELOPMENT) {
            $rootScope.$on("$stateChangeSuccess", (event, state, params) => {
                console.info(
                    "%c» " + state.name + " «",
                    "color: #ffb05b; background: #333; font-weight: bold",
                    "" + state.url + " ",
                    params
                );
            });
            $rootScope.$on("$stateChangeStart", (event, state, params) => {
                console.info("Starting state change to", state);
            });
            $rootScope.$on("$stateChangeError", (event, a, b, c, d, e) => {
                console.info("Failed state change", e, a, b, c, d);
            });
        }
    });

import yleinenData from "./services/yleinenData";
import { taiteenalaCtrl } from "./controllers/perusteprojekti/taiteenala";
import { LangValidator  } from "./directives/langvalidator";
import { nakyvyyslinkki, nakyvyyslinkkiService } from "./directives/nakyvyyslinkki";

angular
    .module("eperusteApp")
    .service("YleinenData", yleinenData)
    .controller("taiteenalaCtrl", taiteenalaCtrl)
    .service("nakyvyyslinkkiService", nakyvyyslinkkiService)
    .directive("nakyvyyslinkki", nakyvyyslinkki)
    .directive("langValidator", LangValidator);

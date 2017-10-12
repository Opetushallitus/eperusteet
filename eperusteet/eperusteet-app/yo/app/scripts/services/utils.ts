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
import * as _ from "lodash";

angular
    .module("eperusteApp")
    .service("Utils", function($window, YleinenData, Kaanna) {
        this.scrollTo = function(selector, offset) {
            var element = angular.element(selector);
            if (element.length) {
                $window.scrollTo(0, element.eq(0).offset().top + (offset || 0));
            }
        };

        function presaveStrip(obj) {
            if (_.isArray(obj)) {
                _.each(obj, presaveStrip);
            } else if (_.isObject(obj)) {
                _.each(_.keys(obj), function(key) {
                    if (_.startsWith(key, "$")) {
                        delete obj[key];
                    }
                });
                _.each(obj, function(value) {
                    presaveStrip(value);
                });
            }
            return obj;
        }
        this.presaveStrip = presaveStrip;

        this.hasLocalizedText = function(field) {
            if (!_.isObject(field)) {
                return false;
            }
            var hasContent = false;
            var langs = _.values(YleinenData.kielet);
            _.each(langs, function(key: any) {
                if (!_.isEmpty(field[key])) {
                    hasContent = true;
                }
            });
            return hasContent;
        };

        this.supportsFileReader = function() {
            return !_.isUndefined($window.FormData);
        };

        this.oppiaineSort = function(oa) {
            return oa.jnro ? oa.jnro : Kaanna.kaanna(oa.nimi).toLowerCase();
        };

        this.nameSort = function(item, key) {
            return Kaanna.kaanna(_.isString(key) ? item[key] : item.nimi).toLowerCase();
        };
    })
    /* Easily clone/restore object with specific keys. */
    .service("CloneHelper", function() {
        function CloneHelperImpl(keys) {
            this.keys = keys;
            this.stash = {};
        }
        CloneHelperImpl.prototype.clone = function(source, destination) {
            var dest = destination || this.stash;
            var src = source || this.stash;
            _.each(this.keys, function(key) {
                dest[key] = _.cloneDeep(src[key]);
            });
        };
        CloneHelperImpl.prototype.restore = function(destination) {
            this.clone(null, destination);
            this.stash = {};
        };
        CloneHelperImpl.prototype.get = function() {
            return this.stash;
        };
        this.init = function(keys) {
            return new CloneHelperImpl(keys);
        };
    })
    /* Shows "back to top" link when scrolled beyond cutoff point */
    .directive("backtotop", function($window, $document, Utils) {
        var CUTOFF_PERCENTAGE = 33;

        return {
            restrict: "AE",
            scope: {},
            template:
                '<div id="backtotop" ng-hide="hidden" title="{{\'takaisin-ylos\' | kaanna}}">' +
                '<a class="action-link" icon-role="arrow-up" ng-click="backToTop()"></a></div>',
            link: function(scope: any) {
                var active = true;
                scope.backToTop = function() {
                    Utils.scrollTo("#ylasivuankkuri");
                };

                scope.hidden = true;
                var window = angular.element($window);
                var document = angular.element($document);
                var scroll = function() {
                    var fitsOnScreen = document.height() <= window.height() * 1.5;
                    var scrollDistance = document.height() - window.height();
                    var inTopArea = window.scrollTop() < scrollDistance * CUTOFF_PERCENTAGE / 100;
                    var hidden = !active || fitsOnScreen || inTopArea;
                    if (hidden !== scope.hidden) {
                        scope.$apply(function() {
                            scope.hidden = hidden;
                        });
                    }
                };
                window.on("scroll", scroll);
                // Disable when in edit mode
                scope.$on("enableEditing", function() {
                    active = false;
                });
                scope.$on("disableEditing", function() {
                    active = true;
                });
                scope.$on("$destroy", function() {
                    window.off("scroll", scroll);
                });
            }
        };
    });

namespace Lokalisointi {
    export interface Lokalisoitu {
        fi?: string;
        sv?: string;
        en?: string;
    }

    export interface TekstiOsa {
        otsikko: Lokalisoitu;
        teksti?: Lokalisoitu;
    }

    function joinString(a: Lokalisoitu, b: Lokalisoitu, key?: string): string {
        if (!a[key] && !b[key]) {
            return null;
        }
        return (a[key] || "") + (b[key] || "");
    }

    function join(a: Lokalisoitu, b: Lokalisoitu) {
        return <Lokalisoitu>{
            fi: joinString(a, b, "fi"),
            sv: joinString(a, b, "sv"),
            en: joinString(a, b, "en")
        };
    }

    function forAll(constant: string): Lokalisoitu {
        return {
            fi: constant,
            sv: constant,
            en: constant
        };
    }

    export function concat(...a: (Lokalisoitu | string)[]): Lokalisoitu {
        if (a.length == 0) {
            return forAll(null);
        }
        return _.reduce(
            a,
            (acc, next) => {
                return join(acc, typeof next === "string" ? forAll(next) : next);
            },
            forAll(null)
        );
    }
}

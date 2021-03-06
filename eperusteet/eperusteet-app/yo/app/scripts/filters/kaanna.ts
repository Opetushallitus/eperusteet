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

import _ from "lodash";
import * as angular from "angular";

angular
    .module("eperusteApp")
    .service("Kaanna", function($translate, Kieli, DEVELOPMENT) {
        function translate(obj, key) {
            function getTranslation(input, lang) {
                return input[lang] || input[lang.toUpperCase()] || input["kieli_" + lang + "#1"];
            }
            let primary = getTranslation(obj, key);
            if (primary) {
                return primary;
            }
            let secondaryKey = key === "fi" || key === "FI" ? "sv" : "fi";
            let secondary = getTranslation(obj, secondaryKey);
            if (secondary) {
                return "[" + secondary + "]";
            }
            return secondary;
        }

        function kaannaSisalto(input) {
            if (_.isEmpty(input)) {
                return "";
            }
            let sisaltokieli = Kieli.getSisaltokieli();
            return translate(input, sisaltokieli);
        }

        return {
            kaanna: function(input, config) {
                if (_.isObject(input)) {
                    return kaannaSisalto(input) || "";
                } else if (_.isString(input)) {
                    const result = $translate.instant(input, config);
                    if (DEVELOPMENT) {
                        if (!_.isEmpty(input) && _.eq(result, input)) {
                            console.info("Käännös puuttuu:", input);
                        }
                    }
                    return result;
                } else if (input === 0 || input) {
                    return "" + input;
                }
                return "";
            },
            kaannaSisalto: kaannaSisalto
        };
    })
    .directive("kaanna", function(Kaanna, $compile, IconMapping) {
        function resolvePostfix(attrs) {
            let postfix = attrs.kaannaPostfix || "";
            if (postfix) {
                postfix = " " + postfix;
            }
            if (!postfix && attrs.vaaditaan !== undefined) {
                postfix = "*";
            }
            return postfix;
        }
        function getAttr(attr, scope) {
            if (!_.isString(attr) || _.size(attr) === 0) {
                return;
            }
            return scope.$eval(attr) || attr;
        }
        return {
            restrict: "A",
            link: function(scope, el: any, attrs: any) {
                function kaannaValue(value) {
                    return _.isObject(value) ? Kaanna.kaannaSisalto(value) : Kaanna.kaanna(value);
                }
                const original = getAttr(attrs.kaanna, scope) || el.text();
                let postfix = resolvePostfix(attrs);
                if (_.isObject(original)) {
                    el.text(Kaanna.kaannaSisalto(original) + postfix);
                    if (attrs.iconRole) {
                        IconMapping.addIcon(attrs.iconRole, el);
                    }
                    scope.$watch(
                        function() {
                            return getAttr(attrs.kaanna, scope);
                        },
                        function(value) {
                            el.text(kaannaValue(value) + postfix);
                        }
                    );
                    scope.$on("changed:sisaltokieli", function() {
                        el.text(kaannaValue(getAttr(attrs.kaanna, scope)) + postfix);
                    });
                } else {
                    let textEl = angular.element("<span>").attr("translate", original);
                    if (attrs.kaannaValues) {
                        textEl.attr("translate-values", attrs.kaannaValues);
                    }
                    el
                        .html("")
                        .append(textEl)
                        .append(postfix);
                    if (attrs.iconRole) {
                        let iconEl = angular.element("<span>").attr("icon-role", attrs.iconRole);
                        el.removeAttr("icon-role");
                        el.prepend(iconEl);
                    }
                    el.removeAttr("kaanna");
                    el.removeAttr("kaanna-values");
                    $compile(el.contents())(scope);
                }
            }
        };
    })
    .filter("kaanna", function(Kaanna) {
        return (input, config) => {
            return Kaanna.kaanna(input, config);
        };
    });

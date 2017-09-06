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
    .service("treeTemplate", function() {
        function toimintoValikko(config) {
            return (
                '<span class="solmu-valikko" uib-dropdown><a class="action-link solmu-valikko-toggle" uib-dropdown-toggle><span class="solmu-caret"></span></a>' +
                '<ul class="dropdown-menu dropdown-menu-right" role="menu">' +
                '<li><a icon-role="edit" kaanna="\'muokkaa\'" ng-click="' +
                config.edit +
                '"></a></li>' +
                (config.copy
                    ? '<li><a icon-role="ep-group" kaanna="\'kopioi-leikelaudalle\'" ng-click="' +
                      config.copy +
                      '"></a></li>'
                    : "") +
                '<li><a icon-role="remove" kaanna="\'poista\'" ng-click="' +
                config.remove +
                '"></a></li>' +
                "</ul>" +
                "</span>"
            );
        }

        function generoiOtsikko() {
            var tosa =
                "{{ tutkinnonOsaSolmunNimi(rakenne) | kaanna }}" +
                '<span ng-if="!rakenne.erikoisuus && tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus">,' +
                "  <strong>{{ + tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus || 0 }}" +
                '    <span ng-if="tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuusMaksimi"> - {{ tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuusMaksimi }}</span>' +
                "  </strong>" +
                "{{ apumuuttujat.laajuusYksikko | kaanna }}" +
                "</span>";
            var editointiIkoni =
                '<div ng-click="togglaaPakollisuus(rakenne)" class="osa-ikoni">' +
                '  <span ng-if="!rakenne.pakollinen"><img src="images/tutkinnonosa.png" alt=""></span> ' +
                '  <span ng-if="rakenne.pakollinen"><img src="images/tutkinnonosa_pakollinen.png" alt=""></span> ' +
                "</div>";
            return (
                "" +
                '<span ng-if="onOsa(rakenne)">' +
                editointiIkoni +
                '  <a class="osa-nimi" ng-if="esitystilassa" ui-sref="root.esitys.peruste.tutkinnonosa({ id: rakenne._tutkinnonOsaViite, suoritustapa: apumuuttujat.suoritustapa })">' +
                tosa +
                "</a>" +
                '  <span class="osa-nimi" ng-if="!muokkaus && !esitystilassa">' +
                '    <a ng-if="rakenne._tutkinnonOsaViite && !isVaTe" ui-sref="root.perusteprojekti.suoritustapa.tutkinnonosa({ tutkinnonOsaViiteId: tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].id, suoritustapa: apumuuttujat.suoritustapa })">' +
                tosa +
                "</a>" +
                '    <a ng-if="rakenne._tutkinnonOsaViite && isVaTe" ui-sref="root.perusteprojekti.suoritustapa.koulutuksenosa({ tutkinnonOsaViiteId: tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].id, suoritustapa: apumuuttujat.suoritustapa })">' +
                tosa +
                "</a>" +
                '    <span ng-if="!rakenne._tutkinnonOsaViite">' +
                tosa +
                "</span>" +
                "  </span>" +
                '  <span class="solmu-osa-muokkaus osa-nimi" ng-if="muokkaus">' +
                tosa +
                "  </span>" +
                "</span>" +
                '<span ng-if="!onOsa(rakenne) && rakenne.nimi">' +
                '  <strong>{{ rakenne.nimi || "nimetön" | kaanna }}</strong>' +
                '  <span ng-if="rakenne.pakollinen">({{"pakollinen" | kaanna}})</span>' +
                "</span>"
            );
        }

        var varivalinta =
            "ng-class=\"{vieras: rakenne.rooli === 'vieras', maarittelematon: rakenne.rooli === 'määrittelemätön', tyhja: rakenne.osat.length === 0, " +
            "suljettu: rakenne.$collapsed, osaamisala: rakenne.rooli === 'osaamisala'}\"";

        var koonIlmaisu =
            '<span ng-if="rakenne.muodostumisSaanto.koko.minimi === rakenne.muodostumisSaanto.koko.maksimi">' +
            "  {{ rakenne.muodostumisSaanto.koko.minimi || 0 }} {{ 'kpl' | kaanna }}" +
            "</span>" +
            '<span ng-if="rakenne.muodostumisSaanto.koko.minimi !== rakenne.muodostumisSaanto.koko.maksimi">' +
            "  {{ rakenne.muodostumisSaanto.koko.minimi || 0 }} - {{ rakenne.muodostumisSaanto.koko.maksimi || 0 }} {{ 'kpl' | kaanna }}" +
            "</span>";

        var laajuudenIlmaisu =
            '<span ng-if="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi">' +
            "  {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }} {{ apumuuttujat.laajuusYksikko | kaanna }}" +
            "</span>" +
            '<span ng-if="rakenne.muodostumisSaanto.laajuus.minimi !== rakenne.muodostumisSaanto.laajuus.maksimi">' +
            "  {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }} - {{ rakenne.muodostumisSaanto.laajuus.maksimi || 0 }} {{ apumuuttujat.laajuusYksikko | kaanna }}" +
            "</span>";

        var avaaKaikki =
            "" +
            '<div class="pull-right">' +
            '  <a ng-if="muokkaus && rakenne.$virheetMaara > 0" style="margin-right: 10px;" ng-click="piilotaVirheet()" class="group-toggler action-link">' +
            '    <span ng-if="!apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "piilota-virheet" | kaanna }}</span>' +
            '    <span ng-if="apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "nayta-virheet" | kaanna }}</span>' +
            "  </a>" +
            '  <a ng-click="togglaaKuvaukset()" class="group-toggler action-link" ng-if="scanKuvaukset()">' +
            '    <span icon-role="ep-part">{{kuvauksetOpen && "piilota-kuvaukset" || "nayta-kuvaukset" | kaanna }}</span>' +
            "    " +
            "  </a>" +
            '  <a ng-click="togglaaPolut()" class="group-toggler action-link">' +
            '    <span class="avaa-sulje" icon-role="ep-open-close">{{ "avaa-sulje-rakenne" | kaanna }}</span>' +
            "  </a>" +
            "</div>";

        this.root = function() {
            return (
                "" +
                "<div>" +
                '  <div class="ylapainikkeet">' +
                '    <span class="rakenne-nimi">{{ apumuuttujat.peruste.nimi | kaanna }}' +
                '    <span ng-if="rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.laajuus">' +
                '      <span ng-if="rakenne.$laajuus">{{ rakenne.$laajuus }} / </span>' +
                '      <span ng-if="isNumber(rakenne.muodostumisSaanto.laajuus.minimi)">' +
                "        {{ rakenne.muodostumisSaanto.laajuus.minimi }}" +
                "      </span>" +
                '      <span ng-if="rakenne.muodostumisSaanto.laajuus.maksimi && rakenne.muodostumisSaanto.laajuus.minimi !== rakenne.muodostumisSaanto.laajuus.maksimi">' +
                "        - {{ rakenne.muodostumisSaanto.laajuus.maksimi }}" +
                "      </span>" +
                "      {{ apumuuttujat.laajuusYksikko | kaanna }}" +
                "    </span></span>" +
                '    <a class="action-link" ng-if="muokkaus" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)" kaanna>muokkaa-muodostumissääntöjä</button>' +
                '    <a ng-if="zoomaus" icon-role="back" class="back action-link"></a>' +
                avaaKaikki +
                "  </div>" +
                '  <div><div class="tree-yliviiva"></div></div>' +
                '  <div ng-if="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="isovirhe-otsikko">' +
                virheSnippet +
                "  </div>" +
                "</div>" +
                '<div ng-if="rakenne.rooli !== \'määrittelemätön\'" class="collapser" ng-show="!rakenne.$collapsed">' +
                '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
                '    <li ng-repeat="osa in rakenne.osat" class="tree-list-item">' +
                '      <tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osa-viitteet="tutkinnonOsaViitteet" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true" callbacks="callbacks"></tree>' +
                "    </li>" +
                '    <li class="ui-state-disabled tree-list-item" ng-if="muokkaus && rakenne.osat.length > 0">' +
                '      <span class="tree-anchor"></span>' +
                "    </li>" +
                "  </ul>" +
                "</div>"
            );
        };

        var optiot =
            "" +
            '<span ng-click="rakenne.$collapsed = rakenne.osat.length > 0 ? !rakenne.$collapsed : false" ng-if="!onOsa(rakenne)" class="colorbox" ' +
            varivalinta +
            ">" +
            "  <span ng-if=\"rakenne.rooli !== 'määrittelemätön'\">" +
            '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
            '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
            "  </span>" +
            "</span>" +
            '<div class="right">' +
            '  <div ng-if="!onOsa(rakenne) && muokkaus" class="right-item valikko">' +
            toimintoValikko({
                edit: "ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)",
                remove: "removeWithConfirmation(rakenne)",
                copy: "copyToSkratchpad(rakenne)"
            }) +
            "  </div>" +
            '  <div ng-if="onOsa(rakenne) && muokkaus" class="right-item valikko">' +
            toimintoValikko({ edit: "rakenneosaModaali(rakenne)", remove: "poista(rakenne, vanhempi)" }) +
            "  </div>" +
            '  <div class="pull-right" ng-if="!onOsa(rakenne)">' +
            '    <span class="right-item" ng-if="isNumber(rakenne.muodostumisSaanto.laajuus.minimi)">' +
            laajuudenIlmaisu +
            "    </span>" +
            '    <span class="right-item" ng-if="isNumber(rakenne.muodostumisSaanto.koko.minimi)">' +
            koonIlmaisu +
            "    </span>" +
            "  </div>" +
            "</div>" +
            '<div class="left">' +
            "  <span ng-class=\"{ 'pointer': muokkaus }\">" +
            generoiOtsikko() +
            "</span>" +
            "</div>";

        var virheSnippet =
            '  <span ng-if="rakenne.$virhe.virhe">' +
            "    <span>{{rakenne.$virhe.virhe|kaanna}}</span>. " +
            "  </span>" +
            '  <span ng-if="rakenne.$virhe.selite.kaannos" kaanna="{{rakenne.$virhe.selite.kaannos}}" kaanna-values="rakenne.$virhe.selite.muuttujat"></span>';

        var kentta =
            '<div ng-class="osaLuokat(rakenne)">' +
            optiot +
            "</div>" +
            '<div ng-if="rakenne.osaamisala || (rakenne.kuvaus && rakenne.kuvaus[lang].length > 0)" class="kuvaus">' +
            '  <div class="kuvausteksti" ng-class="{ \'text-truncated\': !rakenne.$showKuvaus }">' +
            '    <div class="osaamisala" ng-if="rakenne.osaamisala"><b kaanna="osaamisala"></b>: {{ rakenne.osaamisala.nimi | kaanna }} ({{ rakenne.osaamisala.osaamisalakoodiArvo }})</div>' +
            '    <p ng-if="rakenne.kuvaus && rakenne.kuvaus[lang].length > 0">{{ rakenne.kuvaus | kaanna }}</p>' +
            "  </div>" +
            '  <div class="avausnappi" ng-click="rakenne.$showKuvaus = !rakenne.$showKuvaus" ng-attr-title="{{rakenne.$showKuvaus && (\'piilota-ryhman-kuvaus\'|kaanna) || (\'nayta-ryhman-kuvaus\'|kaanna)}}">' +
            '  <div class="avausnappi-painike">&hellip;</div></div>' +
            "</div>" +
            '<div ng-if="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="virhe">' +
            virheSnippet +
            "</div>";

        this.leaf = function() {
            return "<div>" + kentta + "</div>";
        };

        this.leafChildren = function() {
            return (
                '<div ng-if="rakenne.rooli !== \'määrittelemätön\'" class="collapser" ng-show="!rakenne.$collapsed">' +
                '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" class="tree-group" ng-model="rakenne.osat" id="tree-sortable">' +
                '    <li ng-repeat="osa in rakenne.osat track by trackingFunction(osa, $index)" class="tree-list-item">' +
                '      <tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osa-viitteet="tutkinnonOsaViitteet" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true" callbacks="callbacks"></tree>' +
                "    </li>" +
                "  </ul>" +
                "</div>"
            );
        };
    })
    .directive("tree", function($compile, treeTemplate, $timeout, $animate) {
        return {
            restrict: "E",
            template: treeTemplate.leaf(),
            scope: {
                rakenne: "=",
                tutkinnonOsaViitteet: "=",
                uusiTutkinnonOsa: "=",
                vanhempi: "=",
                apumuuttujat: "=",
                muokkaus: "=",
                callbacks: "="
            },
            controller: "treeController",
            link: function(scope: any, el: any) {
                $animate.enabled(false, el);

                if (!scope.vanhempi) {
                    var templateElement = angular.element(treeTemplate.root());
                    $compile(templateElement)(scope);
                    el.replaceWith(templateElement);
                }

                function renderChildren() {
                    $compile(treeTemplate.leafChildren())(scope, function(cloned) {
                        el.append(cloned);
                    });
                }

                if (scope.muokkaus || (_.isArray(scope.rakenne.osat) && scope.rakenne.osat.length > 0)) {
                    // Give the browser a breather once in a while to retain responsiveness
                    if (_.random() > 0.8) {
                        $timeout(function() {
                            renderChildren();
                        });
                    } else {
                        renderChildren();
                    }
                }
            }
        };
    })
    .controller("treeController", function(
        $scope,
        $translate,
        $state,
        Muodostumissaannot,
        Algoritmit,
        Kaanna,
        TreeDragAndDrop,
        Utils,
        Varmistusdialogi,
        YleinenData
    ) {
        $scope.kuvauksetOpen = false;
        $scope.esitystilassa = $state.includes("**.esitys.**");
        $scope.lang = $translate.use() || $translate.preferredLanguage();
        $scope.isNumber = _.isNumber;

        $scope.trackingFunction = function(osa, index) {
            return osa.nimi && osa.nimi._id ? osa.nimi._id : index;
        };

        $scope.isVaTe = YleinenData.isValmaTelma($scope.apumuuttujat.peruste);

        $scope.$watch(
            "rakenne.$virhe",
            function() {
                if ($scope.rakenne.$virhe && $scope.rakenne.$virhe.selite && $scope.rakenne.$virhe.selite.muuttujat) {
                    $scope.lisaaLaajuusYksikko($scope.rakenne.$virhe.selite.muuttujat);
                }
            },
            true
        );

        $scope.lisaaLaajuusYksikko = function(obj) {
            return _.merge(obj, {
                laajuusYksikko: Kaanna.kaanna($scope.apumuuttujat.laajuusYksikko)
            });
        };

        $scope.poista = function(i, a) {
            _.remove(a.osat, i);
            $scope.callbacks.poistoTehty();
        };

        $scope.onOsa = function(osa) {
            return osa._tutkinnonOsaViite || osa.erikoisuus;
        };

        $scope.rakenneosaModaali = Muodostumissaannot.rakenneosaModaali(function(rakenneosa) {
            if (rakenneosa) {
                _.merge($scope.rakenne, rakenneosa);
            }
        });

        $scope.togglaaPakollisuus = function(rakenne) {
            if ($scope.muokkaus) {
                rakenne.pakollinen = !rakenne.pakollinen;
            }
        };

        $scope.tutkinnonOsaSolmunNimi = function(solmu) {
            if (solmu._tutkinnonOsaViite) {
                return $scope.tutkinnonOsaViitteet[solmu._tutkinnonOsaViite].nimi;
            } else if (solmu.erikoisuus) {
                return (solmu.vieras && solmu.vieras.nimi) || "nimeton-vierastutkinto";
            } else {
                return "nimetön";
            }
        };

        function doRemoveRyhma(ryhma) {
            _.remove($scope.vanhempi.osat, ryhma);
        }

        $scope.removeWithConfirmation = function(ryhma) {
            Varmistusdialogi.dialogi({
                otsikko: "poistetaanko-ryhma",
                successCb: function() {
                    doRemoveRyhma(ryhma);
                }
            })();
        };

        $scope.osaLuokat = function(osa) {
            var luokat = [];
            if ($scope.muokkaus) {
                luokat.push("pointer");
            }
            if ($scope.onOsa(osa)) {
                luokat.push("bubble-osa");
                var viite = $scope.tutkinnonOsaViitteet[osa._tutkinnonOsaViite];
                if (viite && (viite.$elevate || ($scope.apumuuttujat.haku && viite.$matched))) {
                    luokat.push("huomio");
                }
            } else {
                luokat.push("bubble");
            }
            return luokat;
        };

        $scope.copyToSkratchpad = function(ryhma) {
            $scope.callbacks.copyToSkratchpad(ryhma);
        };

        $scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
            if (!$scope.vanhempi) {
                $scope.rakenne = uusiryhma;
            } else {
                var indeksi = $scope.vanhempi.osat.indexOf(ryhma);
                if (!uusiryhma) {
                    doRemoveRyhma(ryhma);
                } else if (indeksi !== -1) {
                    $scope.vanhempi.osat[indeksi] = uusiryhma;
                }
            }
        }, $scope.apumuuttujat.peruste);

        $scope.scanKuvaukset = function() {
            var hasKuvaukset = false;
            $scope.kuvauksetOpen = false;
            Algoritmit.kaikilleLapsisolmuille($scope.rakenne, "osat", function(osa) {
                if (!$scope.kuvauksetOpen && osa.$showKuvaus) {
                    $scope.kuvauksetOpen = true;
                }
                if (!hasKuvaukset && Utils.hasLocalizedText(osa.kuvaus)) {
                    hasKuvaukset = true;
                }
            });
            return hasKuvaukset;
        };

        $scope.togglaaKuvaukset = function() {
            Algoritmit.kaikilleLapsisolmuille($scope.rakenne, "osat", function(osa) {
                osa.$showKuvaus = !$scope.kuvauksetOpen;
            });
            $scope.kuvauksetOpen = !$scope.kuvauksetOpen;
        };

        $scope.togglaaPolut = function() {
            var avaamattomat = _($scope.rakenne.osat)
                .reject(function(osa) {
                    return osa._tutkinnonOsaViite || osa.$collapsed || osa.osat.length === 0;
                })
                .size();

            _.forEach($scope.rakenne.osat, function(r) {
                if (r.osat && _.size(r.osat) > 0) {
                    r.$collapsed = avaamattomat !== 0;
                }
            });
        };

        // Drag & drop: puun sisällä
        $scope.sortableOptions = {
            connectWith: ".tree-group",
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            delay: 100,
            disabled: !$scope.muokkaus,
            placeholder: "placeholder tree-list-item",
            tolerance: "pointer",
            start: function(e, ui) {
                ui.placeholder.html('<div class="group-placeholder"></div>');
            },
            cancel: ".ui-state-disabled",
            update: TreeDragAndDrop.update
        };

        $scope.$watch("muokkaus", function() {
            $scope.sortableOptions.disabled = !$scope.muokkaus;
        });

        $scope.piilotaVirheet = function() {
            $scope.apumuuttujat.piilotaVirheet = !$scope.apumuuttujat.piilotaVirheet;
        };
    })
    .directive("treeWrapper", function() {
        return {
            restrict: "AE",
            transclude: true,
            terminal: true,
            templateUrl: "views/partials/tree.html",
            scope: {
                rakenne: "=",
                voiLiikuttaa: "=",
                ajaKaikille: "=",
                muokkaus: "=",
                esitys: "=?"
            },
            controller: "TreeWrapperController"
        };
    })
    .controller("TreeWrapperController", function(
        $scope,
        Kaanna,
        PerusteenRakenne,
        Muodostumissaannot,
        Algoritmit,
        TreeDragAndDrop,
        $filter,
        RyhmaCloner,
        Kielimapper,
        YleinenData
    ) {
        $scope.suljettuViimeksi = true;
        $scope.lisataanUuttaOsaa = false;
        $scope.uusiOsa = null;
        $scope.skratchpad = [];
        $scope.uniikit = [];
        $scope.kaytetytUniikit = {};
        $scope.kaikkiUniikit = [];
        $scope.topredicate = "nimi.fi";
        $scope.tosarajaus = "";
        $scope.naytaKuvaus = function() {
            return !!Kaanna.kaanna($scope.rakenne.rakenne.kuvaus);
        };
        $scope.isVaTe = YleinenData.isValmaTelma($scope.rakenne.$peruste);
        $scope.vateConverter = Kielimapper.mapTutkinnonosatKoulutuksenosat($scope.isVaTe);

        $scope.tutkinnonOsat = {
            perSivu: 8,
            rajaus: "",
            multiPage: false,
            sivu: 1
        };

        $scope.paivitaTekstiRajaus = function(value) {
            if (!_.isEmpty(value)) {
                PerusteenRakenne.kaikilleRakenteille($scope.rakenne.rakenne, function(item) {
                    // 1. Find matches
                    item.$collapsed = true;
                    var osa = $scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
                    if (osa) {
                        osa.$matched = Algoritmit.rajausVertailu(value, osa, "nimi");
                    }
                });
                PerusteenRakenne.kaikilleRakenteille($scope.rakenne.rakenne, function(item) {
                    // 2. Uncollapse parents of matched
                    var osa = $scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
                    if (osa && osa.$matched) {
                        var parent = item.$parent;
                        while (parent) {
                            if (parent.$parent) {
                                parent.$collapsed = false;
                            }
                            parent = parent.$parent;
                        }
                    }
                });
            } else {
                // Uncollapse all when search is cleared
                PerusteenRakenne.kaikilleRakenteille($scope.rakenne.rakenne, function(item) {
                    item.$collapsed = false;
                });
            }
        };

        $scope.paivitaRajaus = function(input) {
            input = input === undefined ? $scope.tosarajaus : input;
            $scope.tosarajaus = input;
            var filtered = !_.isEmpty(input);
            $scope.uniikit = _.reject($scope.kaikkiUniikit, function(yksi) {
                var nimi = $scope.rakenne.tutkinnonOsaViitteet[yksi._tutkinnonOsaViite]
                    ? (Kaanna.kaanna($scope.rakenne.tutkinnonOsaViitteet[yksi._tutkinnonOsaViite].nimi) || "")
                          .toLowerCase()
                    : "";
                return (
                    !yksi.alwaysVisible &&
                    ((filtered && nimi.indexOf(input.toLowerCase()) === -1) ||
                        ($scope.piilotaKaikki && $scope.kaytetytUniikit[yksi._tutkinnonOsaViite]))
                );
            });
        };

        $scope.toggleNotUsed = function() {
            $scope.piilotaKaikki = !$scope.piilotaKaikki;
            $scope.paivitaRajaus();
        };

        $scope.jarjestysSorter = function(item) {
            if (item.erikoisuus === "vieras") {
                return -1;
            }
            if (item._tutkinnonOsaViite) {
                var osa = $scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
                if (osa && _.isNumber(osa.jarjestys)) {
                    return osa.jarjestys;
                }
            }
            return Number.MAX_VALUE;
        };

        $scope.nimiSorter = function(item) {
            if (item._tutkinnonOsaViite) {
                var osa = $scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
                return Kaanna.kaanna(osa.nimi).toLowerCase();
            }
        };

        $scope.ryhmaSorter = function(item) {
            if (!item._tutkinnonOsaViite) {
                return Kaanna.kaanna(item.nimi).toLowerCase();
            }
        };

        function paivitaUniikit() {
            var uudetUniikit = [];
            _($scope.rakenne.tutkinnonOsaViitteet)
                .each(function(osa) {
                    var match =
                        $scope.tutkinnonOsat.rajaus &&
                        _.contains(Kaanna.kaanna(osa.nimi).toLowerCase(), $scope.tutkinnonOsat.rajaus.toLowerCase());
                    if (!$scope.tutkinnonOsat.rajaus || match) {
                        uudetUniikit.push({ _tutkinnonOsaViite: osa.id });
                    }
                })
                .value();
            $scope.tutkinnonOsat.multiPage = _.size(uudetUniikit) > $scope.tutkinnonOsat.perSivu;
            $scope.kaikkiUniikit = uudetUniikit;
            // Näytä aina vieras tutkinnon osa listassa
            $scope.kaikkiUniikit.unshift({
                erikoisuus: "vieras",
                alwaysVisible: true
            });
            $scope.uniikit = $scope.kaikkiUniikit;
            $scope.paivitaRajaus();
            $scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy($scope.rakenne.rakenne);
        }
        paivitaUniikit();

        function adjustIndex(arr, sorters, originalIndex, paginationOffset = 0) {
            var sortedList = $filter("orderBy")(arr, sorters);
            var newIndex = _.findIndex(arr, function(item) {
                return item === sortedList[originalIndex + paginationOffset];
            });
            return newIndex;
        }

        // Drag & drop: Leikelauta <-> puu
        $scope.sortableOptions = {
            connectWith: ".tree-group",
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            delay: 100,
            disabled: !$scope.muokkaus,
            placeholder: "placeholder tree-list-item",
            tolerance: "pointer",
            stop: function() {
                PerusteenRakenne.kaikilleRakenteille($scope.rakenne.rakenne, function(r) {
                    delete r.$uusi;
                });
            },
            start: function(e, ui) {
                ui.placeholder.html('<div class="group-placeholder"></div>');
                // Use same sorting as in ng-repeat template
                var sortedIndex = adjustIndex(
                    $scope.skratchpad,
                    [$scope.ryhmaSorter, $scope.jarjestysSorter, $scope.nimiSorter],
                    ui.item.sortable.index
                );
                ui.item.sortable.index = sortedIndex;
            },
            update: TreeDragAndDrop.update
        };

        // Drag & drop: Tutkinnon osat <-> puu
        $scope.sortableOptionsUnique = {
            connectWith: ".tree-group",
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            delay: 100,
            disabled: !$scope.muokkaus,
            placeholder: "placeholder tree-list-item",
            tolerance: "pointer",
            stop: paivitaUniikit,
            update: TreeDragAndDrop.update,
            start: function(e, ui) {
                ui.placeholder.html('<div class="group-placeholder"></div>');
                // Use same sorting as in ng-repeat template
                var sortedIndex = adjustIndex(
                    $scope.uniikit,
                    [$scope.jarjestysSorter, $scope.nimiSorter],
                    ui.item.sortable.index,
                    ($scope.tutkinnonOsat.sivu - 1) * $scope.tutkinnonOsat.perSivu
                );
                ui.item.sortable.index = sortedIndex;
            }
        };

        $scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
            if (uusiryhma) {
                if (ryhma === undefined) {
                    uusiryhma.$uusi = true;
                    $scope.skratchpad.push(uusiryhma);
                } else {
                    ryhma = _.merge(ryhma, uusiryhma);
                }
            } else {
                _.remove($scope.skratchpad, ryhma);
            }
        }, $scope.rakenne.$peruste);

        $scope.$watch("skratchpad.length", function(value) {
            Muodostumissaannot.skratchpadNotEmpty(value > 0);
        });

        $scope.poista = function(i, a) {
            _.remove(i, a);
            $scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy($scope.rakenne.rakenne);
        };

        $scope.callbacks = {
            poistoTehty: function() {
                $scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy($scope.rakenne.rakenne);
            },
            copyToSkratchpad: function(ryhma) {
                var newRyhma = RyhmaCloner.clone(ryhma);
                $scope.skratchpad.push(newRyhma);
            }
        };

        $scope.uusiTutkinnonOsa = function(cb) {
            $scope.lisataanUuttaOsaa = true;
            cb();
        };

        $scope.$watch("rakenne.$suoritustapa", function() {
            var sts = null;
            if ($scope.rakenne.$peruste) {
                sts = _($scope.rakenne.$peruste.suoritustavat)
                    .filter(function(st) {
                        return st.laajuusYksikko;
                    })
                    .value();
                sts = _.zipObject(_.map(sts, "suoritustapakoodi"), sts)[$scope.rakenne.$suoritustapa];
            }

            $scope.apumuuttujat = {
                suoritustapa: $scope.rakenne.$suoritustapa,
                laajuusYksikko: sts ? sts.laajuusYksikko : null,
                vanhin: $scope.rakenne,
                piilotaVirheet: true,
                peruste: $scope.rakenne.$peruste
            };
        });

        $scope.$watch("muokkaus", function() {
            $scope.sortableOptions.disabled = !$scope.muokkaus;
            $scope.sortableOptionsUnique.disabled = !$scope.muokkaus;
            if (!$scope.muokkaus) {
                $scope.skratchpad = [];
            }
        });

        $scope.$watch("apumuuttujat.haku", function(value) {
            $scope.paivitaTekstiRajaus(value);
        });
    })
    .config(function($uibTooltipProvider) {
        $uibTooltipProvider.setTriggers({
            mouseenter: "mouseleave",
            click: "click",
            focus: "blur",
            never: "mouseleave",
            show: "hide"
        });
    })
    .service("TreeDragAndDrop", function($timeout) {
        var NODESELECTOR = ".tree-list-item";
        /*
     * Puun säännöt
     *
     * 1. Osaamisalaryhmää ei voida asettaa puuhun jos mikä tahansa lisäämiskohdan edeltäjä on aito osaamisalaryhmä
     *   - Aito osaamisalaryhmä: ryhmä, joka on itse tyyppiä osaamisala
     *   - Osaamisalaryhmä: aito osaamisalaryhmä tai ryhmä, jonka mikä tahansa jälkeläinen on aito osaamisalaryhmä.
     * 2. Vieras tutkinnon osa voi olla vain vieras-ryhmässä
     * 3. Vieras-ryhmän alla ei voi olla muita ryhmiä
     */
        function hasOsaamisala(item) {
            return !_.isEmpty(item.osaamisala);
        }

        function isOsaamisalaRyhma(item) {
            if (hasOsaamisala(item)) {
                return true;
            }
            return _.any(item.osat, function(child) {
                return isOsaamisalaRyhma(child);
            });
        }

        function parentsOrSelfHaveOsaamisala(node, item) {
            if (!item || !item.osa) {
                return false;
            }
            if (hasOsaamisala(item.osa)) {
                return true;
            }
            var parent = node.parent().closest(NODESELECTOR);
            return parentsOrSelfHaveOsaamisala(parent, parent ? parent.scope() : null);
        }

        function showErrorBubble(virheElId, listItem, ui) {
            var parent = listItem.find(".bubble").first();
            var pos = parent.offset();
            if (!pos) {
                // dropped to root, no parent bubble
                pos = ui.item.offset();
            }
            var el = angular.element("#" + virheElId);
            el.offset({ top: pos.top, left: pos.left + 200 });
            el.trigger("show");
            $timeout(function() {
                el.trigger("hide");
            }, 5000);
            ui.item.sortable.cancel();
        }

        this.update = function(e, ui) {
            var itemScope = ui.item.scope();
            var ominaisuudet: any = {};

            if (itemScope && itemScope.osa) {
                ominaisuudet.draggedHasOsaamisala = isOsaamisalaRyhma(itemScope.osa);
                ominaisuudet.draggedIsVierastutkinto = itemScope.osa.erikoisuus === "vieras";
                ominaisuudet.draggedIsRyhma = _.has(itemScope.osa, "rooli");
            }

            var target = ui.item.sortable.droptarget;
            var listItem = target.closest(NODESELECTOR);
            var parentScope = listItem ? listItem.scope() : null;
            ominaisuudet.parentIsNotVierasRyhma =
                !parentScope || !parentScope.osa.rooli || parentScope.osa.rooli !== "vieras";

            if (ominaisuudet.draggedHasOsaamisala && parentsOrSelfHaveOsaamisala(listItem, parentScope)) {
                // 1.
                showErrorBubble("osaamisala-varoitus", listItem, ui);
            } else if (ominaisuudet.draggedIsVierastutkinto && ominaisuudet.parentIsNotVierasRyhma) {
                // 2.
                showErrorBubble("vierastutkinto-varoitus", listItem, ui);
            } else if (!ominaisuudet.parentIsNotVierasRyhma && ominaisuudet.draggedIsRyhma) {
                // 3.
                showErrorBubble("vierastutkinto-varoitus2", listItem, ui);
            }
        };
    })
    .service("RyhmaCloner", function() {
        function traverse(node) {
            if (_.isObject(node)) {
                _.each(_.keys(node), function(key) {
                    if (key.substring(0, 1) === "$" || key === "_id" || key === "id") {
                        delete node[key];
                    }
                });
            }
            if (_.isObject(node) || _.isArray(node)) {
                _.each(node, traverse);
            }
        }

        this.clone = function(ryhma) {
            var clone = angular.copy(ryhma);
            traverse(clone);
            return clone;
        };
    });

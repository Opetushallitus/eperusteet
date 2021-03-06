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
import { Lokalisointi } from "scripts/services/utils";

angular
    .module("eperusteApp")
    .service("PerusteProjektiSivunavi", function(
        $location,
        $q,
        $state,
        $stateParams,
        $timeout,
        AIPEService,
        Kaanna,
        Kielimapper,
        LukioKurssiService,
        LukiokoulutusService,
        PerusopetusService,
        PerusteprojektiTiedotService,
        Utils,
        YleinenData
    ) {
        var STATE_OSAT_ALKU = "root.perusteprojekti.suoritustapa.";
        var STATE_OSAT = "root.perusteprojekti.suoritustapa.tutkinnonosat";
        var STATE_TUTKINNON_OSA = "root.perusteprojekti.suoritustapa.tutkinnonosa";
        var STATE_TEKSTIKAPPALE = "root.perusteprojekti.suoritustapa.tekstikappale";
        var STATE_TAITEENALA = "root.perusteprojekti.suoritustapa.taiteenala";
        var STATE_OSALISTAUS = "root.perusteprojekti.suoritustapa.osalistaus";
        var STATE_AIPE_OSALISTAUS = "root.perusteprojekti.suoritustapa.aipeosalistaus";
        var STATE_LUKIOOSALISTAUS = "root.perusteprojekti.suoritustapa.lukioosat";
        var STATE_OSAALUE = "root.perusteprojekti.suoritustapa.osaalue";
        var STATE_AIPE_OSAALUE = "root.perusteprojekti.suoritustapa.aipeosaalue";

        var STATE_LUKIOOSAALUE = "root.perusteprojekti.suoritustapa.lukioosaalue",
            STATE_LUKIOKURSSI = "root.perusteprojekti.suoritustapa.kurssi";
        var isTutkinnonosatActive = function() {
            return $state.is(STATE_OSAT) || $state.is(STATE_TUTKINNON_OSA);
        };

        // const AM_ITEMS = [
        //     {
        //         label: "tutkinnonosat",
        //         link: [STATE_OSAT, {}],
        //         isActive: isTutkinnonosatActive,
        //         $type: "ep-parts"
        //     }
        // ];

        function getAMItems(isVaTe, vateConverter) {
            return [
                {
                    label: vateConverter("tutkinnonosat"),
                    link: [STATE_OSAT_ALKU + (isVaTe ? "koulutuksenosat" : "tutkinnonosat"), {}],
                    isActive: isTutkinnonosatActive,
                    $type: "ep-parts"
                }
            ];
        }

        var service = null;
        var _isVisible = false;
        var items = [];
        var nameMap = {};
        var perusteenTyyppi = "AM";
        var data = {
            projekti: {
                peruste: {
                    sisalto: {}
                }
            }
        };
        var callbacks = {
            itemsChanged: angular.noop,
            typeChanged: angular.noop
        };
        var kurssit = null;
        var specialPerusteenOsaParts = {};

        function getLink(lapsi): any {
            if (!lapsi.perusteenOsa) {
                return "";
            }
            var params = {
                perusteenOsaViiteId: lapsi.id,
                versio: null
            };

            if (lapsi.perusteenOsa.tunniste === "rakenne") {
                return ["root.perusteprojekti.suoritustapa.muodostumissaannot", { versio: "" }];
            } else if (lapsi.perusteenOsa.tunniste === "laajaalainenosaaminen" && perusteenTyyppi === "AIPE") {
                return [
                    STATE_AIPE_OSALISTAUS,
                    {
                        suoritustapa: "aipe",
                        osanTyyppi: "osaaminen"
                    }
                ];
            } else if (lapsi.perusteenOsa.tunniste === "laajaalainenosaaminen") {
                return [
                    STATE_OSALISTAUS,
                    {
                        suoritustapa: "perusopetus",
                        osanTyyppi: "osaaminen"
                    }
                ];
            } else {
                switch (lapsi.perusteenOsa.osanTyyppi) {
                    case "taiteenala":
                        return [STATE_TAITEENALA, params];
                    default:
                        return [STATE_TEKSTIKAPPALE, params];
                }
            }
        }

        var processNode = function(node, level = 0, parent?) {
            _.each(node.lapset, function(lapsi) {
                let label;
                if (lapsi.perusteenOsa) {
                    label = lapsi.perusteenOsa.nimi;
                }
                if (!label || label === "") {
                    label = "nimeton";
                }

                var link = null,
                    special = null,
                    isActive = null;

                if (
                    lapsi.perusteenOsa &&
                    lapsi.perusteenOsa.osanTyyppi &&
                    specialPerusteenOsaParts[lapsi.perusteenOsa.osanTyyppi]
                ) {
                    special = specialPerusteenOsaParts[lapsi.perusteenOsa.osanTyyppi];
                    link = special.link;
                    isActive = special.isActive;
                }

                items.push({
                    label: label,
                    id: lapsi.id,
                    depth: level,
                    link: link || getLink(lapsi),
                    isActive: isActive || isRouteActive,
                    valmis: lapsi && lapsi.perusteenOsa && lapsi.perusteenOsa.valmis,
                    kaannettava: lapsi && lapsi.perusteenOsa && lapsi.perusteenOsa.kaannettava,
                    $$parentItem: parent,
                    $type: lapsi.perusteenOsa && lapsi.perusteenOsa.tunniste === "rakenne" ? "ep-tree" : "ep-text"
                });
                nameMap[lapsi.id] = label;
                if (special) {
                    _.each(special.lapset, function(mappedChild) {
                        mappedChild.depth += level;
                        items.push(mappedChild);
                        nameMap[mappedChild.id] = mappedChild.perusteenOsa ? mappedChild.perusteenOsa.nimi : "";
                    });
                }
                processNode(lapsi, level + 1, node);
            });
        };

        var isRouteActive = function(item) {
            // ui-sref-active doesn't work directly in ui-router 0.2.*
            // with optional parameters.
            // Versionless url should be considered same as specific version url.
            var url =
                item.href && item.href.indexOf("/rakenne") > -1
                    ? item.href.substr(1)
                    : $state
                          .href(
                              STATE_TEKSTIKAPPALE,
                              {
                                  perusteenOsaViiteId: item.id,
                                  versio: null
                              },
                              { inherit: true }
                          )
                          .replace(/#/g, "");
            return $location.url().indexOf(url) > -1;
        };

        var normalize = function(str) {
            if (!str) {
                return str;
            }
            return str.replace(/\/0\//g, "//");
        };

        var isYlRouteActive = function(item) {
            // ignore tabId
            var tablessUrl = $state
                .href(item.link[0], _.extend(_.clone(item.link[1]), { tabId: "" }))
                .replace(/#/g, "");
            return normalize($location.url()).indexOf(normalize(tablessUrl)) > -1;
        };

        function ylMapper(targetItems, osa, key, level, link?, parent?) {
            level = level || 0;
            let nimi = _.has(osa, "nimi") ? osa.nimi : osa.perusteenOsa.nimi;
            if (
                perusteenTyyppi === "LU" &&
                key === "oppiaineet_oppimaarat" &&
                (osa.lokalisoitukoodi || osa.koodiArvo)
            ) {
                // Oppiaineelle ei varmaan tunnusta haluttu?
                if (link && link[0] == STATE_LUKIOKURSSI) {
                    nimi = Lokalisointi.concat(nimi, " (", osa.lokalisoitukoodi || osa.koodiArvo, ")");
                }
            }
            targetItems.push({
                depth: level,
                label: nimi,
                link: link || [
                    perusteenTyyppi == "LU"
                        ? STATE_LUKIOOSAALUE
                        : perusteenTyyppi == "AIPE" ? STATE_AIPE_OSAALUE : STATE_OSAALUE,
                    {
                        osanTyyppi: key,
                        osanId: osa.id,
                        tabId: 0
                    }
                ],
                isActive: isYlRouteActive,
                $$parentItem: parent
            });
            _(osa.oppimaarat)
                .sortBy("jnro")
                .each(function(lapsi) {
                    ylMapper(targetItems, lapsi, key, level + 1, null, osa);
                })
                .value();
            if (kurssit && perusteenTyyppi === "LU" && key === "oppiaineet_oppimaarat") {
                var foundKurssit = LukioKurssiService.filterOrderedKurssisByOppiaine(kurssit, function(oa) {
                    return oa.oppiaineId === osa.id;
                });
                _.each(foundKurssit, function(filteredKurssi) {
                    ylMapper(
                        targetItems,
                        filteredKurssi,
                        key,
                        level + 1,
                        [STATE_LUKIOKURSSI, { kurssiId: filteredKurssi.id }],
                        osa
                    );
                });
            }
        }

        function mapYL(target, osat, key, parent, doSort = true) {
            let chain: any = _.chain(osat);
            if (doSort) {
                chain = chain.sortBy(
                    key === "oppiaineet" ||
                    key === "oppiaineet-oppimaarat" ||
                    key === "oppiaineet_oppimaarat" ||
                    key === "aihekokonaisuudet"
                        ? "jnro"
                        : Utils.nameSort
                );
            }
            chain
                .each(function(osa) {
                    ylMapper(target, osa, key, 1, null, parent);
                })
                .value();
        }

        function lukioOsanTyyppi(key) {
            switch (key) {
                case LukiokoulutusService.OPPIAINEET_OPPIMAARAT:
                    return "lukioopetussuunnitelmarakenne";
                case LukiokoulutusService.AIHEKOKONAISUUDET:
                    return "aihekokonaisuudet";
                case LukiokoulutusService.OPETUKSEN_YLEISET_TAVOITTEET:
                    return "opetuksenyleisettavoitteet";
                default:
                    return null;
            }
        }

        async function buildTree(isVaTe, vateConverter) {
            items = [];
            switch (perusteenTyyppi) {
                case "YL": {
                    var tiedot1 = service.getYlTiedot();
                    _.each(PerusopetusService.LABELS, function(key, label) {
                        var item = {
                            label: label,
                            link: [STATE_OSALISTAUS, { suoritustapa: "perusopetus", osanTyyppi: key }]
                        };
                        items.push(item);
                        mapYL(items, tiedot1[key], key, item);
                    });
                    break;
                }
                case "AIPE": {
                    var aipeTiedot = service.getYlTiedot();
                    _.each(AIPEService.LABELS, (key, label) => {
                        let item = {
                            label: label,
                            link: [STATE_AIPE_OSALISTAUS, { suoritustapa: "aipe", osanTyyppi: key }]
                        };
                        items.push(item);
                        mapYL(items, aipeTiedot[key], key, item, false);
                    });
                    break;
                }
                case "LU": {
                    var lukioTiedot = service.getYlTiedot();
                    kurssit = lukioTiedot.kurssit;
                    _.each(LukiokoulutusService.LABELS, function(k) {
                        var itemList = [],
                            key = lukioOsanTyyppi(k),
                            item = {
                                lapset: itemList,
                                link: [STATE_LUKIOOSALISTAUS, { suoritustapa: "lukiokoulutus", osanTyyppi: k }],
                                isActive: function(node) {
                                    var url = $location.url(),
                                        href = $state.href(node.link[0], node.link[1]).replace("#/", "/");
                                    return url === href;
                                }
                            };
                        specialPerusteenOsaParts[key] = item;
                        mapYL(itemList, lukioTiedot[k], k, item);
                    });
                    break;
                }
                case "AM":
                    items = getAMItems(isVaTe, vateConverter);
                    break;
                default:
                    break;
            }

            try {
                processNode(data.projekti.peruste.sisalto);
            } catch (err) {
                console.error(err);
            }
            $timeout(function() {
                callbacks.itemsChanged(items);
            });
        }

        function getPerusteenTyyppi(data) {
            if (YleinenData.isPerusopetus(data.projekti.peruste)) {
                return "YL";
            } else if (YleinenData.isAipe(data.projekti.peruste)) {
                return "AIPE";
            } else if (YleinenData.isLukiokoulutus(data.projekti.peruste)) {
                return "LU";
            } else if (YleinenData.isSimple(data.projekti.peruste)) {
                return "ESI";
            } else {
                return "AM";
            }
        }

        async function load() {
            data.projekti = service.getProjekti();
            data.projekti.peruste = service.getPeruste();
            data.projekti.peruste.sisalto = service.getSisalto();
            perusteenTyyppi = getPerusteenTyyppi(data);

            var constIsVaTe = false;
            try {
                constIsVaTe = YleinenData.isValmaTelma(data.projekti.peruste);
            } catch (e) {}

            callbacks.typeChanged(perusteenTyyppi);
            await buildTree(constIsVaTe, Kielimapper.mapTutkinnonosatKoulutuksenosat(constIsVaTe));
        }

        this.register = function(key, cb) {
            callbacks[key] = cb;
        };

        this.refresh = async function(light) {
            if (!service) {
                try {
                    service = await PerusteprojektiTiedotService;
                    await load();
                } catch (err) {
                    console.error(err);
                }
            } else {
                if (light) {
                    await load();
                } else {
                    try {
                        await service.alustaPerusteenSisalto($stateParams, true);
                        await load();
                    } catch (err) {
                        console.error(err);
                    }
                }
            }
        };

        this.setVisible = function(visible) {
            _isVisible = _.isUndefined(visible) ? true : visible;
            if (!_isVisible) {
                PerusopetusService.clearCache();
            }
        };

        this.isVisible = function() {
            return _isVisible;
        };
    });

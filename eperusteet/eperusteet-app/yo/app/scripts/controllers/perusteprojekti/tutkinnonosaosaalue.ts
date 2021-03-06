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
    .controller("TutkinnonOsaOsaAlueCtrl", function(
        $scope,
        $state,
        $stateParams,
        Editointikontrollit,
        TutkinnonOsanOsaAlue,
        Lukitus,
        Notifikaatiot,
        Utils,
        Koodisto,
        Kielimapper,
        YleinenData,
        MuokkausUtils
    ) {
        $scope.osaamistavoitepuu = [];
        var tempId = 0;

        $scope.isVaTe = YleinenData.isValmaTelma($scope.peruste);
        $scope.vateConverter = Kielimapper.mapTutkinnonosatKoulutuksenosat($scope.isVaTe);
        $scope.alarelaatiotLadattu = false;

        $scope.osaAlue = {
            nimi: {},
            kuvaus: {}
        };

        if ($scope.isVaTe) {
            $scope.$$osaamistavoiteOpen = true;
            $scope.$$osaamisenArviointiOpen = true;
            $scope.osaAlue.valmaTelmaSisalto = {
                osaamistavoitteet: {
                    kohde: {},
                    kriteerit: [],
                    tekstina: {}
                },
                osaamisenArviointi: {
                    kuvaus: {},
                    kriteerit: [],
                    tekstina: {}
                }
            };
        }

        $scope.valitseAlarelaatio = function(ar) {
            $scope.osaAlue.koodi = $scope.osaAlue.koodi ? $scope.osaAlue.koodi : {};
            $scope.osaAlue.koodi.uri = _.get(ar, 'koodiUri', null);
            $scope.osaAlue.koodi.koodisto = _.get(ar, 'koodisto.koodistoUri', null);
            $scope.osaAlue.koodi.versio = _.get(ar, 'versio', null);
            $scope.osaAlue.koodiUri = _.get(ar, 'koodiUri', null);
        };

        function luoOsaamistavoitepuu() {
            if ($scope.osaAlue && $scope.osaAlue.osaamistavoitteet) {
                $scope.osaamistavoitepuu = _($scope.osaAlue.osaamistavoitteet)
                    .filter("pakollinen")
                    .each(function(o: any) {
                        o.$poistettu = false;
                    })
                    .value();

                // Alustetaan tutkinnon osaan liittyvät asiat
                if ($scope.tutkinnonOsaViite.tutkinnonOsa.tyyppi === "reformi_tutke2") {
                    if ($scope.osaamistavoitepuu.length == 0) {
                        $scope.lisaaOsaamistavoite();
                    }
                }

                _($scope.osaAlue.osaamistavoitteet)
                    .filter({ pakollinen: false })
                    .each(function(r: any) {
                        r.$poistettu = false;
                        if (r._esitieto) {
                            lisaaOsaamistavoitteelleLapsi(r);
                        } else {
                            $scope.osaamistavoitepuu.push(r);
                        }
                    })
                    .value();
            }
        }

        function lisaaOsaamistavoitteelleLapsi(lapsi) {
            _.each($scope.osaamistavoitepuu, function(osaamistavoite) {
                if (osaamistavoite.id === parseInt(lapsi._esitieto, 10)) {
                    osaamistavoite.lapsi = lapsi;
                }
            });
        }

        $scope.lisaaOsaamistavoite = function() {
            const osaamistavoitePakollinen: any = {
                pakollinen: true,
                nimi: {},
                koodi: null,
                $open: true,
                $poistettu: false
            };
            const osaamistavoiteValinnainen = {
                pakollinen: false,
                koodi: null,
                $poistettu: false
            };
            osaamistavoitePakollinen.lapsi = osaamistavoiteValinnainen;
            $scope.osaamistavoitepuu.push(osaamistavoitePakollinen);
        };

        $scope.tuoOsaamistavoite = function() {
            //TODO
        };

        $scope.poistaTavoite = function(tavoite) {
            if (tavoite.pakollinen === true) {
                if (_.isObject(tavoite.lapsi)) {
                    tavoite.lapsi.nimi = tavoite.nimi;
                    tavoite.lapsi.$open = tavoite.$open;
                    tavoite.lapsi._esitieto = null;
                    $scope.osaamistavoitepuu.push(tavoite.lapsi);
                }
                tavoite.$poistettu = true;
            } else {
                tavoite.$poistettu = true;
            }
        };

        function goBack() {
            $state.go("^", {}, { reload: true });
        }

        $scope.openKoodisto = function(osaAlue) {
            if (!osaAlue.koodi) {
                osaAlue.koodi = {};
            }
            var openDialog = Koodisto.modaali(
                function(koodisto) {
                    osaAlue.koodi = {
                        uri: koodisto.koodiUri,
                        arvo: koodisto.koodiArvo,
                        versio: koodisto.versio,
                        koodisto: koodisto.koodisto.koodistoUri
                    };
                    MuokkausUtils.nestedSet(osaAlue.koodi, "koodiUri", ",", koodisto.koodiUri);
                    MuokkausUtils.nestedSet(osaAlue.koodi, "koodiArvo", ",", koodisto.koodiArvo);
                },
                {
                    tyyppi: function() {
                        return "ammatillisenoppiaineet";
                    },
                    ylarelaatioTyyppi: function() {
                        return "";
                    },
                    tarkista: _.constant(true)
                }
            );
            openDialog();
        };

        var osaAlueCallbacks = {
            edit: function() {
                TutkinnonOsanOsaAlue.get(
                    {
                        viiteId: $stateParams.tutkinnonOsaViiteId,
                        osaalueenId: $stateParams.osaAlueId
                    },
                    function(vastaus) {
                        $scope.osaAlue = vastaus;
                        luoOsaamistavoitepuu();
                    },
                    function(virhe) {
                        Notifikaatiot.serverCb(virhe);
                        goBack();
                    }
                );
            },
            cancel: function() {
                Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
                goBack();
            },
            save: function() {
                $scope.osaAlue.osaamistavoitteet = kokoaOsaamistavoitteet();

                TutkinnonOsanOsaAlue.save(
                    {
                        viiteId: $stateParams.tutkinnonOsaViiteId,
                        osaalueenId: $stateParams.osaAlueId
                    },
                    $scope.osaAlue,
                    function(res) {
                        Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
                        goBack();
                    },
                    function(virhe) {
                        Notifikaatiot.serverCb(virhe);
                        goBack();
                    }
                );
            },
            validate: function() {
                if (!Utils.hasLocalizedText($scope.osaAlue.nimi)) {
                    return false;
                } else {
                    return (
                        $scope.isVaTe ||
                        _.includes(YleinenData.yhteisetTutkinnonOsat, $scope.tutkinnonOsaViite.tutkinnonOsa.tyyppi) ||
                        _.all($scope.osaamistavoitepuu, function(osaamistavoite: any) {
                            return Utils.hasLocalizedText(osaamistavoite.nimi);
                        })
                    );
                }
            }
        };

        function setOsaamistavoiteKoodi(osaamistavoite) {
            if (
                osaamistavoite.koodi &&
                !_.isEmpty(osaamistavoite.koodi.koodiArvo) &&
                !_.isEmpty(osaamistavoite.koodi.koodiUri)
            ) {
                osaamistavoite.koodiArvo = osaamistavoite.koodi.koodiArvo;
                osaamistavoite.koodiUri = osaamistavoite.koodi.koodiUri;
            }
        }

        var kokoaOsaamistavoitteet = function() {
            var osaamistavoitteet = [];
            _.each($scope.osaamistavoitepuu, function(osaamistavoite) {
                if (osaamistavoite.pakollinen && !osaamistavoite.$poistettu) {
                    if (!osaamistavoite.id) {
                        tempId = tempId - 1;
                        osaamistavoite.id = tempId;
                    }

                    setOsaamistavoiteKoodi(osaamistavoite);
                    setOsaamistavoiteKoodi(osaamistavoite.lapsi);

                    osaamistavoitteet.push(osaamistavoite);
                    if (osaamistavoite.lapsi && !osaamistavoite.lapsi.$poistettu) {
                        osaamistavoite.lapsi._esitieto = osaamistavoite.id;
                        osaamistavoite.lapsi.nimi = osaamistavoite.nimi;
                        if (!osaamistavoite.lapsi.id) {
                            tempId = tempId - 1;
                            osaamistavoite.lapsi.id = tempId;
                        }
                        osaamistavoitteet.push(osaamistavoite.lapsi);
                    }
                } else if (!osaamistavoite.pakollinen && !osaamistavoite.$poistettu) {
                    if (!osaamistavoite.id) {
                        tempId = tempId - 1;
                        osaamistavoite.id = tempId;
                    }
                    osaamistavoitteet.push(osaamistavoite);
                }
            });
            return osaamistavoitteet;
        };

        function lukitse(cb) {
            Lukitus.lukitsePerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId, cb);
        }

        Editointikontrollit.registerCallback(osaAlueCallbacks);
        lukitse(function() {
            Editointikontrollit.startEditing();
        });
    });

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

angular.module("eperusteApp").directive("revertNote", function() {
    return {
        template: require("views/partials/muokkaus/revertnote.html"),
        restrict: "AE",
        replace: true,
        scope: {
            versiot: "=versions",
            object: "=",
            revertCb: "&",
            changeVersion: "&"
        },
        controller: function($scope, $state, $stateParams, Varmistusdialogi, Lukitus, VersionHelper, $translate) {
            $scope.version = {
                revert: function() {
                    var suoritustapa = $scope.$parent.suoritustapa;
                    var revCb = function(res) {
                        $scope.revertCb({ response: res });
                    };

                    var cb;
                    switch ($state.current.name) {
                        case "root.perusteprojekti.suoritustapa.tutkinnonosa":
                            cb = function() {
                                Lukitus.lukitsePerusteenosaByTutkinnonOsaViite($scope.object.id, function() {
                                    VersionHelper.revertTutkinnonOsaViite($scope.versiot, $scope.object, revCb);
                                });
                            };
                            break;
                        case "root.perusteprojekti.suoritustapa.tekstikappale":
                            cb = function() {
                                Lukitus.lukitsePerusteenosa($scope.object.id, function() {
                                    VersionHelper.revertPerusteenosa($scope.versiot, $scope.object, revCb);
                                });
                            };
                            break;
                        case "root.perusteprojekti.suoritustapa.muodostumissaannot":
                            cb = function() {
                                Lukitus.lukitseSisalto($scope.object.$peruste.id, suoritustapa, function() {
                                    VersionHelper.revertRakenne(
                                        $scope.versiot,
                                        { id: $scope.object.$peruste.id, suoritustapa: suoritustapa },
                                        revCb
                                    );
                                });
                            };
                            break;
                        case "root.perusteprojekti.suoritustapa.lukioosat":
                            if ($stateParams.osanTyyppi === "aihekokonaisuudet") {
                                cb = function() {
                                    Lukitus.lukitseLukioAihekokonaisuudet().then(function() {
                                        VersionHelper.revertLukioAihekokonaisuudetYleiskuvaus(
                                            $scope.versiot,
                                            { id: $scope.$parent.perusteId, suoritustapa: suoritustapa },
                                            revCb
                                        );
                                    });
                                };
                            } else if ($stateParams.osanTyyppi === "opetuksen_yleiset_tavoitteet") {
                                cb = function() {
                                    Lukitus.lukitseLukioYleisettavoitteet().then(function() {
                                        VersionHelper.revertLukioYleisetTavoitteet(
                                            $scope.versiot,
                                            { id: $scope.$parent.perusteId, suoritustapa: suoritustapa },
                                            revCb
                                        );
                                    });
                                };
                            } else if ($stateParams.osanTyyppi === "oppiaineet_oppimaarat") {
                                if ($stateParams.osanId) {
                                    cb = function() {
                                        Lukitus.lukitseLukioOppiaine($stateParams.osanId).then(function() {
                                            VersionHelper.revertLukioOppiaine(
                                                $scope.versiot,
                                                $stateParams.osanId,
                                                revCb
                                            );
                                        });
                                    };
                                } else {
                                    cb = function() {
                                        Lukitus.lukitseLukiorakenne().then(function() {
                                            VersionHelper.revertLukioRakenne($scope.versiot, null, revCb);
                                        });
                                    };
                                }
                            }
                            break;
                        case "root.perusteprojekti.suoritustapa.lukioosaalue":
                            if ($stateParams.osanTyyppi === "oppiaineet_oppimaarat") {
                                if ($stateParams.osanId) {
                                    cb = function() {
                                        Lukitus.lukitseLukioOppiaine($stateParams.osanId).then(function() {
                                            VersionHelper.revertLukioOppiaine(
                                                $scope.versiot,
                                                $stateParams.osanId,
                                                revCb
                                            );
                                        });
                                    };
                                } else {
                                    cb = function() {
                                        Lukitus.lukitseLukiorakenne().then(function() {
                                            VersionHelper.revertLukioRakenne($scope.versiot, revCb);
                                        });
                                    };
                                }
                            } else {
                                cb = function() {
                                    Lukitus.lukitseLukioAihekokonaisuus($scope.object.id).then(function() {
                                        VersionHelper.revertLukioAihekokonaisuus(
                                            $scope.versiot,
                                            { id: $scope.object.id, suoritustapa: suoritustapa },
                                            revCb
                                        );
                                    });
                                };
                            }
                            break;
                        case "root.perusteprojekti.suoritustapa.kurssi":
                            cb = function() {
                                Lukitus.lukitseLukioKurssi($scope.object.id).then(function() {
                                    VersionHelper.revertLukiokurssi($scope.versiot, $scope.object.id, revCb);
                                });
                            };
                            break;
                        default:
                            cb = angular.noop;
                    }
                    Varmistusdialogi.dialogi({
                        successCb: cb,
                        otsikko: "vahvista-version-palauttaminen",
                        teksti: $translate("vahvista-version-palauttaminen-teksti", {
                            versio: $scope.versiot.chosen.index
                        }), // FIXME
                        primaryBtn: "vahvista",
                        comment: {
                            enabled: false,
                            placeholder: "kommentoi-muutosta"
                        }
                    })();
                },
                goToLatest: function() {
                    VersionHelper.chooseLatest($scope.versiot);
                    $scope.changeVersion();
                }
            };
        }
    };
});

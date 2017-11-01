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
    .service("VersionHelper", function(
        PerusteenOsat,
        $uibModal,
        RakenneVersiot,
        $log,
        RakenneVersio,
        Notifikaatiot,
        $state,
        $location,
        $stateParams,
        TutkinnonOsaViitteet,
        LukioYleisetTavoitteetService,
        LukioAihekokonaisuudetService,
        LukioKurssiService,
        LukioOppiaineService,
        Kayttajatiedot,
        $q
    ) {
        const rakennaNimet = (list: any) => {
            let reqs = [];
            _.forEach(_.uniq(list, "muokkaajaOid"), (i: any) => {
                reqs.push(Kayttajatiedot.get({ oid: i.muokkaajaOid }).$promise);
            });

            $q.all(reqs).then(function(values) {
                _.forEach(list, (name: any) => {
                    const henkilo = _.find(values, (i: any) => i.oidHenkilo === name.muokkaajaOid);
                    const nimi = _.isEmpty(henkilo)
                        ? " "
                        : (henkilo.kutsumanimi || "") + " " + (henkilo.sukunimi || "");
                    name.$nimi = nimi === " " ? name.muokkaajaOid : nimi;
                });
            });
        };

        function getVersions(data, tunniste, tyyppi, force, cb) {
            cb = cb || angular.noop;
            if (!_.isObject(data)) {
                throw "VersionHelper: not an object!";
            }
            if (!force && data.list) {
                return;
            }
            var handle = function(res) {
                rakennaNimet(res);
                data.list = res;
                versiotListHandler(data);
                cb(res);
            };
            switch (tyyppi) {
                case "perusteenosa":
                    PerusteenOsat.versiot({ osanId: tunniste.id }, handle);
                    break;
                case "tutkinnonOsaViite":
                    TutkinnonOsaViitteet.versiot({ viiteId: tunniste.id }, handle);
                    break;
                case "perusteenOsaViite":
                    PerusteenOsat.versiotByViite({ viiteId: tunniste.id }, handle);
                    break;
                case "rakenne":
                    RakenneVersiot.query({ perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa }, handle);
                    break;
                case "lukioyleisettavoitteet":
                    LukioYleisetTavoitteetService.getVersiot().then(handle);
                    break;
                case "lukioaihekokonaisuudet":
                    LukioAihekokonaisuudetService.getAihekokonaisuudetYleiskuvausVersiot().then(handle);
                    break;
                case "lukioaihekokonaisuus":
                    LukioAihekokonaisuudetService.getAihekokonaisuusVersiot(tunniste.id).then(handle);
                    break;
                case "lukiokurssi":
                    LukioKurssiService.listVersions(tunniste.id, cb).then(handle);
                    break;
                case "lukiooppiaine":
                    LukioOppiaineService.listVersions(tunniste.id, cb).then(handle);
                    break;
                case "lukiorakenne":
                    LukioKurssiService.listRakenneVersions(cb).then(handle);
                    break;
                default:
                    $log.error("Unknwon versio tyyppi: ", tyyppi);
            }
        }

        function versiotListHandler(data) {
            data.chosen = latest(data.list);
            data.latest = true;

            _.each(data.list, function(item, index: number) {
                // reverse numbering for UI, oldest = 1
                item.index = data.list.length - index;
            });
        }

        function latest(data) {
            return _.first(data) || {};
        }

        function revert(data, tunniste, tyyppi, cb) {
            var genericHandler = function(res) {
                cb(res);
            };
            // revert = get old (currently chosen) data, save as new version
            if (tyyppi === "Perusteenosa" || tyyppi === "Tutkinnonosa") {
                PerusteenOsat.palauta(
                    {
                        osanId: tunniste.id,
                        versioId: data.chosen.numero
                    },
                    {},
                    cb,
                    Notifikaatiot.serverCb
                );
            } else if (tyyppi === "Rakenne") {
                RakenneVersio.palauta(
                    {
                        perusteId: tunniste.id,
                        suoritustapa: tunniste.suoritustapa,
                        versioId: data.chosen.numero
                    },
                    {},
                    cb,
                    Notifikaatiot.serverCb
                );
            } else if (tyyppi === "TutkinnonOsaViite") {
                TutkinnonOsaViitteet.palauta(
                    {
                        viiteId: tunniste.id,
                        versioId: data.chosen.numero
                    },
                    {},
                    cb,
                    Notifikaatiot.serverCb
                );
            } else if (tyyppi === "lukioyleisettavoitteet") {
                LukioYleisetTavoitteetService.palauta(tunniste.id, data.chosen.numero).then(genericHandler);
            } else if (tyyppi === "lukioaihekokonaosuudetyleiskuvaus") {
                LukioAihekokonaisuudetService.palautaAihekokonaisuudetYleiskuvaus(data.chosen.numero).then(
                    genericHandler
                );
            } else if (tyyppi === "lukioaihekokonaisuus") {
                LukioAihekokonaisuudetService.palautaAihekokonaisuus(tunniste.id, data.chosen.numero).then(
                    genericHandler
                );
            } else if (tyyppi === "lukiokurssi") {
                LukioKurssiService.palautaLukiokurssi(tunniste, data.chosen.numero).then(genericHandler);
            } else if (tyyppi === "lukiooppiaine") {
                LukioOppiaineService.palautaLukioOppiaine(tunniste, data.chosen.numero).then(genericHandler);
            } else if (tyyppi === "lukiorakenne") {
                LukioKurssiService.palautaRakenne(data.chosen.numero).then(genericHandler);
            }
        }

        function change(data, tunniste, tyyppi, cb) {
            if (tyyppi === "Perusteenosa") {
                PerusteenOsat.getVersio(
                    {
                        osanId: tunniste.id,
                        versioId: data.chosen.numero
                    },
                    function(response) {
                        changeResponseHandler(data, response, cb);
                    }
                );
            } else if (tyyppi === "Rakenne") {
                RakenneVersio.get(
                    { perusteId: tunniste.id, suoritustapa: tunniste.suoritustapa, versioId: data.chosen.numero },
                    function(response) {
                        changeResponseHandler(data, response, cb);
                    }
                );
            }
        }

        function changeResponseHandler(data, response, cb) {
            cb(response);
            data.latest = data.chosen.numero === (latest(data.list) as any).numero;
        }

        this.lastModified = function(data) {
            if (data && data.chosen) {
                var found = _.find(data.list, { numero: data.chosen.numero });
                if (found) {
                    return (found as any).pvm;
                }
            }
        };

        this.select = function(data, index) {
            var found = _.find(data.list, { index: parseInt(index, 10) });
            if (found) {
                data.chosen = found;
                data.latest = data.chosen.numero === (latest(data.list) as any).numero;
                return (found as any).numero;
            }
        };

        this.currentIndex = function(data) {
            if (data && data.chosen) {
                return data.chosen.index;
            }
        };

        this.latestIndex = function(data) {
            var latestItem = latest(data.list);
            if (latestItem) {
                return (latestItem as any).index;
            }
        };

        this.getPerusteenosaVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "perusteenosa", force, cb);
        };

        this.getTutkinnonOsaViiteVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "tutkinnonOsaViite", force, cb);
        };

        this.getPerusteenOsaVersionsByViite = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "perusteenOsaViite", force, cb);
        };

        this.getRakenneVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "rakenne", force, cb);
        };

        this.getLukioYleisetTavoitteetVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukioyleisettavoitteet", force, cb);
        };

        this.getLukioAihekokonaisuudetVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukioaihekokonaisuudet", force, cb);
        };

        this.getLukioAihekokonaisuusVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukioaihekokonaisuus", force, cb);
        };

        this.getLukiokurssiVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukiokurssi", force, cb);
        };

        this.getLukioOppiaineVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukiooppiaine", force, cb);
        };

        this.getLukioRakenneVersions = function(data, tunniste, force, cb) {
            getVersions(data, tunniste, "lukiorakenne", force, cb);
        };

        this.chooseLatest = function(data) {
            data.chosen = latest(data.list);
        };

        this.changePerusteenosa = function(data, tunniste, cb) {
            change(data, tunniste, "Perusteenosa", cb);
        };

        this.changeRakenne = function(data, tunniste, cb) {
            change(data, tunniste, "Rakenne", cb);
        };

        this.revertTutkinnonOsaViite = function(data, object, cb) {
            revert(data, { id: object.id }, "TutkinnonOsaViite", cb);
        };

        this.revertPerusteenosa = function(data, object, cb) {
            var isTekstikappale = _.has(object, "nimi") && _.has(object, "teksti");
            var type = isTekstikappale ? "Perusteenosa" : "Tutkinnonosa";
            revert(data, { id: object.id }, type, cb);
        };

        this.revertRakenne = function(data, tunniste, cb) {
            revert(data, tunniste, "Rakenne", cb);
        };

        this.revertLukioYleisetTavoitteet = function(data, tunniste, cb) {
            revert(data, tunniste, "lukioyleisettavoitteet", cb);
        };

        this.revertLukioAihekokonaisuudetYleiskuvaus = function(data, tunniste, cb) {
            revert(data, tunniste, "lukioaihekokonaosuudetyleiskuvaus", cb);
        };

        this.revertLukioAihekokonaisuus = function(data, tunniste, cb) {
            revert(data, tunniste, "lukioaihekokonaisuus", cb);
        };

        this.revertLukiokurssi = function(data, tunniste, cb) {
            revert(data, tunniste, "lukiokurssi", cb);
        };

        this.revertLukioOppiaine = function(data, tunniste, cb) {
            revert(data, tunniste, "lukiooppiaine", cb);
        };

        this.revertLukioRakenne = function(data, tunniste, cb) {
            revert(data, tunniste, "lukiorakenne", cb);
        };

        this.setUrl = function(data) {
            if (_.isEmpty(data)) {
                return;
            }

            const currentIdx = this.currentIndex(data);
            const latestIdx = this.latestIndex(data);

            $state.go($state.current.name, {
                ...$stateParams,
                versio: currentIdx === latestIdx ? undefined : currentIdx
            });
        };

        this.historyView = function(data) {
            $uibModal
                .open({
                    template: require("views/partials/muokkaus/versiohelper.html"),
                    controller: "HistoryViewCtrl",
                    resolve: {
                        versions: function() {
                            return data;
                        }
                    }
                })
                .result.then(function(re) {
                    var params = _.clone($stateParams);
                    params.versio = "/" + re.index;
                    $state.go($state.current.name, params);
                });
        };
    })
    .controller("HistoryViewCtrl", function($scope, versions, $uibModalInstance) {
        $scope.versions = versions;
        $scope.close = function(versio) {
            if (versio) {
                $uibModalInstance.close(versio);
            } else {
                $uibModalInstance.dismiss();
            }
        };
        $scope.paginate = {
            current: 1,
            perPage: 10
        };
    });

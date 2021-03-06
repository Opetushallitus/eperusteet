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
    .service("PerusteenTutkintonimikkeet", function(PerusteTutkintonimikekoodit, YleinenData) {
        this.perusteellaTutkintonimikkeet = function(peruste) {
            if (_.isObject(peruste)) {
                peruste = peruste.koulutustyyppi;
            }
            return (
                _.isString(peruste) &&
                YleinenData.koulutustyyppiInfo[peruste] &&
                YleinenData.koulutustyyppiInfo[peruste].hasTutkintonimikkeet
            );
        };

        this.get = function(perusteId, object) {
            PerusteTutkintonimikekoodit.get({ perusteId: perusteId }, function(res) {
                object.koodisto = _.map(res, function(osa: any) {
                    function parsiNimi(kentta) {
                        if (osa[kentta + "Arvo"]) {
                            let nimi = osa.b[osa[kentta + "Arvo"]].metadata;
                            osa["$" + kentta + "Nimi"] = _.zipObject(_.map(nimi, "kieli"), _.map(nimi, "nimi"));
                        }
                    }

                    parsiNimi("osaamisala");
                    parsiNimi("tutkintonimike");
                    parsiNimi("tutkinnonOsa");
                    delete osa.b;
                    return osa;
                });
                object.koodisto.$resolved = true;
            });
        };
    })
    .controller("PerusteenTiedotCtrl", function(
        $scope,
        $stateParams,
        $state,
        Restangular,
        SERVICE_LOC,
        Koodisto,
        Perusteet,
        YleinenData,
        PerusteProjektiService,
        perusteprojektiTiedot,
        Notifikaatiot,
        Editointikontrollit,
        Kaanna,
        Varmistusdialogi,
        $timeout,
        $rootScope,
        PerusteTutkintonimikekoodit,
        $uibModal,
        PerusteenTutkintonimikkeet,
        valittavatKielet,
        Kieli,
        Arviointiasteikot,
        Upload,
        Api
    ) {
        $scope.kvliiteReadonly = true;


        (async function lataaKvliite() {
            const arviointiasteikotP = Arviointiasteikot.list().$promise;
            const kvliite = await Perusteet.kvliite({ perusteId: $scope.peruste.id }).$promise;
            const arviointiasteikot = await arviointiasteikotP;
            kvliite.tasot = _.filter(kvliite.tasot, taso => (taso as any).nimi);
            $scope.arviointiasteikot = arviointiasteikot;
            $scope.arviointiasteikotMap = _.indexBy(arviointiasteikot, "id");
            // EP-1315
            const arviointiasteikko = _.find($scope.arviointiasteikotMap, (aa: any) => {
                return aa.osaamistasot.length === 1;
            });
            if (arviointiasteikko) {
                arviointiasteikko.osaamistasot[0].otsikko = {};
                _.each(Kieli.SISALTOKIELET, kieli => {
                    arviointiasteikko.osaamistasot[0].otsikko[kieli] = Kaanna.kaanna("kvliiteen-yksiportainen-arviointiasteikko", kieli);
                });
            }
            $scope.koulutustyypit = YleinenData.koulutustyypit;
            $scope.kvliite = kvliite;
            $scope.kvliitePeriytynyt = !kvliite || kvliite.periytynyt;
            $scope.peruste.kvliite = kvliite;
            $scope.editablePeruste.kvliite = kvliite;
            $scope.useampiSuoritustapa = _.size(kvliite.muodostumisenKuvaus) > 1;
        })();

        $scope.editEnabled = false;
        let editingCallbacks = {
            edit: function() {
                fixTimefield("siirtymaPaattyy");
                fixTimefield("paatospvm");
                fixTimefield("voimassaoloAlkaa");
                fixTimefield("voimassaoloLoppuu");
                $scope.editablePeruste = _.cloneDeep($scope.peruste);
                if (!$scope.editablePeruste.kvliite) {
                    $scope.editablePeruste.kvliite = {};
                }
            },
            save: function() {
                $scope.tallennaPeruste();
            },
            asyncValidate: function(after) {
                Editointikontrollit.notifySentenceCaseWarnings({
                    obj: $scope.editablePeruste,
                    paths: [
                        'nimi'
                    ],
                    after: after
                });
            },
            validate: function() {
                return $scope.projektinPerusteForm.$valid && !_.isEmpty($scope.editablePeruste.kielet);
            },
            cancel: function() {
                $scope.editablePeruste = $scope.peruste;
            },
            notify: function(mode) {
                $scope.editEnabled = mode;
                // Fix msd-elastic issue of setting incorrect initial height
                $timeout(function() {
                    $rootScope.$broadcast("elastic:adjust");
                });
            }
        };
        Editointikontrollit.registerCallback(editingCallbacks);

        $scope.voiMuokata = function() {
            // TODO Vain omistaja/sihteeri voi muokata
            return true;
        };

        $scope.muokkaa = function() {
            Editointikontrollit.startEditing();
        };

        function noudaKorvattavienDiaarienNimet(korvattavatDiaarinumerot) {
            if (korvattavatDiaarinumerot !== null && angular.isDefined(korvattavatDiaarinumerot)) {
                angular.forEach(korvattavatDiaarinumerot, function(value) {
                    noudaDiaarilleNimi(value);
                });
            }
            $scope.ladataanKorvattavia = false;
        }

        function noudaDiaarilleNimi(diaari) {
            Perusteet.diaari(
                { diaarinumero: diaari },
                function(vastaus) {
                    $scope.korvattavaDiaariNimiMap[diaari] = vastaus.nimi;
                },
                function() {
                    $scope.korvattavaDiaariNimiMap[diaari] = "korvattavaa-ei-loydy-jarjestelmasta";
                }
            );
        }

        $scope.lisaaKorvattavaDiaari = function(uusiKorvattavaDiaari) {
            if (_.indexOf($scope.editablePeruste.korvattavatDiaarinumerot, uusiKorvattavaDiaari) !== -1) {
                Notifikaatiot.varoitus("diaari-jo-listalla");
            } else if (!YleinenData.isDiaariValid(uusiKorvattavaDiaari)) {
                Notifikaatiot.varoitus("diaarinumero-ei-validi");
            } else if (uusiKorvattavaDiaari === $scope.editablePeruste.diaarinumero) {
                Notifikaatiot.varoitus("oma-diaarinumero");
            } else {
                $scope.editablePeruste.korvattavatDiaarinumerot.push(uusiKorvattavaDiaari);
                noudaDiaarilleNimi(uusiKorvattavaDiaari);
                $scope.uusiKorvattavaDiaari = "";
            }
        };

        $scope.poistaKorvattavaDiaari = function(diaarinumero) {
            const index = _.indexOf($scope.editablePeruste.korvattavatDiaarinumerot, diaarinumero);
            if (index >= 0) {
                $scope.editablePeruste.korvattavatDiaarinumerot.splice(index, 1);
            }
        };

        $scope.ladataanKorvattavia = true;
        $scope.korvattavaDiaariNimiMap = {};
        $scope.hakemassa = false;
        $scope.peruste = perusteprojektiTiedot.getPeruste();

        noudaKorvattavienDiaarienNimet($scope.peruste.korvattavatDiaarinumerot);
        $scope.editablePeruste = $scope.peruste;
        $scope.peruste.nimi = $scope.peruste.nimi || {};
        $scope.peruste.maarayskirje = $scope.peruste.maarayskirje || {};
        $scope.peruste.kuvaus = $scope.peruste.kuvaus || {};
        $scope.projektiId = $stateParams.perusteProjektiId;
        // $scope.open = {};
        $scope.suoritustapa = PerusteProjektiService.getSuoritustapa() || "naytto";
        $scope.kielet = YleinenData.kielet;
        $scope.dokumentit = {};
        $scope.koodisto = [];
        $scope.$koodistoResolved = false;
        $scope.$perusteellaTutkintonimikkeet = PerusteenTutkintonimikkeet.perusteellaTutkintonimikkeet($scope.peruste);
        $scope.kieliOrder = Kieli.kieliOrder;
        $scope.liite = {
            files: []
        };
        $scope.liitteet = [];
        $scope.liitteetUrl =  window.location.origin + SERVICE_LOC + "/perusteet/" + $scope.peruste.id + "/liitteet/";

        $scope.getLiitteet = async () => {
            $scope.liitteet = Restangular.stripRestangular(await Api.one("perusteet", $scope.peruste.id).all("liitteet").getList());
        };
        $scope.getLiitteet();

        $scope.saveLiite = async (tyyppi) => {
            try {
                const image = $scope.liite.files[0];
                const url = SERVICE_LOC + "/perusteet/" + $scope.peruste.id + "/liitteet";
                const data = {
                    url: url,
                    fields: {
                        nimi: image.name,
                        tyyppi: tyyppi
                    },
                    file: image
                };

                await Upload.upload(data);

                $scope.message = "liitetiedosto-tallennettu";
                $scope.liite.files = [];

                await $scope.getLiitteet();

            } catch (err) {
                $scope.message = err.syy || "liitetiedosto-tallennusvirhe";
                $scope.liite.files = [];
            }
        };

        $scope.Osaamisala = {
            poista: function(oa) {
                _.remove($scope.editablePeruste.osaamisalat, oa);
            },
            lisaa: function() {
                Koodisto.modaali(
                    function(koodi) {
                        $scope.editablePeruste.osaamisalat.push({
                            nimi: koodi.nimi,
                            arvo: koodi.koodiArvo,
                            uri: koodi.koodiUri,
                            koodisto: koodi.koodisto.koodistoUri
                        });
                    },
                    {
                        tyyppi: function() {
                            return "osaamisala";
                        },
                        ylarelaatioTyyppi: function() {
                            return "";
                        }
                    },
                    angular.noop,
                    null
                )();
            }
        };

        function valitseValittavatKielet(kielet = undefined) {
            let current = kielet || $scope.editablePeruste.kielet;
            $scope.valittavatKielet = _(valittavatKielet)
                .sortBy($scope.kieliOrder)
                .map(function(kielikoodi) {
                    return { available: _.indexOf(current, kielikoodi) === -1, koodi: kielikoodi };
                })
                .value();
        }
        valitseValittavatKielet($scope.peruste.kielet);

        $scope.lisaaKieli = function(kieli) {
            $scope.editablePeruste.kielet.push(kieli);
            $scope.editablePeruste.kielet = _.unique($scope.editablePeruste.kielet);
            valitseValittavatKielet();
        };

        $scope.poistaKieli = function(kieli) {
            _.remove($scope.editablePeruste.kielet, function(v) {
                return v === kieli;
            });
            valitseValittavatKielet();
        };

        $scope.kaikkiKieletValittu = function() {
            return _.size($scope.editablePeruste.kielet) === _.size(valittavatKielet);
        };

        $scope.lisaaNimike = function() {
            $uibModal
                .open({
                    template: require("views/modals/lisaaTutkintonimike.html"),
                    controller: function($q, $scope, $uibModalInstance) {
                        $scope.koodit = {};
                        $scope.valmisCb = function(res) {
                            $scope.koodit[res.koodisto.koodistoUri] = res;
                        };

                        $scope.ok = $uibModalInstance.close;
                        $scope.peru = $uibModalInstance.dismiss;
                    }
                })
                .result.then(function(uusi) {
                    let obj: any = {
                        peruste: $scope.peruste.id,
                        tutkintonimikeUri: uusi.tutkintonimikkeet.koodiUri,
                        tutkintonimikeArvo: uusi.tutkintonimikkeet.koodiArvo,
                        $tutkintonimikeNimi: uusi.tutkintonimikkeet.nimi
                    };

                    if (uusi.osaamisala) {
                        obj.osaamisalaUri = uusi.osaamisala.koodiUri;
                        obj.osaamisalaArvo = uusi.osaamisala.koodiArvo;
                        obj.$osaamisalaNimi = uusi.osaamisala.nimi;
                    }

                    if (uusi.tutkinnonosat) {
                        obj.tutkinnonOsaUri = uusi.tutkinnonosat.koodiUri;
                        obj.tutkinnonOsaArvo = uusi.tutkinnonosat.koodiArvo;
                        obj.$tutkinnonOsaNimi = uusi.tutkinnonosat.nimi;
                    }

                    $scope.koodisto.push(obj);
                    PerusteTutkintonimikekoodit.save(
                        { perusteId: $scope.peruste.id },
                        obj,
                        function(res) {
                            obj.id = res.id;
                            Notifikaatiot.onnistui("tutkintonimikkeen-lisays-onnistui");
                        },
                        function() {
                            _.remove($scope.koodisto, obj);
                            Notifikaatiot.varoitus("tutkintonimikkeen-lisays-epaonnistui");
                        }
                    );
                });
        };

        $scope.lisaaMuutosmaarays = () =>
            $scope.editablePeruste.muutosmaaraykset.push({
                url: {}
            });
        $scope.poistaMuutosmaarays = muutosmaarays => _.remove($scope.editablePeruste.muutosmaaraykset, muutosmaarays);

        $scope.poistaLiite = async liite => {
            await Api.one("perusteet", $scope.peruste.id).one("liitteet", liite.id).remove();
            _.remove($scope.liitteet, liite);
            _.each($scope.editablePeruste.maarayskirje.liitteet, (l: any, kieli) => {
                if (l.id === liite.id) {
                    delete $scope.editablePeruste.maarayskirje.liitteet[kieli];
                    delete $scope.peruste.maarayskirje.liitteet[kieli];
                }
            });
            _.each($scope.editablePeruste.muutosmaaraykset, muutosmaarays => {
                _.each(muutosmaarays.liitteet, (l: any, kieli) => {
                    if (l.id === liite.id) {
                        delete muutosmaarays.liitteet[kieli];
                    }
                });
            });
            _.each($scope.peruste.muutosmaaraykset, muutosmaarays => {
                _.each(muutosmaarays.liitteet, (l: any, kieli) => {
                    if (l.id === liite.id) {
                        delete muutosmaarays.liitteet[kieli];
                    }
                });
            });
        };

        PerusteenTutkintonimikkeet.get($scope.peruste.id, $scope);

        $scope.poistaTutkintonimike = function(nimike) {
            PerusteTutkintonimikekoodit.remove(
                {
                    perusteId: $scope.peruste.id,
                    nimikeId: nimike.id
                },
                function() {
                    _.remove($scope.koodisto, nimike);
                    Notifikaatiot.onnistui("tutkintonimike-poistettu-onnistuneesti");
                },
                Notifikaatiot.serverCb
            );
        };

        function fixTimefield(field) {
            if (typeof $scope.peruste[field] === "number") {
                $scope.peruste[field] = new Date($scope.peruste[field]);
            }
        }

        let currentTime = new Date().getTime();

        $scope.voimassaOleva = !!(
            !$scope.peruste.voimassaoloLoppuu ||
            ($scope.peruste.voimassaoloAlkaa &&
                currentTime > $scope.peruste.voimassaoloAlkaa &&
                currentTime < $scope.peruste.voimassaoloLoppuu)
        );

        fixTimefield("siirtymaPaattyy");
        fixTimefield("voimassaoloAlkaa");
        fixTimefield("voimassaoloLoppuu");

        $scope.rajaaKoodit = function(koodi) {
            return koodi.koodi.indexOf("_3") !== -1;
        };

        $scope.hasContent = function(value) {
            if (_.isEmpty(value)) {
                return false;
            }
            if (_.isObject(value)) {
                return _.any(_.values(value));
            }
            return !!value;
        };

        $scope.koodistoHaku = function(koodisto) {
            let added: any = {
                nimi: koodisto.nimi,
                koulutuskoodiArvo: koodisto.koodiArvo,
                koulutuskoodiUri: koodisto.koodiUri
            };
            // Kun ensimmäinen koodi lisätään, perusteen nimi kopioidaan koodistosta
            if ($scope.editablePeruste.koulutukset.length === 0) {
                _.each(_.values(YleinenData.kielet), function(kieli: any) {
                    if (koodisto.nimi.hasOwnProperty(kieli)) {
                        $scope.editablePeruste.nimi[kieli] = koodisto.nimi[kieli];
                    }
                });
            }
            $scope.editablePeruste.koulutukset.push(added);

            // $scope.open[koodisto.koodi] = true;

            Koodisto.haeAlarelaatiot(
                koodisto.koodiUri,
                function(relaatiot) {
                    _.forEach(relaatiot, function(rel) {
                        switch (rel.koodisto.koodistoUri) {
                            case "koulutusalaoph2002":
                                added.koulutusalakoodi = rel.koodiUri;
                                break;
                            case "opintoalaoph2002":
                                added.opintoalakoodi = rel.koodiUri;
                                break;
                        }
                    });
                },
                Notifikaatiot.fataali
            );
        };

        $scope.tallennaPeruste = function() {
            if (!$scope.editablePeruste.voimassaoloLoppuu) {
                delete $scope.editablePeruste.siirtymaPaattyy;
            }
            // Poistetaan määräyskirjeen tyhjät kentät
            if ($scope.editablePeruste.maarayskirje.liitteet) {
                _.each($scope.editablePeruste.maarayskirje.liitteet, (value, key) => {
                    if (_.isEmpty(value)) {
                        delete $scope.editablePeruste.maarayskirje.liitteet[key];
                    }
                });
            }
            // Poistetaan muutoskirjeen tyhjät kentät
            _.each($scope.editablePeruste.muutosmaaraykset, muutosmaarays => {
                if (muutosmaarays.liitteet) {
                    _.each(muutosmaarays.liitteet, (value, key) => {
                        if (_.isEmpty(value)) {
                            delete muutosmaarays.liitteet[key];
                        }
                    });
                }
            });
            Perusteet.save(
                { perusteId: $scope.peruste.id },
                $scope.editablePeruste,
                res => {
                    $scope.peruste = res;
                    PerusteProjektiService.update();
                    Notifikaatiot.onnistui("tallennettu");
                },
                err => {
                    Notifikaatiot.serverCb(err);
                }
            );
        };

        $scope.avaaKoodistoModaali = Koodisto.modaali(
            $scope.koodistoHaku,
            {
                tyyppi: _.constant("koulutus"),
                // ylarelaatioTyyppi: () => $scope.editablePeruste.koulutustyyppi
                ylarelaatioTyyppi: () => "" // Koodistohaun ehto on outo
            },
            _.noop,
            null
        );

        $scope.poistaKoulutus = function(koulutuskoodiArvo) {
            Varmistusdialogi.dialogi({
                otsikko: "vahvista-poisto",
                teksti: "poistetaanko-koulutus",
                primaryBtn: "poista",
                successCb: function() {
                    $scope.editablePeruste.koulutukset = _.remove($scope.editablePeruste.koulutukset, function(
                        koulutus
                    ) {
                        return (koulutus as any).koulutuskoodiArvo !== koulutuskoodiArvo;
                    });
                }
            })();
        };

        $scope.koulutusalaNimi = function(koodi) {
            return $scope.Koulutusalat.haeKoulutusalaNimi(koodi);
        };

        $scope.opintoalaNimi = function(koodi) {
            return $scope.Opintoalat.haeOpintoalaNimi(koodi);
        };

        $scope.$on("event:spinner_on", function() {
            $scope.hakemassa = true;
        });

        $scope.$on("event:spinner_off", function() {
            $scope.hakemassa = false;
        });
    })
    .directive("datepickerPopup", function(uibDatepickerPopupConfig, dateFilter) {
        return {
            restrict: "A",
            require: "^ngModel",
            link: function($scope, element, attrs, ngModel: any) {
                let dateFormat;

                // FIXME Temp fix for Angular 1.3 support [#2659](https://github.com/angular-ui/bootstrap/issues/2659)
                attrs.$observe("uibDatepickerPopup", function(value) {
                    dateFormat = value || uibDatepickerPopupConfig.datepickerPopup;
                    ngModel.$render();
                });

                ngModel.$formatters.push(function(value) {
                    return ngModel.$isEmpty(value) ? value : dateFilter(value, dateFormat);
                });
            }
        };
    });

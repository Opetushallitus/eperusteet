import * as angular from "angular";
import _ from "lodash";

export function taiteenalaCtrl(
    $location,
    $q,
    $rootScope,
    $scope,
    $state,
    $stateParams,
    Editointikontrollit,
    Kaanna,
    Kommentit,
    KommentitByPerusteenOsa,
    Koodisto,
    Lukitus,
    Notifikaatiot,
    PerusteProjektiSivunavi,
    PerusteenOsanTyoryhmat,
    PerusteenOsat,
    PerusteprojektiTiedotService,
    PerusteprojektiTyoryhmat,
    ProjektinMurupolkuService,
    TEXT_HIERARCHY_MAX_DEPTH,
    TekstikappaleOperations,
    TutkinnonOsaEditMode,
    Tyoryhmat,
    Utils,
    Varmistusdialogi,
    VersionHelper,
    YleinenData,
    perusteprojektiBackLink,
    virheService
) {
    async function init() {
        $scope.taiteenala = {};
        $scope.versiot = {};
        const pts = await PerusteprojektiTiedotService;
        $scope.peruste = pts.getPeruste();

        $scope.sisalto = {};
        $scope.viitteet = {};
        $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);

        $scope.koodistoClick = Koodisto.modaali(
            ({ nimi, koodiArvo, koodiUri, koodisto: { koodistoUri } }) => {
                $scope.editableTaiteenala.koodi = {
                    nimi,
                    arvo: koodiArvo,
                    uri: koodiUri,
                    koodisto: koodistoUri
                };
            },
            {
                tyyppi: () => {
                    return "oppiaineetyleissivistava2";
                },
                ylarelaatioTyyppi: () => {
                    return "";
                },
                tarkista: _.constant(true)
            }
        );

        $scope.cleanKoodi = () => {
            $scope.editableTaiteenala.koodi = null;
        };

        var taiteenalaDefer = $q.defer();
        $scope.taiteenalaPromise = taiteenalaDefer.promise;

        $scope.valitseOsaamisala = function(oa) {
            $scope.editableTaiteenala.osaamisala = oa;
        };

        $scope.kopioiMuokattavaksi = function() {
            TekstikappaleOperations.clone($scope.viitteet[$scope.taiteenala.id].viite);
        };

        $scope.muokkaa = async () => {
            Editointikontrollit.startEditing(await lukitse());
        };

        $scope.canAddLapsi = function() {
            return (
                $scope.taiteenala.id &&
                $scope.viitteet[$scope.taiteenala.id] &&
                $scope.viitteet[$scope.taiteenala.id].level < TEXT_HIERARCHY_MAX_DEPTH - 1
            );
        };

        $scope.addLapsi = function() {
            TekstikappaleOperations.addChild($scope.viiteId(), $stateParams.suoritustapa);
        };

        $scope.$watch("editEnabled", function(editEnabled) {
            PerusteProjektiSivunavi.setVisible(!editEnabled);
        });

        $scope.fields = [
            {
                path: "nimi",
                hideHeader: false,
                localeKey: "teksikappaleen-nimi",
                type: "editor-header",
                localized: true,
                mandatory: true,
                mandatoryMessage: "mandatory-otsikkoa-ei-asetettu",
                order: 1
            },
            {
                path: "teksti",
                hideHeader: false,
                localeKey: "tekstikappaleen-teksti",
                type: "editor-area",
                localized: true,
                mandatory: false,
                order: 2
            }
        ];

        $scope.taiteenalaFields = [
            {
                path: "aikuistenOpetus",
                localeKey: "taiteenala-aikuisten-opetus"
            },
            {
                path: "kasvatus",
                localeKey: "taiteenala-kasvatus"
            },
            {
                path: "oppimisenArviointiOpetuksessa",
                localeKey: "taiteenala-oppimisen-arvionti"
            },
            {
                path: "teemaopinnot",
                localeKey: "taiteenala-teemaopinnot"
            },
            {
                path: "tyotavatOpetuksessa",
                localeKey: "taiteenala-tyotavat"
            },
            {
                path: "yhteisetOpinnot",
                localeKey: "taiteenala-yhteisetOpinnot"
            }
        ];

        function fieldCount(): number {
            let result = 0;
            for (const field of $scope.taiteenalaFields) {
                if ($scope.editableTaiteenala[field.path]) {
                    result += 1;
                }
            }
            return result;
        }

        const sisaltoCache = {};
        $scope.lisaaTaiteenalasisalto = path => {
            $scope.editableTaiteenala[path] = sisaltoCache[path] ? sisaltoCache[path] : {};
            $scope.editableTaiteenala[path].jrno = fieldCount();
        };

        $scope.sortableOptions = {
            cursor: "move",
            cursorAt: { top: 2, left: 2 },
            handle: ".handle",
            delay: 100,
            tolerance: "pointer"
        };

        $scope.removeTaiteenalasisalto = path => {
            sisaltoCache[path] = $scope.editableTaiteenala[path];
            $scope.editableTaiteenala[path] = null;
        };

        $scope.sortedFields = [];

        const updateSortedFields = () => {
            $scope.sortedFields = _($scope.taiteenalaFields)
                .filter((val: any) => $scope.taiteenala[val.path])
                .map((val: any) => ({
                    ...$scope.taiteenala[val.path],
                    localeKey: val.localeKey
                }))
                .sortBy("jnro")
                .value();
            $scope.taiteenalaFields = _.sortBy($scope.taiteenalaFields, (field: any) => {
                return $scope.taiteenala[field.path] && $scope.taiteenala[field.path].jnro;
            });
        };

        $scope.poistaTyoryhma = function(tr) {
            Varmistusdialogi.dialogi({
                successCb: function() {
                    var uusi = _.remove(_.clone($scope.tyoryhmat), function(vanha) {
                        return vanha !== tr;
                    });
                    paivitaRyhmat(uusi, function() {
                        $scope.tyoryhmat = uusi;
                    });
                },
                otsikko: "poista-tyoryhma-perusteenosasta",
                teksti: Kaanna.kaanna("poista-tyoryhma-teksti", { nimi: tr })
            })();
        };

        $scope.lisaaTyoryhma = function() {
            Tyoryhmat.valitse(_.clone($scope.kaikkiTyoryhmat), _.clone($scope.tyoryhmat), function(uudet) {
                var uusi = _.clone($scope.tyoryhmat).concat(uudet);
                paivitaRyhmat(uusi, function() {
                    $scope.tyoryhmat = uusi;
                });
            });
        };

        $scope.tree = {
            init: function() {
                updateViitteet();
            },
            get: function() {
                var items: any[] = [];
                var id = $scope.taiteenala.id;
                if ($scope.viitteet[id]) {
                    do {
                        items.push({
                            label: $scope.viitteet[id].nimi,
                            url:
                                $scope.taiteenala.id === id
                                    ? null
                                    : $state.href("root.perusteprojekti.suoritustapa.tekstikappale", {
                                          perusteenOsaViiteId: $scope.viitteet[id].viite,
                                          versio: ""
                                      })
                        });
                        id = $scope.viitteet[id] ? $scope.viitteet[id].parent : null;
                    } while (id);
                }
                items.reverse();
                return items.length > 1 ? items : [];
            }
        };

        $scope.vaihdaVersio = function(versio) {
            $scope.versiot.hasChanged = true;
            VersionHelper.setUrl($scope.versiot);
            //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.taiteenala.id}, responseFn);
        };

        $scope.revertCb = function(response) {
            responseFn(response);
            saveCb(response);
        };

        $scope.poista = function() {
            var nimi = Kaanna.kaanna($scope.taiteenala.nimi);

            Varmistusdialogi.dialogi({
                successCb: doDelete,
                otsikko: "poista-tekstikappale-otsikko",
                teksti: Kaanna.kaanna("poista-tekstikappale-teksti", { nimi: nimi })
            })();
        };

        function successCb(re) {
            if (re.osanTyyppi !== "taiteenala") {
                $location.path(perusteprojektiBackLink.substring(1));
            }

            $scope.taiteenala = re;
            setupTekstikappale($scope.taiteenala);
            taiteenalaDefer.resolve($scope.taiteenala);
            updateSortedFields();
            if (TutkinnonOsaEditMode.getMode()) {
                $scope.isNew = true;
                $scope.muokkaa();
            }
        }

        function errorCb() {
            virheService.virhe("virhe-tekstikappaletta-ei-löytynyt");
        }

        var versio = $stateParams.versio;
        if (versio) {
            VersionHelper.getPerusteenOsaVersionsByViite(
                $scope.versiot,
                { id: $stateParams.perusteenOsaViiteId },
                true,
                function() {
                    var revNumber = VersionHelper.select($scope.versiot, versio);
                    if (!revNumber) {
                        errorCb();
                    } else {
                        PerusteenOsat.getVersioByViite(
                            {
                                viiteId: $stateParams.perusteenOsaViiteId,
                                versioId: revNumber
                            },
                            successCb,
                            errorCb
                        );
                    }
                }
            );
        } else {
            PerusteenOsat.getByViite({ viiteId: $stateParams.perusteenOsaViiteId }, successCb, errorCb);
        }

        TekstikappaleOperations.setPeruste($scope.$parent.peruste);
        $scope.kaikkiTyoryhmat = [];

        function paivitaRyhmat(uudet, cb) {
            PerusteenOsanTyoryhmat.save(
                {
                    projektiId: $stateParams.perusteProjektiId,
                    osaId: $scope.taiteenala.id
                },
                uudet,
                cb,
                Notifikaatiot.serverCb
            );
        }

        Utils.scrollTo("#ylasivuankkuri");
        Kommentit.haeKommentit(KommentitByPerusteenOsa, {
            id: $stateParams.perusteProjektiId,
            perusteenOsaId: $stateParams.perusteenOsaViiteId
        });

        async function haeSisalto() {
            if ($scope.tiedotService) {
                const res = await $scope.tiedotService.haeSisalto($scope.$parent.peruste.id, $stateParams.suoritustapa);
                $scope.sisalto = res;
                setNavigation();
            }
        }

        if (
            $stateParams.suoritustapa ||
            YleinenData.isPerusopetus($scope.$parent.peruste) ||
            YleinenData.isLukiokoulutus($scope.$parent.peruste) ||
            YleinenData.isAipe($scope.$parent.peruste)
        ) {
            const instance = await PerusteprojektiTiedotService;
            $scope.tiedotService = instance;
            await haeSisalto();
        }

        function lukitse() {
            return $q(resolve => {
                Lukitus.lukitsePerusteenosa($scope.taiteenala.id, resolve);
            });
        }

        function fetch(cb) {
            PerusteenOsat.get(
                { osanId: $scope.taiteenala.id },
                (_ as any).setWithCallback($scope, "tekstikappale", cb)
            );
        }

        async function setNavigation() {
            $scope.tree.init();
            ProjektinMurupolkuService.setCustom($scope.tree.get());
            VersionHelper.setUrl($scope.versiot);
        }

        $scope.viiteId = function() {
            return $scope.viitteet[$scope.taiteenala.id] ? $scope.viitteet[$scope.taiteenala.id].viite : null;
        };

        async function storeTree(sisalto, level = 0) {
            sisalto = await sisalto;
            _.each(sisalto.lapset, function(lapsi) {
                if (lapsi.perusteenOsa) {
                    if (!_.isObject($scope.viitteet[lapsi.perusteenOsa.id])) {
                        $scope.viitteet[lapsi.perusteenOsa.id] = {};
                    }
                    $scope.viitteet[lapsi.perusteenOsa.id].viite = lapsi.id;
                    $scope.viitteet[lapsi.perusteenOsa.id].level = level;
                    $scope.viitteet[lapsi.perusteenOsa.id].nimi = lapsi.perusteenOsa.nimi;
                    if (sisalto.perusteenOsa) {
                        $scope.viitteet[lapsi.perusteenOsa.id].parent = sisalto.perusteenOsa.id;
                    }
                    storeTree(lapsi, level + 1);
                }
            });
        }

        function updateViitteet() {
            $scope.viitteet = {};
            storeTree($scope.sisalto);
        }

        function refreshPromise() {
            $scope.editableTaiteenala = angular.copy($scope.taiteenala);
            taiteenalaDefer = $q.defer();
            $scope.taiteenalaPromise = taiteenalaDefer.promise;
            taiteenalaDefer.resolve($scope.editableTaiteenala);
        }

        async function saveCb(res) {
            // Päivitä versiot
            const versiot = await haeVersiot(true);
            VersionHelper.setUrl($scope.versiot);
            PerusteProjektiSivunavi.refresh();
            Lukitus.vapautaPerusteenosa(res.id);
            Notifikaatiot.onnistui("muokkaus-tekstikappale-tallennettu");
            await haeSisalto();
            updateSortedFields();
        }

        function doDelete(isNew) {
            TekstikappaleOperations.delete($scope.viiteId(), isNew);
        }

        async function setupTekstikappale(kappale) {
            try {
                const data = await $q.all([
                    PerusteenOsanTyoryhmat.get({
                        projektiId: $stateParams.perusteProjektiId,
                        osaId: $scope.taiteenala.id
                    }).$promise,
                    PerusteprojektiTyoryhmat.get({ id: $stateParams.perusteProjektiId }).$promise
                ]);
                $scope.tyoryhmat = data[0];
                $scope.kaikkiTyoryhmat = _.unique(_.map(data[1], "nimi"));
            } catch (error) {
                Notifikaatiot.serverCb(error);
            }

            $scope.editableTaiteenala = angular.copy(kappale);

            Editointikontrollit.registerCallback({
                edit: () => {
                    return $q((resolve, reject) => {
                        TekstikappaleOperations.noDeleteWasDoneYet();
                        lukitse().then(() => {
                            fetch(function() {
                                refreshPromise();
                                resolve();
                            });
                        });
                    });
                },
                asyncValidate: function(cb) {
                    Editointikontrollit.notifySentenceCaseWarnings({
                        obj: $scope.editableTaiteenala,
                        paths: [
                            'nimi'
                        ],
                        after: cb
                    });
                },
                save: kommentti => {
                    return $q((resolve, reject) => {
                        let idx = 0;
                        for (const field of $scope.taiteenalaFields) {
                            if ($scope.editableTaiteenala[field.path]) {
                                $scope.editableTaiteenala[field.path].jnro = idx++;
                            }
                        }
                        $scope.editableTaiteenala.metadata = { kommentti: kommentti };
                        PerusteenOsat.saveTekstikappale(
                            {
                                osanId: $scope.editableTaiteenala.id
                            },
                            $scope.editableTaiteenala,
                            function(res) {
                                saveCb(res);
                                $scope.taiteenala = angular.copy($scope.editableTaiteenala);
                                $scope.isNew = false;
                                resolve();
                            },
                            Notifikaatiot.serverCb
                        );
                    });
                },
                cancel: () => {
                    return $q((resolve, reject) => {
                        if (!TekstikappaleOperations.wasDeleted()) {
                            Lukitus.vapautaPerusteenosa($scope.taiteenala.id, function() {
                                if ($scope.isNew) {
                                    doDelete(true);
                                } else {
                                    fetch(function() {
                                        refreshPromise();
                                    });
                                }
                                $scope.isNew = false;
                            });
                        }
                        resolve();
                    });
                },
                notify: mode => {
                    $scope.editEnabled = mode;
                },
                validate: mandatoryValidator => {
                    return mandatoryValidator($scope.fields, $scope.editableTaiteenala);
                }
            });

            await haeVersiot();
            await setNavigation();
            Lukitus.tarkista($scope.taiteenala.id, $scope);
        }
        async function haeVersiot(force?) {
            return VersionHelper.getPerusteenosaVersions($scope.versiot, { id: $scope.taiteenala.id }, force);
        }

        async function responseFn(response) {
            $scope.taiteenala = response;
            await setupTekstikappale(response);
            taiteenalaDefer = $q.defer();
            $scope.taiteenalaPromise = taiteenalaDefer.promise;
            await taiteenalaDefer.resolve($scope.editableTaiteenala);
            VersionHelper.setUrl($scope.versiot);
        }

        // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
        var received = 0;
        $scope.$on("ckEditorInstanceReady", function() {
            if (++received === $scope.fields.length) {
                $rootScope.$broadcast("editointikontrollitRefresh");
            }
        });
    }
    init();
}

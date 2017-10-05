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

angular.module("eperusteApp").service("Muodostumissaannot", function($uibModal, $q) {
    var skratchpadHasContent = false;
    function osienLaajuudenSumma(rakenneOsat) {
        return (
            _(rakenneOsat || [])
                .map(function(osa) {
                    return osa
                        ? osa.$vaadittuLaajuus && osa.$laajuus > osa.$vaadittuLaajuus
                          ? osa.$vaadittuLaajuus
                          : osa.$laajuus
                        : 0;
                })
                .reduce(function(sum, newval) {
                    return sum + newval;
                }, 0) || 0
        );
    }

    function kaannaSaanto(ms?) {
        if (!ms) {
            return;
        }
        var fraasi: any = {
            kaannos: "",
            muuttujat: {}
        };

        var msl = ms.laajuus;
        var msk = ms.koko;

        if (msl && msl.minimi && msl.maksimi) {
            fraasi.kaannos = "osia-valittava-vahintaan-laajuus";
            fraasi.muuttujat.laajuusMinimi = msl.minimi;

            if (msl.minimi !== msl.maksimi) {
                fraasi.kaannos = "osia-valittava-vahintaan-ja-enintaan-laajuus";
                fraasi.muuttujat.laajuusMaksimi = msl.maksimi;
            }
        } else if (msk && msk.minimi && msk.maksimi) {
            fraasi.kaannos = "osia-valittava-vahintaan-koko";
            fraasi.muuttujat.kokoMinimi = msk.minimi;
            if (msk.minimi !== msk.maksimi) {
                fraasi.kaannos = "osia-valittava-vahintaan-ja-enintaan-koko";
                fraasi.muuttujat.kokoMaksimi = msk.maksimi;
            }
        }

        return fraasi;
    }

    /* TODO (jshint complexity/W074) simplify/split ---> */
    function validoiRyhma(rakenne, viitteet) {
        var virheet = 0;

        function lajittele(osat) {
            var buckets = {};
            _.forEach(osat, function(osa) {
                if (!buckets[osa.$laajuus]) {
                    buckets[osa.$laajuus] = 0;
                }
                buckets[osa.$laajuus] += 1;
            });
            return buckets;
        }

        function asetaVirhe(virhe, ms?) {
            rakenne.$virhe = {
                virhe: virhe,
                selite: kaannaSaanto(ms)
            };
            virheet += 1;
        }

        function avaintenSumma(osat, n, avaimetCb) {
            var res = 0;
            var i = n;
            var lajitellut = lajittele(osat);
            _.forEach(avaimetCb(lajitellut), function(k) {
                while (lajitellut[k]-- > 0 && i-- > 0) {
                    res += parseInt(k, 10) || 0;
                }
            });
            return res;
        }

        if (!rakenne || !rakenne.osat) {
            return 0;
        }

        delete rakenne.$virhe;

        _.forEach(rakenne.osat, function(tosa) {
            if (!tosa._tutkinnonOsaViite) {
                virheet += validoiRyhma(tosa, viitteet) || 0;
            }
        });

        // On rakennemoduuli
        if (rakenne.muodostumisSaanto && rakenne.rooli !== "määrittelemätön") {
            var ms = rakenne.muodostumisSaanto;
            var msl = ms.laajuus || 0;
            var msk = ms.koko || 0;

            if (msl && msk) {
                var minimi = avaintenSumma(rakenne.osat, msk.minimi, function(lajitellut) {
                    return _.keys(lajitellut);
                });
                var maksimi = avaintenSumma(rakenne.osat, msk.maksimi, function(lajitellut) {
                    return _.keys(lajitellut).reverse();
                });
                if (minimi < msl.minimi) {
                    asetaVirhe("rakenne-validointi-maara-laajuus-minimi", ms);
                } else if (maksimi < msl.maksimi) {
                    asetaVirhe("rakenne-validointi-maara-laajuus-maksimi", ms);
                }
            } else if (msl) {
                // Validoidaan maksimi
                if (msl.maksimi) {
                    if (osienLaajuudenSumma(rakenne.osat) < msl.maksimi) {
                        asetaVirhe("muodostumis-rakenne-validointi-laajuus", ms);
                    }
                }
            } else if (msk) {
                if (_.size(rakenne.osat) < msk.maksimi) {
                    asetaVirhe("muodostumis-rakenne-validointi-maara", ms);
                }
            }
        }

        var tosat = _(rakenne.osat)
            .filter(function(osa) {
                return osa._tutkinnonOsaViite;
            })
            .value();

        if (
            _.size(tosat) !==
            _(tosat)
                .uniq("_tutkinnonOsaViite")
                .size()
        ) {
            asetaVirhe("muodostumis-rakenne-validointi-uniikit");
        }

        return virheet;
    }
    /* <--- */

    // Laskee rekursiivisesti puun solmujen (rakennemoduulien) kokonaislaajuuden
    function laskeLaajuudet(rakenne, viitteet) {
        if (!rakenne) {
            return;
        }

        rakenne.$laajuus = rakenne.$laajuus || 0;
        if (rakenne.$laajuusMaksimi > rakenne.$laajuus) {
            rakenne.$laajuus = rakenne.$laajuusMaksimi;
        }

        rakenne.$vaadittuLaajuus = rakenne.$vaadittuLaajuus || 0;

        _.forEach(rakenne.osat, function(osa) {
            laskeLaajuudet(osa, viitteet);
        });

        // Osa
        if (rakenne._tutkinnonOsaViite) {
            rakenne.$laajuus = viitteet[rakenne._tutkinnonOsaViite].laajuus;
        } else if (rakenne.osat) {
            // Ryhmä
            if (rakenne.muodostumisSaanto) {
                var msl = rakenne.muodostumisSaanto.laajuus;
                if (msl) {
                    rakenne.$vaadittuLaajuus = msl.maksimi || msl.minimi;
                }
            }
            rakenne.$laajuus =
                rakenne.rooli === "määritelty" ? osienLaajuudenSumma(rakenne.osat) : rakenne.$vaadittuLaajuus;
        }
    }

    function ryhmaModaali(thenCb, peruste) {
        return function(suoritustapa, ryhma, vanhempi, leikelauta) {
            $uibModal
                .open({
                    template: require("views/modals/ryhmaModal.html"),
                    controller: "MuodostumisryhmaModalCtrl",
                    resolve: {
                        ryhma: _.constant(ryhma),
                        vanhempi: _.constant(vanhempi),
                        suoritustapa: _.constant(suoritustapa),
                        leikelauta: _.constant(leikelauta),
                        peruste: _.constant(peruste)
                    }
                })
                .result.then(function(res) {
                    thenCb(ryhma, vanhempi, res);
                });
        };
    }

    function rakenneosaModaali(thenCb) {
        return function(rakenneosa) {
            $uibModal
                .open({
                    template: require("views/modals/rakenneosaModal.html"),
                    controller: "RakenneosaModalCtrl",
                    resolve: {
                        rakenneosa: function() {
                            return rakenneosa;
                        }
                    }
                })
                .result.then(function(res) {
                    thenCb(res);
                });
        };
    }

    return {
        validoiRyhma: validoiRyhma,
        laskeLaajuudet: laskeLaajuudet,
        ryhmaModaali: ryhmaModaali,
        rakenneosaModaali: rakenneosaModaali,
        kaannaSaanto: kaannaSaanto,
        skratchpadNotEmpty: function(value) {
            if (arguments.length > 0) {
                skratchpadHasContent = value;
            }
            return $q.when(skratchpadHasContent);
        }
    };
});

/* jshint +W074 */

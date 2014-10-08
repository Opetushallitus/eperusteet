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

'use strict';
/* global _ */

/* jshint -W074 */

angular.module('eperusteApp')
  .service('Muodostumissaannot', function($modal) {
    var skratchpadHasContent = false;
    function osienLaajuudenSumma(rakenneOsat) {
      return _(rakenneOsat || [])
        .map(function(osa) {
          return osa ? (osa.$vaadittuLaajuus && osa.$laajuus > osa.$vaadittuLaajuus ? osa.$vaadittuLaajuus : osa.$laajuus) : 0;
        })
        .reduce(function(sum, newval) { return sum + newval; }, 0) || 0;
    }

    function kaannaSaanto(ms) {
      if (!ms) { return; }
      var fraasi = [];
      var msl = ms.laajuus;
      var msk = ms.koko;

      if (msl && msl.minimi && msl.maksimi) {
        fraasi.push('osia-valittava-vahintaan');
        fraasi.push(msl.minimi);
        if (msl.minimi !== msl.maksimi) {
          fraasi.push('ja-enintaan');
          fraasi.push(msl.maksimi);
        }
        fraasi.push('$laajuusYksikko');
        fraasi.push('edesta');
      }

      if (msk && msk.minimi && msk.maksimi) {
        if (!_.isEmpty(fraasi)) {
          fraasi.push('ja-myos-valittava');
        }
        else {
          fraasi.push('osia-valittava-vahintaan');
        }
        fraasi.push(msk.minimi);
        if (msk.minimi !== msk.maksimi) {
          fraasi.push('ja-enintaan');
          fraasi.push(msk.maksimi);
        }
        fraasi.push('kappaletta');
      }

      return _.isEmpty(fraasi) ? ['muodostumissaantoa-ei-maaritelty'] : fraasi;
    }

    /* TODO (jshint complexity/W074) simplify/split ---> */
    function validoiRyhma(rakenne, viitteet) {
      var virheet = 0;

      function lajittele(osat) {
        var buckets = {};
        _.forEach(osat, function(osa) {
          if (!buckets[osa.$laajuus]) { buckets[osa.$laajuus] = 0; }
          buckets[osa.$laajuus] += 1;
        });
        return buckets;
      }

      function asetaVirhe(virhe, ms) {
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
          while (lajitellut[k]-- > 0 && i-- > 0) { res += parseInt(k, 10) || 0; }
        });
        return res;
      }

      if (!rakenne || !rakenne.osat) { return 0; }

      delete rakenne.$virhe;

      _.forEach(rakenne.osat, function(tosa) {
        if (!tosa._tutkinnonOsaViite) {
          virheet += validoiRyhma(tosa, viitteet) || 0;
        }
      });

      // On rakennemoduuli
      if (rakenne.muodostumisSaanto && rakenne.rooli !== 'määrittelemätön') {
        var ms = rakenne.muodostumisSaanto;
        var msl = ms.laajuus || 0;
        var msk = ms.koko || 0;
        kaannaSaanto(rakenne.muodostumisSaanto);

        if (msl && msk) {
          var minimi = avaintenSumma(rakenne.osat, msk.minimi, function(lajitellut) { return _.keys(lajitellut); });
          var maksimi = avaintenSumma(rakenne.osat, msk.maksimi, function(lajitellut) { return _.keys(lajitellut).reverse(); });
          if (minimi < msl.minimi) {
            asetaVirhe('rakenne-validointi-maara-laajuus-minimi', ms);
          }
          else if (maksimi < msl.maksimi) {
            asetaVirhe('rakenne-validointi-maara-laajuus-maksimi', ms);
          }
        } else if (msl) {
          // Validoidaan maksimi
          if (msl.maksimi) {
            if (osienLaajuudenSumma(rakenne.osat, viitteet) < msl.maksimi) {
              asetaVirhe('muodostumis-rakenne-validointi-laajuus', ms);
            }
          }
        } else if (msk) {
          if (_.size(rakenne.osat) < msk.maksimi) {
            asetaVirhe('muodostumis-rakenne-validointi-maara', ms);
          }
        }
      }

      var tosat = _(rakenne.osat)
        .filter(function(osa) { return osa._tutkinnonOsaViite; })
        .value();

      if (_.size(tosat) !== _(tosat).uniq('_tutkinnonOsaViite').size()) {
        asetaVirhe('muodostumis-rakenne-validointi-uniikit');
      }

      return virheet;
    }
    /* <--- */

    // Laskee rekursiivisesti puun solmujen (rakennemoduulien) kokonaislaajuuden
    function laskeLaajuudet(rakenne, viitteet) {
      if (!rakenne) { return; }

      _.forEach(rakenne.osat, function(osa) {
        laskeLaajuudet(osa, viitteet);
      });

      rakenne.$laajuus = 0;

      // Osa
      if (rakenne._tutkinnonOsaViite) {
        rakenne.$laajuus = viitteet[rakenne._tutkinnonOsaViite].laajuus;
      }
      // Ryhmä
      else if (rakenne.osat) {
        if (rakenne.muodostumisSaanto) {
          var msl = rakenne.muodostumisSaanto.laajuus;
          if (msl) {
            rakenne.$vaadittuLaajuus = msl.maksimi || msl.minimi;
          }
        }
        rakenne.$laajuus = rakenne.rooli === 'määritelty' ? osienLaajuudenSumma(rakenne.osat, viitteet) : rakenne.$vaadittuLaajuus;
      }
    }

    function ryhmaModaali(thenCb) {
      return function(suoritustapa, ryhma, vanhempi, leikelauta) {
        $modal.open({
          templateUrl: 'views/modals/ryhmaModal.html',
          controller: 'MuodostumisryhmaModalCtrl',
          resolve: {
            ryhma: function() { return ryhma; },
            vanhempi: function() { return vanhempi; },
            suoritustapa: function() { return suoritustapa; },
            leikelauta: function() { return leikelauta; }
          }
        })
        .result.then(function(res) {
          thenCb(ryhma, vanhempi, res);
        });
      };
    }

    function rakenneosaModaali(thenCb) {
      return function(rakenneosa) {
        $modal.open({
          templateUrl: 'views/modals/rakenneosaModal.html',
          controller: 'RakenneosaModalCtrl',
          resolve: {
            rakenneosa: function() { return rakenneosa; }
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
      skratchpadNotEmpty: function (value) {
        if (arguments.length > 0) {
          skratchpadHasContent = value;
        } else {
          return skratchpadHasContent;
        }
      }
    };
  });

/* jshint +W074 */

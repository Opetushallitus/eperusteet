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

angular.module('eperusteApp')
  .service('Muodostumissaannot', function($modal) {
    function osienLaajuudenSumma(osat) {
        return _(osat)
          .map(function(osa) { return osa.$vaadittuLaajuus ? osa.$vaadittuLaajuus : osa.$laajuus; })
          .reduce(function(sum, newval) { return sum + newval; }) || 0;
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
          fraasi.push('ja-enintään');
          fraasi.push(msl.maksimi);
        }
        fraasi.push('$laajuusYksikko');
        fraasi.push('edestä');
      }

      if (msk && msk.minimi && msk.maksimi) {
        if (!_.isEmpty(fraasi)) {
          fraasi.push('ja-myös-valittava');
        }
        else {
          fraasi.push('osia-valittava-vahintaan');
        }
        fraasi.push(msk.minimi);
        if (msk.minimi === msk.maksimi) {
          fraasi.push('ja-enintään');
          fraasi.push(msk.maksimi);
        }
        fraasi.push('kappaletta');
      }

      return fraasi;
    }

    function validoiRyhma(rakenne) {
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

      if (!rakenne || !rakenne.osat) { return; }

      delete rakenne.$virhe;

      _.forEach(rakenne.osat, function(tosa) {
        if (!tosa._tutkinnonOsaViite) {
          validoiRyhma(tosa);
        }
      });

      // On rakennemoduuli
      if (rakenne.muodostumisSaanto && rakenne.rooli !== 'virtuaalinen') {
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
            if (osienLaajuudenSumma(rakenne.osat) < msl.maksimi) {
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
    }

    // Laskee rekursiivisesti puun solmujen (rakennemoduulien) kokonaislaajuuden
    function laskeLaajuudet(rakenne, tutkinnonOsat, root) {
      root = root || true;

      if (!rakenne) { return; }

      _.forEach(rakenne.osat, function(osa) { laskeLaajuudet(osa, tutkinnonOsat, false); });
      rakenne.$laajuus = 0;

      if (rakenne._tutkinnonOsa) {
        rakenne.$laajuus = tutkinnonOsat[rakenne._tutkinnonOsa].laajuus;
      }
      else {
        if (rakenne.osat && rakenne.muodostumisSaanto) {
          var msl = rakenne.muodostumisSaanto.laajuus;
          if (msl) {
            rakenne.$vaadittuLaajuus = msl.maksimi;
          }
        }
        rakenne.$laajuus = osienLaajuudenSumma(rakenne.osat);
      }
      rakenne.$laajuus = rakenne.$laajuus || 0;
    }

    function ryhmaModaali(thenCb) {
      return function(suoritustapa, ryhma, vanhempi) {
        $modal.open({
          templateUrl: 'views/modals/ryhmaModal.html',
          controller: 'MuodostumisryhmaModalCtrl',
          resolve: {
            ryhma: function() { return ryhma; },
            vanhempi: function() { return vanhempi; },
            suoritustapa: function() { return suoritustapa; }
          }
        })
        .result.then(function(res) { thenCb(ryhma, vanhempi, res); });
      };
    }

    return {
      validoiRyhma: validoiRyhma,
      laskeLaajuudet: laskeLaajuudet,
      ryhmaModaali: ryhmaModaali,
      kaannaSaanto: kaannaSaanto
    };
  });

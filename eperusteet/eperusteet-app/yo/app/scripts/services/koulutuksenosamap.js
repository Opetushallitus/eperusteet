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

angular.module('eperusteApp')
  .service('Kielimapper', function() {
    var constKoulutuksenosat = {
      'peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat': 'peruste-validointi-koulutuksenosa-ammattitaidon-osoittamistavat',
      'peruste-validointi-tutkinnonosa-ammattitaitovaatimukset': 'peruste-validointi-koulutuksenosa-ammattitaitovaatimukset',
      'peruste-validointi-tutkinnonosa-kuvaus': 'peruste-validointi-koulutuksenosa-kuvaus',
      'peruste-validointi-tutkinnonosa-nimi': 'peruste-validointi-koulutuksenosa-nimi',
      'peruste-validointi-tutkinnonosa-tavoitteet': 'peruste-validointi-koulutuksenosa-tavoitteet',
      'tutkinnon-osia-ei-loytynyt': 'koulutuksen-osia-ei-loytynyt',
      'virhe-tutkinnonosaa-ei-löytynyt': 'virhe-koulutuksenonosaa-ei-löytynyt',
      'tutkinnon-osien-koodit-kaytossa-muissa-tutkinnon-osissa': 'koulutuksen-osien-koodit-kaytossa-muissa-koulutuksen-osissa',
      'uusi-tutkinnonosa': 'uusi-koulutuksenosa',
      'tuo-tutkinnonosa': 'tuo-koulutuksenosa',
      'poista-tutkinnonosa': 'poista-koulutuksenosa',
      'lisaa-tutkinnonosa': 'lisaa-koulutuksenosa',
      'poistetaanko-tutkinnonosa': 'poistetaanko-koulutuksenosa',
      'tutkinnon-osa-rakenteessa-ei-voi-poistaa': 'koulutuksen-osa-rakenteessa-ei-voi-poistaa',
      'tutkinnon-osa-rakenteesta-poistettu': 'koulutuksen-osa-rakenteesta-poistettu',
      'tutkinnon-osa-ei-sisaltoa': 'koulutuksen-osa-ei-sisaltoa',
      'ohje-muodostuminen-tutkinnonosat': 'ohje-muodostuminen-koulutuksenosa',
      'tutkinnonosa-kopioitu-onnistuneesti': 'koulutuksenosa-kopioitu-onnistuneesti',
      'haku-tutkinnon-nimi-placeholder': 'haku-koulutuksen-nimi-placeholder',
      'rakenne-validointi-uniikit': 'rakenne-validointi-uniikit',
      'muokkaa-tutkinnon-osaa': 'muokkaa-koulutuksen-osaa',
      'tutkinnon-osa-haku-placeholder': 'koulutuksen-osa-haku-placeholder',
      'muodostumis-rakenne-validointi-uniikit': 'muodostumis-rakenne-validointi-uniikit',
      'tutkinnon-rakenne': 'koulutuksen-rakenne',
      'tutkinnon-muodostuminen': 'koulutuksen-muodostuminen',
      'tutkinnon-muodostuminen-muokkaus': 'koulutuksen-muodostuminen-muokkaus',
      'tutkinnon-rakenteen-kuvaus': 'koulutuksen-rakenteen-kuvaus',
      'tutkinnonosa-nimi': 'koulutuksenosa-nimi',
      'tutkinnonosa-laajuus': 'koulutuksenosa-laajuus',
      'tutkinnonosa-koko': 'koulutuksenosa-koko',
      'tutkinnonosa': 'koulutuksenosa',
      'tutkinnonosat': 'koulutuksenosat',
      'koodi-virhe-2': 'koodi-virhe-2',
      'tutkinnonosa-save-fail': 'koulutuksenosa-save-fail',
      'haetaan-tutkinnon-osia': 'haetaan-koulutuksen-osia',
      'muokkaus-tutkinnon-osa': 'muokkaus-koulutuksen-osa',
      'muokkaus-rakenne': 'muokkaus-rakenne',
      'luonti-tutkinnon-osa': 'luonti-koulutuksen-osa',
      'muokkaus-tutkinnon-osan-tavoitteet-placeholder': 'muokkaus-koulutuksen-osan-tavoitteet-placeholder',
      'muokkaus-tutkinnon-osan-ammattitaitovaatimukset-placeholder': 'muokkaus-koulutuksen-osan-ammattitaitovaatimukset-placeholder',
      'tutke2-osalta-puuttuu-osa-alue-koodi': 'tutke2-osalta-puuttuu-osa-alue-koodi',
      'muokkaus-tutkinnon-osa-tallennettu': 'muokkaus-koulutuksen-osa-tallennettu',
      'tutkinnon-koodi': 'koulutuksen-koodi',
      'vierastutkinnonosa': 'vieraskoulutuksenosa',
      'nimeton-vierastutkinto': 'nimeton-vierastutkinto',
      'vierastutkinto-pitaa-olla-vierastutkintoryhmassa': 'vierastutkinto-pitaa-olla-vierastutkintoryhmassa',
      'tutkinnon-osan-asetettua-koodia-ei-koodistossa': 'koulutuksen-osan-asetettua-koodia-ei-koodistossa',
      'tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-koulutuksen-osilta': 'tutkintonimikkeen-vaatimaa-koulutuksenosakoodia-ei-loytynyt-koulutuksen-osilta',
      'et-ole-vielä-tallentanut-tutkinnonosia': 'et-ole-vielä-tallentanut-koulutuksenosia',
      'tutkinnon-tyyppi': 'koulutuksen-tyyppi',
      'perusteella-ei-viela-tutkinnonosia': 'perusteella-ei-viela-koulutuksenosia',
      'perusteen-koodi': 'perusteen-koodi',
      'rajaa-tutkinnonosia': 'rajaa-koulutuksenosia',
      'muodostumissaannot': 'muodostumissaannot',
      'liittamattomia-tutkinnon-osia': 'liittamattomia-koulutuksen-osia',
      'koodittomia-tutkinnon-osia': 'koodittomia-koulutuksen-osia',
      'tutkinnon-osan-koodi-kaytossa': 'koulutuksen-osan-koodi-kaytossa',
      'ohje-tutkinnonosa-tyyppi': 'ohje-koulutuksenosa-tyyppi',
      'luonti-projektin-nimi-ohje': 'luonti-projektin-nimi-ohje'
    };

    function mapTutkinnonosatKoulutuksenosat(isValmaTelma) {
      if (isValmaTelma) {
        return function(key) {
          return constKoulutuksenosat[key] || key;
        };
      }
      else {
        return function(key) {
          return key;
        };
      }
    }

    return {
      mapTutkinnonosatKoulutuksenosat: mapTutkinnonosatKoulutuksenosat
    };
  });

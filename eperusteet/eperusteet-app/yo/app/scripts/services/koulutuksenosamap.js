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
  .service('Kielimapper', function() {
    var constKoulutuksenosat = {
      'et-ole-vielä-tallentanut-tutkinnonosia': 'et-ole-vielä-tallentanut-koulutuksenosia',
      'haetaan-tutkinnon-osia': 'haetaan-koulutuksen-osia',
      'haku-tutkinnon-nimi-placeholder': 'haku-koulutuksen-nimi-placeholder',
      'koodi-virhe-2': 'koodi-virhe-2',
      'koodittomia-tutkinnon-osia': 'koodittomia-koulutuksen-osia',
      'liittamattomia-tutkinnon-osia': 'liittamattomia-koulutuksen-osia',
      'lisaa-tutkinnonosa': 'lisaa-koulutuksenosa',
      'luonti-projektin-nimi-ohje': 'luonti-projektin-nimi-ohje',
      'luonti-tutkinnon-osa': 'luonti-koulutuksen-osa',
      'muodostumis-rakenne-validointi-uniikit': 'muodostumis-rakenne-validointi-uniikit',
      'muodostumissaannot': 'muodostumissaannot',
      'muokkaa-tutkinnon-osaa': 'muokkaa-koulutuksen-osaa',
      'muokkaus-rakenne': 'muokkaus-rakenne',
      'muokkaus-tutkinnon-osa': 'muokkaus-koulutuksen-osa',
      'muokkaus-tutkinnon-osa-tallennettu': 'muokkaus-koulutuksen-osa-tallennettu',
      'muokkaus-tutkinnon-osan-ammattitaidon-osoittamistavat-header': 'muokkaus-koulutuksen-osan-ammattitaidon-osoittamistavat-header',
      'muokkaus-tutkinnon-osan-ammattitaidon-osoittamistavat-placeholder': 'muokkaus-koulutuksen-osan-ammattitaidon-osoittamistavat-placeholder',
      'muokkaus-tutkinnon-osan-ammattitaitovaatimukset-header': 'muokkaus-koulutuksen-osan-ammattitaitovaatimukset-header',
      'muokkaus-tutkinnon-osan-ammattitaitovaatimukset-placeholder': 'muokkaus-koulutuksen-osan-ammattitaitovaatimukset-placeholder',
      'muokkaus-tutkinnon-osan-arviointi-taulukko-header': 'muokkaus-koulutuksen-osan-arviointi-taulukko-header',
      'muokkaus-tutkinnon-osan-arviointi-teksti-header': 'muokkaus-koulutuksen-osan-arviointi-teksti-header',
      'muokkaus-tutkinnon-osan-arviointi-teksti-placeholder': 'muokkaus-koulutuksen-osan-arviointi-teksti-placeholder',
      // 'muokkaus-tutkinnon-osan-koodi-header': 'muokkaus-koulutuksen-osan-koodi-header',
      // 'muokkaus-tutkinnon-osan-nimi-header': 'muokkaus-koulutuksen-osan-nimi-header',
      'muokkaus-tutkinnon-osan-tavoitteet-header': 'muokkaus-koulutuksen-osan-tavoitteet-header',
      'muokkaus-tutkinnon-osan-tavoitteet-placeholder': 'muokkaus-koulutuksen-osan-tavoitteet-placeholder',
      'nimeton-vierastutkinto': 'nimeton-vierastutkinto',
      'ohje-muodostuminen-tutkinnonosat': 'ohje-muodostuminen-koulutuksenosa',
      'ohje-tutkinnonosa-tyyppi': 'ohje-koulutuksenosa-tyyppi',
      'peruste-validointi-tutkinnonosa-ammattitaidon-osoittamistavat': 'peruste-validointi-koulutuksenosa-ammattitaidon-osoittamistavat',
      'peruste-validointi-tutkinnonosa-ammattitaitovaatimukset': 'peruste-validointi-koulutuksenosa-ammattitaitovaatimukset',
      'peruste-validointi-tutkinnonosa-kuvaus': 'peruste-validointi-koulutuksenosa-kuvaus',
      'peruste-validointi-tutkinnonosa-nimi': 'peruste-validointi-koulutuksenosa-nimi',
      'peruste-validointi-tutkinnonosa-tavoitteet': 'peruste-validointi-koulutuksenosa-tavoitteet',
      'perusteella-ei-viela-tutkinnonosia': 'perusteella-ei-viela-koulutuksenosia',
      'perusteen-koodi': 'perusteen-koodi',
      'poista-tutkinnonosa': 'poista-koulutuksenosa',
      'poistetaanko-tutkinnonosa': 'poistetaanko-koulutuksenosa',
      'rajaa-tutkinnonosia': 'rajaa-koulutuksenosia',
      'rakenne-validointi-uniikit': 'rakenne-validointi-uniikit',
      'tuo-tutkinnonosa': 'tuo-koulutuksenosa',
      'tutke2-osalta-puuttuu-osa-alue-koodi': 'tutke2-osalta-puuttuu-osa-alue-koodi',
      'tutkinnon-koodi': 'koulutuksen-koodi',
      'tutkinnon-muodostuminen': 'koulutuksen-muodostuminen',
      'tutkinnon-muodostuminen-muokkaus': 'koulutuksen-muodostuminen-muokkaus',
      'tutkinnon-osa-ei-sisaltoa': 'koulutuksen-osa-ei-sisaltoa',
      'tutkinnon-osa-haku-placeholder': 'koulutuksen-osa-haku-placeholder',
      'tutkinnon-osa-rakenteessa-ei-voi-poistaa': 'koulutuksen-osa-rakenteessa-ei-voi-poistaa',
      'tutkinnon-osa-rakenteesta-poistettu': 'koulutuksen-osa-rakenteesta-poistettu',
      'tutkinnon-osan-asetettua-koodia-ei-koodistossa': 'koulutuksen-osan-asetettua-koodia-ei-koodistossa',
      'tutkinnon-osan-koodi-kaytossa': 'koulutuksen-osan-koodi-kaytossa',
      'tutkinnon-osia-ei-loytynyt': 'koulutuksen-osia-ei-loytynyt',
      'tutkinnon-osien-koodit-kaytossa-muissa-tutkinnon-osissa': 'koulutuksen-osien-koodit-kaytossa-muissa-koulutuksen-osissa',
      'tutkinnon-rakenne': 'koulutuksen-rakenne',
      'tutkinnon-rakenteen-kuvaus': 'koulutuksen-rakenteen-kuvaus',
      'tutkinnon-tyyppi': 'koulutuksen-tyyppi',
      'tutkinnonosa': 'koulutuksenosa',
      'tutkinnonosa-koko': 'koulutuksenosa-koko',
      'tutkinnonosa-kopioitu-onnistuneesti': 'koulutuksenosa-kopioitu-onnistuneesti',
      'tutkinnonosa-laajuus': 'koulutuksenosa-laajuus',
      'tutkinnonosa-nimi': 'koulutuksenosa-nimi',
      'tutkinnonosa-save-fail': 'koulutuksenosa-save-fail',
      'tutkinnonosat': 'koulutuksenosat',
      'tutkintonimikkeen-vaatimaa-tutkinnonosakoodia-ei-loytynyt-koulutuksen-osilta': 'tutkintonimikkeen-vaatimaa-koulutuksenosakoodia-ei-loytynyt-koulutuksen-osilta',
      'uusi-tutkinnonosa': 'uusi-koulutuksenosa',
      'vierastutkinnonosa': 'vieraskoulutuksenosa',
      'vierastutkinto-pitaa-olla-vierastutkintoryhmassa': 'vierastutkinto-pitaa-olla-vierastutkintoryhmassa',
      'virhe-tutkinnonosaa-ei-löytynyt': 'virhe-koulutuksenonosaa-ei-löytynyt',
    };

    function mapTutkinnonosatKoulutuksenosat(isValmaTelma) {
      if (isValmaTelma) {
        return function(key) {
          console.log('avain', key);
          return constKoulutuksenosat[key] || key;
        };
      }
      else {
        return _.identity;
      }
    }

    return {
      mapTutkinnonosatKoulutuksenosat: mapTutkinnonosatKoulutuksenosat
    };
  });

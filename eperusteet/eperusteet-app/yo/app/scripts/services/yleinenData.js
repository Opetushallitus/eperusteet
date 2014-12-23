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
/*global _, moment*/

angular.module('eperusteApp')
  .service('YleinenData', function YleinenData($rootScope, $translate, Arviointiasteikot, Notifikaatiot, Kaanna) {
    this.dateOptions = {
      'year-format': 'yy',
      //'month-format': 'M',
      //'day-format': 'd',
      'starting-day': 1
    };

    this.naviOmit = ['root', 'editoi', 'suoritustapa', 'sisalto', 'aloitussivu', 'selaus', 'esitys'];

    this.kontekstit = ['ammatillinenperuskoulutus',
      'ammatillinenaikuiskoulutus'];

    this.rakenneRyhmaRoolit = [
      'määritelty',
      'määrittelemätön',
      'vieras'
    ];

    this.osaamisalaRooli = 'osaamisala';

    this.yksikot = [
      'OSAAMISPISTE',
      'OPINTOVIIKKO',
    ];
    this.yksikotMap = {
      osp: 'OSAAMISPISTE',
      ov: 'OPINTOVIIKKO',
    };

    this.tilakuvaukset = [
      'poistettu',
      //'pohja',
      'laadinta',
      'kommentointi',
      'viimeistely',
      'kaannos',
      'hyvaksytty'
    ];

    this.suoritustavat = [
      'ops',
      'naytto'
    ];

    this.koulutustyypit = [
      'koulutustyyppi_1',
      'koulutustyyppi_11',
      'koulutustyyppi_12',
      'koulutustyyppi_15',
      'koulutustyyppi_16'
    ];

    this.koulutustyypitNimiMap = {
      'perustutkinto' : 'koulutustyyppi_1',
      'ammattitutkinto' : 'koulutustyyppi_11',
      'erikoisammattitutkinto' : 'koulutustyyppi_12',
      'esiopetus' : 'koulutustyyppi_15',
      'perusopetus' : 'koulutustyyppi_16',
    };

    this.koulutustyypitMap = {
      'koulutustyyppi_1': 'perustutkinto',
      'koulutustyyppi_11': 'ammattitutkinto',
      'koulutustyyppi_12': 'erikoisammattitutkinto',
      'koulutustyyppi_15': 'esiopetus',
      'koulutustyyppi_16': 'perusopetus',
    };

    this.koulutustyypinSuoritustapaOletus = {
      'koulutustyyppi_1': 'ops',
      'koulutustyyppi_11': 'naytto',
      'koulutustyyppi_12': 'naytto',
      'koulutustyyppi_15': 'esiopetus',
      'koulutustyyppi_16': 'perusopetus',

    };

    this.kielet = {
      'suomi': 'fi',
      'ruotsi': 'sv'
    };

    this.kieli = 'fi';

    this.arviointiasteikot = undefined;

    this.defaultItemsInModal = 10;

    this.dateFormatDatepicker = 'd.M.yyyy';
    this.dateFormatMomentJS = 'D.M.YYYY';

    this.isPerusopetus = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_16';
    };

    this.isEsiopetus = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_15';
    };

    this.validSuoritustapa = function (peruste, suoritustapa) {
      return peruste.koulutustyyppi === 'koulutustyyppi_12' ? 'naytto' : suoritustapa;
    };

    this.valitseSuoritustapaKoulutustyypille = function(koulutustyyppi) {
      return this.koulutustyypinSuoritustapaOletus[koulutustyyppi] || 'ops';
    };

    this.showKoulutukset = function (peruste) {
      // Näytetäänkö perusteelle koulutukset (koulutuskoodit) perusteen tiedoissa
      return peruste.koulutustyyppi !== 'koulutustyyppi_16';
    };

    this.haeArviointiasteikot = function() {
      if (this.arviointiasteikot === undefined) {
        var self = this;
        Arviointiasteikot.list({}, function(tulos) {

          self.arviointiasteikot = _.indexBy(tulos, 'id');
          $rootScope.$broadcast('arviointiasteikot');

        }, Notifikaatiot.serverCb);

      } else {
        $rootScope.$broadcast('arviointiasteikot');
      }
    };

    this.vaihdaKieli = function(kielikoodi) {
      var loytyi = false;
      for (var avain in this.kielet) {
        if (this.kielet.hasOwnProperty(avain) && this.kielet[avain] === kielikoodi) {
          loytyi = true;
          break;
        }
      }
      // Jos kielikoodi ei löydy listalta niin käytetään suomea.
      if (!loytyi) {
        kielikoodi = 'fi';
      }
      if (this.kielikoodi !== kielikoodi) {
        moment.lang(kielikoodi);
        $translate.use(kielikoodi);
        this.kieli = kielikoodi;
        $rootScope.$broadcast('notifyCKEditor');
        $rootScope.$broadcast('changed:uikieli');
      }
    };

    this.valitseKieli = function(teksti) {
      return Kaanna.kaannaSisalto(teksti);
    };

  });

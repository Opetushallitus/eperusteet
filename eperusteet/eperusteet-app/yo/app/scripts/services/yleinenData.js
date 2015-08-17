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

    this.kommenttiMaxLength = 1024;

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

    this.suoritustavat = [
      'ops',
      'naytto'
    ];

    this.koulutustyyppiInfo = {
      'koulutustyyppi_1': {
        nimi: 'perustutkinto',
        oletusSuoritustapa: 'ops',
        hasLaajuus: true,
        hasTutkintonimikkeet: true,
        hakuState: 'root.selaus.ammatillinenperuskoulutus',
        sisaltoTunniste: 'sisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_11': {
        nimi: 'ammattitutkinto',
        oletusSuoritustapa: 'naytto',
        hasTutkintonimikkeet: true,
        hakuState: 'root.selaus.ammatillinenaikuiskoulutus',
        sisaltoTunniste: 'sisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_5': {
        nimi: 'telma',
        hasLaajuus: true,
        oletusSuoritustapa: 'ops',
        hasTutkintonimikkeet: true,
        hakuState: 'root.selaus.ammatillinenaikuiskoulutus',
        sisaltoTunniste: 'sisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_18': {
        nimi: 'velma',
        hasLaajuus: true,
        oletusSuoritustapa: 'ops',
        hasTutkintonimikkeet: true,
        hakuState: 'root.selaus.ammatillinenaikuiskoulutus',
        sisaltoTunniste: 'sisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_12': {
        nimi: 'erikoisammattitutkinto',
        oletusSuoritustapa: 'naytto',
        hasTutkintonimikkeet: true,
        hakuState: 'root.selaus.ammatillinenaikuiskoulutus',
        sisaltoTunniste: 'sisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_15': {
        nimi: 'esiopetus',
        oletusSuoritustapa: 'esiopetus',
        hasTutkintonimikkeet: false,
        hakuState: 'root.selaus.esiopetuslista',
        sisaltoTunniste: 'eosisalto',
        hasPdfCreation: false
      },
      'koulutustyyppi_16': {
        nimi: 'perusopetus',
        oletusSuoritustapa: 'perusopetus',
        hasTutkintonimikkeet: false,
        hakuState: 'root.selaus.perusopetuslista',
        sisaltoTunniste: 'posisalto',
        hasPdfCreation: false
      },
      'koulutustyyppi_6': {
        nimi: 'lisaopetus',
        oletusSuoritustapa: 'lisaopetus',
        hasTutkintonimikkeet: false,
        hakuState: 'root.selaus.lisaopetuslista',
        sisaltoTunniste: 'losisalto',
        hasPdfCreation: true
      },
      'koulutustyyppi_20': {
        nimi: 'varhaiskasvatus',
        oletusSuoritustapa: 'varhaiskasvatus',
        hasTutkintonimikkeet: false,
        hakuState: 'root.selaus.varhaisopetuslista',
        sisaltoTunniste: 'vksisalto',
        hasPdfCreation: false
      }
    };

    this.koulutustyypit = _.keys(this.koulutustyyppiInfo);
    this.ammatillisetkoulutustyypit = ['koulutustyyppi_1', 'koulutustyyppi_11', 'koulutustyyppi_12'];
    this.laajuudellisetKoulutustyypit = _(this.koulutustyyppiInfo)
        .keys()
        .filter(function(key) { return this.koulutustyyppiInfo[key].hasLaajuus; }.bind(this))
        .value();

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

    this.isValmaTelma = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_18' || peruste.koulutustyyppi === 'koulutustyyppi_5';
    };

    this.isLisaopetus = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_6';
    };

    this.isVarhaiskasvatus = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_20';
    };

    this.isEsiopetus = function (peruste) {
      return peruste.koulutustyyppi === 'koulutustyyppi_15';
    };

    this.isSimple = function (peruste) {
      return this.isEsiopetus(peruste) || this.isLisaopetus(peruste) || this.isVarhaiskasvatus(peruste);
    };

    this.validSuoritustapa = function (peruste, suoritustapa) {
      // Deprecated, TODO: poista, käytä koulutustyyppiInfoa
      return peruste.koulutustyyppi === 'koulutustyyppi_12' ? 'naytto' : suoritustapa;
    };

    this.valitseSuoritustapaKoulutustyypille = function(koulutustyyppi) {
      if (this.koulutustyyppiInfo[koulutustyyppi]) {
        return this.koulutustyyppiInfo[koulutustyyppi].oletusSuoritustapa;
      }
      return 'ops';
    };

    this.showKoulutukset = function (peruste) {
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

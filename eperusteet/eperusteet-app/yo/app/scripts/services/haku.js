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
/*global _*/

angular.module('eperusteApp')
  .service('Haku', function Haku(YleinenData) {

    var self = this;

    this.getHakuparametrit = function (stateName) {
      return _.clone(self.hakuparametrit[stateName]);
    };

    this.setHakuparametrit = function (stateName, hakuparametrit) {
      self.hakuparametrit[stateName] = _.merge(hakuparametrit);
    };

    this.resetHakuparametrit = function (stateName) {
      self.hakuparametrit[stateName] = _.clone(self.hakuparametritOrg[stateName]);
      return self.hakuparametrit[stateName];
    };

    this.hakuparametritOrg = {
      'selaus.ammatillinenperuskoulutus': {
        nimi: '',
        koulutusala: '',
        tyyppi: 'koulutustyyppi_1',
        kieli: YleinenData.kieli,
        opintoala: '',
        siirtyma: false,
        sivu: 0,
        sivukoko: 20,
        suoritustapa: 'ops',
        tila: 'valmis'
      },
      'selaus.ammatillinenaikuiskoulutus': {
        nimi: '',
        koulutusala: '',
        tyyppi: '',
        kieli: YleinenData.kieli,
        opintoala: '',
        siirtyma: false,
        sivu: 0,
        sivukoko: 20,
        suoritustapa: 'naytto',
        tila: 'valmis'
      }
    };

    this.hakuparametrit = _.clone(self.hakuparametritOrg);

  });

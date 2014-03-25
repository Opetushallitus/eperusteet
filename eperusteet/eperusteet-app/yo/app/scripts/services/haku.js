'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('Haku', function Haku(YleinenData) {

    
    this.hakuParametrit = {
      nimi: '',
      koulutusala: '',
      tyyppi: '',
      kieli: YleinenData.kieli,
      opintoala: '',
      siirtyma: false,
      sivu: 0,
      sivukoko: 20,
      tutkintotyypit: _.zipObject(YleinenData.kontekstit, [])
    };
  });

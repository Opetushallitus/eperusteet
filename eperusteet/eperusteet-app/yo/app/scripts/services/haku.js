'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('Haku', function Haku(YleinenData, $state, $rootScope) {
    
    var self = this;
    
    this.getHakuparametrit = function (stateName) {
      return _.clone(self.hakuparametrit[stateName]);
    };
    
    this.setHakuparametrit = function (stateName, hakuparametrit) {
      return self.hakuparametrit[stateName] = _.merge(hakuparametrit);
    };
    
    this.resetHakuparametrit = function (stateName) {
      self.hakuparametrit[stateName] = _.clone(self.hakuparametritOrg[stateName]);
      return self.hakuparametrit[stateName];
    }

    this.hakuparametritOrg = {
      'selaus.ammatillinenperuskoulutus': {
        nimi: '',
        koulutusala: '',
        tyyppi: 'koulutustyyppi_1',
        kieli: YleinenData.kieli,
        opintoala: '',
        siirtyma: false,
        sivu: 0,
        sivukoko: 20
      }, 
      'selaus.ammatillinenaikuiskoulutus': {
        nimi: '',
        koulutusala: '',
        tyyppi: '',
        kieli: YleinenData.kieli,
        opintoala: '',
        siirtyma: false,
        sivu: 0,
        sivukoko: 20
      }
    };
    
    this.hakuparametrit = _.clone(self.hakuparametritOrg);
    
  });
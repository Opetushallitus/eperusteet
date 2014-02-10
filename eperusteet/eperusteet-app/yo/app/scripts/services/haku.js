'use strict';

angular.module('eperusteApp')
  .service('Haku', function Haku() {
    this.hakuParametrit = {
      nimi: null,
      koulutusala: '',
      tyyppi: '',
      kieli: 'fi',
      opintoala: '',
      siirtyma: 'false'
    };
  });

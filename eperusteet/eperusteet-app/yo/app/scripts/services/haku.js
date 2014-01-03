'use strict';

angular.module('eperusteApp')
  .service('Haku', function Haku() {
    this.hakuParametrit = {
      nimi: null,
      ala: '',
      tyyppi: '',
      kieli: 'fi'
    };
  });

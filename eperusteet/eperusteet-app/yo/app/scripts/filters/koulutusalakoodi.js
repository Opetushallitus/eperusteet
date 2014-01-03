'use strict';

angular.module('eperusteApp')
  .filter('koulutusalakoodiFilter', function() {
    return function(input, koulutusalakoodit) {

      return koulutusalakoodit[input];
    };
  });

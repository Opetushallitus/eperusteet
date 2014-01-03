'use strict';

angular.module('eperusteApp')
  .filter('tutkintokoodiFilter', function() {

    return function(input, tutkintotyypit) {

      return tutkintotyypit[input];
    };
  });

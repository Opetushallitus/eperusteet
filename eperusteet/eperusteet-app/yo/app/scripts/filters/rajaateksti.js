'use strict';
/* global _ */

angular.module('eperusteApp')
  .filter('rajaaKoko', function() {
    return function(input, maksimi) {
      maksimi = maksimi || 20;
      if (_.isString(input) && _.size(input) > maksimi) { return input.substr(0, maksimi) + '...'; }
      else { return input; }
    };
  });

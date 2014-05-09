'use strict';
/* global _ */

angular.module('eperusteApp')
  .filter('mapFilter', function() {
    return function(input, f) {
      input = _.filter(input, function(v) {
        var bool = f(v);
        return bool;
      });
      return input;
    };
  });

'use strict';
/* global _, moment */

angular.module('eperusteApp')
  .filter('mapFilter', function() {
    return function(input, f) {
      input = _.filter(input, function(v) {
        var bool = f(v);
        return bool;
      });
      return input;
    };
  })

  .filter('aikaleima', function ($filter) {
    return function (input) {
      return $filter('date')(input, 'd.M.yyyy H:mm') + ' (' + moment(input).fromNow() + ')';
    };
  });

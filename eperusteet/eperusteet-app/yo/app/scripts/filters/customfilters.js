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

  /**
   * Muotoilee timestampit
   * default: time (ago)
   * parametrit:
   * 'time' pelkkä päiväys ja kellonaika
   * 'ago' pelkkä ihmisluettava esim. '4 tuntia sitten'
   */
  .filter('aikaleima', function ($filter) {
    return function (input, options) {
      var date = $filter('date')(input, 'd.M.yyyy H:mm');
      var ago = moment(input).fromNow();
      if (options === 'ago') {
        return ago;
      } else if (options === 'time') {
        return date;
      }
      return date + ' (' + ago + ')';
    };
  });

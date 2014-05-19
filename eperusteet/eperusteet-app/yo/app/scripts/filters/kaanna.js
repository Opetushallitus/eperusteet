'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Kaanna', function($translate) {
    return {
      kaanna: function(input) {
        var lang = $translate.use() || $translate.preferredLanguage();
        if (_.isObject(input) && input[lang]) { return input[lang] === '' ? 'nimetön' : input[lang]; }
        else if (_.isString(input)) {
          return $translate.instant(input === '' ? 'nimetön' : input);
        }
        else { return $translate.instant('nimetön'); }
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

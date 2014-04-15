'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Kaanna', function($translate) {
    return {
      kaanna: function(input) {
        var lang = $translate.preferredLanguage();
        if (_.isObject(input) && input[lang]) {
          return input[lang];
        }
        return $translate.instant(input);
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

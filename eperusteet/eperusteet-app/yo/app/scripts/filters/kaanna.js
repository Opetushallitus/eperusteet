'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Kaanna', function($translate) {
    return {
      kaanna: function(input, nimeton) {
        nimeton = nimeton || false;

        function lisaaPlaceholder(input) {
          return _.isEmpty(input) && nimeton ? $translate.instant('nimeton') : input;
        }

        var lang = $translate.use() || $translate.preferredLanguage();
        if (_.isObject(input) && input[lang]) {
          return lisaaPlaceholder(input[lang]);
        }
        else if (_.isString(input)) {
          return lisaaPlaceholder($translate.instant(input));
        }
        else {
          return lisaaPlaceholder('');
        }
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

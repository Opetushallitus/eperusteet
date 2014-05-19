'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Kaanna', function($translate) {
    return {
      kaanna: function(input, nimetön) {
        nimetön = nimetön || false;

        function lisääPlaceholder(input) {
          return _.isEmpty(input) && nimetön ? $translate.instant('nimetön') : input;
        }

        var lang = $translate.use() || $translate.preferredLanguage();
        if (_.isObject(input) && input[lang]) {
          return lisääPlaceholder(input[lang]);
        }
        else if (_.isString(input)) {
          return lisääPlaceholder($translate.instant(input));
        }
        else {
          return lisääPlaceholder('');
        }
      }
    };
  })
  .filter('kaanna', function(Kaanna) {
    return Kaanna.kaanna;
  });

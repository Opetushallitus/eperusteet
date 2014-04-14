'use strict';
/* global _ */

angular.module('eperusteApp')
  .filter('kaanna', function($translate) {

    return function(input) {
      var lang = $translate.preferredLanguage();
      if (_.isObject(input) && input[lang]) {
        return input[lang];
      }
      return $translate.instant(input);
    };
  });

/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('LokalisointiResource', function(LOKALISOINTI_SERVICE_LOC, $resource) {
    return $resource('/lokalisointi/cxf/rest/v1/localisation?category=eperusteet', {}, {
      get: {
        method: 'GET',
        isArray: true,
        cache: true
      }
    });
  })
  .factory('LokalisointiLoader', function ($q, $http, LokalisointiResource, $window) {
    var PREFIX = 'localisation/locale-',
        SUFFIX = '.json',
        BYPASS_REMOTE = $window.location.host.indexOf('localhost') === 0;
    return function (options) {
      var deferred = $q.defer();
      var translations = {};
      $http({
        url: PREFIX + options.key + SUFFIX,
        method: 'GET',
        params: ''
      }).success(function (data) {
        _.extend(translations, data);
        if (BYPASS_REMOTE) {
          deferred.resolve(translations);
        } else {
          LokalisointiResource.get({locale: options.key}, function (res) {
            var remotes = _.zipObject(_.map(res, 'key'), _.map(res, 'value'));
            _.extend(translations, remotes);
            deferred.resolve(translations);
          }, function () {
            deferred.reject(options.key);
          });
        }
      }).error(function () {
        deferred.reject(options.key);
      });
      return deferred.promise;
    };
  });

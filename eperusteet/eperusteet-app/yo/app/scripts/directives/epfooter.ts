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

angular.module('eperusteApp')
  .service('EpFooterData', function ($http, $window, $q) {
    var data = null;
    var fetched = false;
    var pattern = /([^=]+)=([^=]+)(?:\n|$)/gi;
    this.fetch = function () {
      var deferred = $q.defer();
      if (fetched) {
        deferred.resolve(data);
      } else {
        fetched = true;
        $http.get($window.location.pathname + 'buildversion.txt').then(res => {
          var result;
          data = {};
          while ((result = pattern.exec(res.data)) !== null) {
            data[result[1]] = result[1] === 'vcsRevision' ? result[2].substr(0, 8) :
              result[2].replace(/\s\s*$/, '');
          }
          deferred.resolve(data);
        }).catch(() => {
          data = null;
          deferred.resolve(data);
        });
      }
      return deferred.promise;
    };
  })

  .directive('epFooter', function (SHOW_VERSION_FOOTER, EpFooterData) {
    return {
      restrict: 'AE',
      templateUrl: 'views/epfooter.html',
      scope: {},
      controller: function($scope) {
        $scope.active = SHOW_VERSION_FOOTER;
        if ($scope.active) {
          EpFooterData.fetch().then(function (data) {
            $scope.data = data;
          });
        }
      }
    };
  });

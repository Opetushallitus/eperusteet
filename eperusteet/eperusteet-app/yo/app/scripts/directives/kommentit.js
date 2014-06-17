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

// /* global _ */

angular.module('eperusteApp')
  .directive('kommentit', function ($timeout, $location, $state, $rootScope, YleinenData, Kommentit) {
    return {
      templateUrl: 'views/kommentit.html',
      restrict: 'E',
      replace: true,
      scope: {
        depth: '=',
        parent: '='
      },
      link: function ($scope) {
        var stateChanged = false;
        $scope.nayta = true;
        $scope.editoitava = '';
        $scope.editoi = false;
        $scope.sisalto = false;
        $scope.urlit = {};

        function lataaKommentit(url) {
          var lataaja = $scope.urlit[url];
          if (lataaja) {
            $timeout(function() {
              lataaja(function(kommentit) {
                $scope.sisalto = kommentit;
                $scope.nayta = true;
              });
            }, 100);
          }
        }

        $scope.$on('$stateChangeStart', function() {
          $scope.nayta = false;
        });

        $scope.$on('update:kommentit', function(event, url, lataaja) {
          if (!$scope.urlit[url]) {
            $scope.urlit[url] = lataaja;
            if (!stateChanged) {
              lataaKommentit($location.url());
            }
          }
        });

        $scope.$on('$stateChangeSuccess', function() {
          stateChanged = true;
          $timeout(function() {
            var url = $location.url();
            if ($scope.urlit[url]) {
              lataaKommentit($location.url());
            }
          }, 1000);
        });

        $scope.muokkaaKommenttia = function(uusikommentti) {
          Kommentit.muokkaaKommenttia(uusikommentti);
        };

        $scope.lisaaKommentti = function(parent, kommentti) {
          Kommentit.lisaaKommentti(parent, kommentti, function() {
            $scope.sisalto.yhteensa += 1;
          });
        };

        $scope.$on('enableEditing', function() { $scope.nayta = false; });
        $scope.$on('disableEditing', function() { $scope.nayta = true; });
      }
    };
  });

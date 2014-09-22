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

/* global _ */

angular.module('eperusteApp')
  .directive('kommentit', function (Kommentit, $timeout, $location, kayttajaToiminnot, Varmistusdialogi) {
    return {
      restrict: 'AE',
      templateUrl: 'views/kommentit.html',
      scope: {},
      controller: function($scope) {
        $scope.nayta = false;
        $scope.editointi = false;
        $scope.editoitava = '';
        $scope.editoi = false;
        $scope.sisalto = false;
        $scope.onLataaja = false;
        $scope.urlit = {};
        $scope.nimikirjaimet = kayttajaToiminnot.nimikirjaimet;

        function lataaKommentit(url) {
          var lataaja = $scope.urlit[url];
          if (lataaja) {
            lataaja(function(kommentit) {
              $scope.sisalto = kommentit;
              $scope.nayta = true;
            });
          }
        }

        $scope.$on('$stateChangeStart', function() {
          $scope.nayta = false;
          $scope.onLataaja = false;
        });

        function lataajaCb(url, lataaja) {
          if (!$scope.urlit[url]) {
            $scope.onLataaja = true;
            $scope.urlit[url] = lataaja;
          }
        }

        var stored = Kommentit.stored();
        if (!_.isEmpty(stored)) {
          lataajaCb(stored.url, stored.lataaja);
        }

        $scope.$on('update:kommentit', function(event, url, lataaja) {
          lataajaCb(url, lataaja);
        });

        $scope.naytaKommentit = function() { lataaKommentit($location.url()); };
        $scope.muokkaaKommenttia = function(kommentti, uusikommentti) { Kommentit.muokkaaKommenttia(kommentti, uusikommentti); };
        $scope.poistaKommentti = function(kommentti) {
          Varmistusdialogi.dialogi({
            otsikko: 'vahvista-poisto',
            teksti: 'poistetaanko-kommentti',
            primaryBtn: 'poista',
            successCb: function () {
              Kommentit.poistaKommentti(kommentti);
            }
          })();
        };
        $scope.lisaaKommentti = function(parent, kommentti) {
          Kommentit.lisaaKommentti(parent, kommentti, function() {
            $scope.sisalto.$yhteensa += 1;
          });
        };

        $scope.$on('enableEditing', function() {
          $scope.editointi = true;
        });
        $scope.$on('disableEditing', function() {
          $scope.editointi = false;
        });
        $timeout(function () {
          $scope.naytaKommentit();
        });
      }
    };
  });

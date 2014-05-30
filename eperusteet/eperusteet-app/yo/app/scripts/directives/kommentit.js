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
  .directive('kommentit', function (YleinenData, Kommentit) {
    return {
      templateUrl: '/views/kommentit.html',
      restrict: 'E',
      replace: true,
      scope: {
        sisalto: '=',
        depth: '=',
        parent: '='
      },
      link: function ($scope) {
        $scope.editoitava = '';
        $scope.editoi = false;

        $scope.muokkaaKommenttia = function(uusikommentti) {
          Kommentit.muokkaaKommenttia(uusikommentti);
          $scope.editoi = false;
        };

        $scope.lisaaKommentti = function(parent, kommentti) {
          Kommentit.lisaaKommentti(parent, kommentti);
          $scope.editoi = false;
        };

        // $scope.kommentit = Kommentit.haeKommentit();
      }
    };
  });

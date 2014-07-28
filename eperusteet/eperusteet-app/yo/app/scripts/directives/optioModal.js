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
  .directive('optioModal', function() {

    function link(scope) {

      scope.muutettavatOptiot = angular.copy(scope.optiot);

      scope.vahvistaMuutokset = function() {
        scope.optiot = angular.copy(scope.muutettavatOptiot);
      };

      scope.alusta = function() {
        scope.muutettavatOptiot = angular.copy(scope.optiot);
      };

      scope.$on('optiotMuuttuneet', function() {
        scope.alusta();
      });

      scope.vaihdaOptio = function(optiojoukko, indeksi) {
        optiojoukko[indeksi].valittu = !optiojoukko[indeksi].valittu;

        if (indeksi === 0 && optiojoukko[0].valittu) {
          _.forEach(_.rest(optiojoukko), function(optio) {
            optio.valittu = false;
          });
        } else {
          optiojoukko[0].valittu = false;
        }

        var kaikki = _.every(_.rest(optiojoukko), function(optio) {
          return optio.valittu === true;
        });
        if (kaikki) {
          scope.vaihdaOptio(optiojoukko, 0);
        }

        if (_.some(optiojoukko, 'valittu') === false) {
          optiojoukko[0].valittu = true;
        }
      };

    }

    return {
      templateUrl: 'views/partials/optioModal.html',
      restrict: 'E',
      transclude: false,
      scope: {
        modalid: '@',
        optiot: '='
      },
      link: link
    };
  });

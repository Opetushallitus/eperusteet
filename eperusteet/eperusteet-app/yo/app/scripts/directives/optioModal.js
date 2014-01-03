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
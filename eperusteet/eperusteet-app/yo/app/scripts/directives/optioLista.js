'use strict';
/*global _*/
angular.module('eperusteApp')
  .directive('optioLista', function() {

    function link(scope) {

      scope.poistaOptio = function(optiojoukko, indeksi) {
        optiojoukko[indeksi].valittu = false;
        if (_.some(optiojoukko, 'valittu') === false) {
          optiojoukko[0].valittu = true;
        }

        scope.$emit('optioPoistettu');
      };
      
    }

    return {
      templateUrl: 'views/partials/optioLista.html',
      restrict: 'E',
      transclude: false,
      scope: {
        optiot: '='
      },
      link: link
    };
  });



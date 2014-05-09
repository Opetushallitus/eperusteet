'use strict';

angular.module('eperusteApp')
  .directive('latausElementti', function(palvelinhaunIlmoitusKanava) {

    return {
      //templateURL: 'views/partials/latausElementti.html',
      restrict: 'E',
      transclude: false,
      link: function(scope, element) {
        // piilotetaan elementti aluksi
        element.hide();

        var hakuAloitettuKäsittelijä = function() {
          // Haku aloitettu sanoma saatu, näytetään lataus elementti
          element.text('Lataa...');
          element.show();
        };

        var hakuLopetettuKäsittelijä = function() {
          // Haku lopetettu sanoma saatu, piilotetaan lataus elementti
          element.hide();
        };

        palvelinhaunIlmoitusKanava.kunHakuAloitettu(scope, hakuAloitettuKäsittelijä);

        palvelinhaunIlmoitusKanava.kunHakuLopetettu(scope, hakuLopetettuKäsittelijä);
      }
    };
  });

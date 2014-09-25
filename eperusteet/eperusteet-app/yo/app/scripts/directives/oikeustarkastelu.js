'use strict';

angular.module('eperusteApp')
  .directive('oikeustarkastelu', function (PerusteprojektiOikeudetService) {
    return {
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
        var oikeudet = scope.$eval(attrs.oikeustarkastelu);
        if (!PerusteprojektiOikeudetService.onkoOikeudet(oikeudet.target, oikeudet.permission)) {
            element.hide();
        }
      }
    };
  });

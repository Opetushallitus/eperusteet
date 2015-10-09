'use strict';
/*global _*/

angular.module('eperusteApp')
  .directive('oikeustarkastelu', function (PerusteprojektiOikeudetService) {
    return {
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
        var oikeudet = scope.$eval(attrs.oikeustarkastelu);
        if ( !angular.isArray(oikeudet) ) {
          oikeudet = [oikeudet];
        }
        if (!_.any(oikeudet, function(o) { return PerusteprojektiOikeudetService.onkoOikeudet(o.target, o.permission);})) {
            element.hide();
        }
      }
    };
  });

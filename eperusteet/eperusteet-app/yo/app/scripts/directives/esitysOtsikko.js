'use strict';
angular.module('eperusteApp')
  .directive('esitysOtsikko', function() {
    return {
      template: '<div ng-transclude></div>',
      restrict: 'E',
      transclude: true,
      link: function postLink(scope, element/*, attrs*/) {
        element.addClass('h' + scope.syvyys);
      }
    };
  });


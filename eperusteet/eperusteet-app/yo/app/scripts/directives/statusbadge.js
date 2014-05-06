'use strict';

/**
 * Statusbadge:
 * <statusbadge status="luonnos|hyvaksyttavana|kommentoitavana|..."/>
 * Tyylit eri statuksille määritellään "statusbadge" sass-moduulissa.
 * Sama avainsana pitää olla käytössä tyyleissä ja lokalisoinnissa.
 */
angular.module('eperusteApp')
  .directive('statusbadge', function () {
    return {
      templateUrl: 'views/partials/statusbadge.html',
      restrict: 'E',
      scope: {
        status: '@'
      },
      controller: 'statusbadgeCtrl'
    };
  })
  .controller('statusbadgeCtrl', function ($scope) {
      $scope.editointi = true;
  });

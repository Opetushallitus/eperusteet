'use strict';

/**
 * Statusbadge:
 * <statusbadge status="luonnos|..." editable="true|false"></statusbadge>
 * Tyylit eri statuksille määritellään "statusbadge" sass-moduulissa.
 * Sama avainsana pitää olla käytössä tyyleissä ja lokalisoinnissa.
 */
angular.module('eperusteApp')
  .directive('statusbadge', function () {
    return {
      templateUrl: 'views/partials/statusbadge.html',
      restrict: 'EA',
      replace: true,
      scope: {
        status: '=',
        editable: '=?'
      },
      controller: 'StatusbadgeCtrl'
    };
  })

  .controller('StatusbadgeCtrl', function ($scope, PerusteprojektinTilanvaihto) {
    $scope.iconMapping = {
      luonnos: 'pencil',
      kommentointi: 'comment',
      viimeistely: 'certificate',
      kaannos: 'book',
      hyvaksytty: 'thumbs-up'
    };

    $scope.appliedClasses = function () {
      var classes = {editable: $scope.editable};
      classes[$scope.status] = true;
      return classes;
    };

    $scope.iconClasses = function () {
      return 'glyphicon glyphicon-' + $scope.iconMapping[$scope.status];
    };

    $scope.startEditing = function () {
      PerusteprojektinTilanvaihto.start($scope.status, function (newStatus) {
        // TODO tilan tallennus, tämä asettaa uuden tilan parent scopen projektiobjektiin.
        $scope.status = newStatus;
      });
    };
  });

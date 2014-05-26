'use strict';

/**
 * Statusbadge:
 * <statusbadge status="luonnos|..." editable="true|false"></statusbadge>
 * Tyylit eri statuksille määritellään "statusbadge" sass-moduulissa.
 * Sama avainsana pitää olla käytössä tyyleissä ja lokalisoinnissa.
 */
angular.module('eperusteApp')
  .directive('statusbadge', function () {
    var OFFSET = 4;
    return {
      templateUrl: 'views/partials/statusbadge.html',
      restrict: 'EA',
      replace: true,
      scope: {
        status: '=',
        editable: '=?'
      },
      controller: 'StatusbadgeCtrl',
      link: function (scope, element) {
        // To fit long status names into the badge, adjust letter spacing
        var el = element.find('.status-name');

        function adjust() {
          if (scope.status.length > 8) {
            var spacing = 1 - ((scope.status.length - OFFSET) * 0.2);
            el.css('letter-spacing', spacing + 'px');
          }
        }
        scope.$watch('status', adjust);
        adjust();
      }
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

'use strict';
/* global _ */

angular.module('eperusteApp')
  .directive('hallintalinkki',['Profiili', '$window', function (Profiili, $window) {
    return {
      template: '<a ng-cloak ui-sref="root.admin" icon-role="settings" kaanna="hallinta"></a>',
      restrict: 'E',
      link: function postLink(scope, element) {

        if ($window.location.host.indexOf('localhost') === 0) {
          element.show();
        } else {
          element.hide();
        }
        scope.$on('fetched:casTiedot', function() {
          if (_.contains(Profiili.groups(), 'APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001')) {
            element.show();
          }
        });

      }
    };
  }]);

'use strict';
/* global _ */

angular.module('eperusteApp')
  .directive('hallintalinkki',['Profiili', '$window', function (Profiili, $window) {
    return {
      template: '<a ng-cloak ui-sref="root.admin" icon-role="settings" kaanna="hallinta"></a>',
      restrict: 'E',
      link: function postLink(scope, element) {

        if ($window.location.host.indexOf('localhost') === 0) {
          console.log('show hallintalinkki');
          element.show();
        } else {
          console.log('hide hallintalinkki');
          element.show();
        }

        scope.$on('fetched:casTiedot', function() {
          console.log('cas tiedot haettu', Profiili.groups());
          if (_.contains(Profiili.groups(), 'APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001')) {
            console.log('ryhmä löyty');
            element.show();
          }

        });

      }
    };
  }]);

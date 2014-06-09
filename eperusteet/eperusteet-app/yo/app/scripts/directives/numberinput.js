'use strict';

angular.module('eperusteApp')
  .directive('numberinput', function () {
    return {
      templateUrl: 'views/partials/numberinput.html',
      restrict: 'E',
      scope: {
        data: '=',
        min: '@',
        max: '@',
        luokka: '@',
        form: '=',
        name: '@'
      },
      replace: true,
      link: function (scope/*, element, attrs*/) {
        // Poistetaan tmpName controlleri form:ista. Ja registeröidään controlleri
        // nimellä, mikä annettiin directiven name kentässä. Näin saadaan form validoinnit
        // näkymään directiven sisällä ja ulkona.
        if (scope.form) {
          var nameCtrl = scope.form.tmpName;
          scope.form.$removeControl(nameCtrl);
          nameCtrl.$name = scope.name;
          scope.form.$addControl(nameCtrl);
        }
      }
    };
  });

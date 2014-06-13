'use strict';

angular.module('eperusteApp')
  .directive('numberinput', function () {
    return {
      templateUrl: 'views/partials/numberinput.html',
      restrict: 'E',
      scope: {
        model: '=',
        min: '@',
        max: '@',
        luokka: '@',
        form: '=',
        name: '@',
        labelId: '@'
      },
      replace: true,
      link: function (scope, element, attrs) {
        // Poistetaan tmpName controlleri form:ista. Ja registeröidään controlleri
        // nimellä, mikä annettiin directiven name kentässä. Näin saadaan form validoinnit
        // näkymään directiven sisällä ja ulkona.
        var form = scope.form;
        if (form) {
          var nameCtrl = form.tmpName;
          form.$removeControl(nameCtrl);
          nameCtrl.$name = attrs.name;
          form.$addControl(nameCtrl);
        }
        if (scope.$parent.inputElId) {
          element.find('input').attr('id', scope.$parent.inputElId);
        }
      }
    };
  });

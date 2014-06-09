/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
'use strict';

angular.module('eperusteApp')
  .directive('pikamenu', function ($document) {
    return {
      restrict: 'EA',
      transclude: true,
      templateUrl: 'views/partials/perusteprojekti/pikamenu.html',
      link: function (scope, element) {
        // Pop the button next to the header after transclusion
        var header = angular.element('#pikamenu-header');
        var button = angular.element('#tutkinnonosat-pikamenu-button');
        button.detach().appendTo(header);

        // Clicking outside of menu closes it
        $document.on('click', function (event) {
          if (element.find(event.target).length > 0) {
            return;
          }
          scope.pikamenu.opened = false;
          scope.$apply();
        });
      },
      controller: 'TutkinnonOsatPikamenu'
    };
  })
  .controller('TutkinnonOsatPikamenu', function($scope, $state) {
    $scope.pikamenu = {
      opened: false
    };
    $scope.navigoiTutkinnonosaan = function (osa) {
      $state.go('perusteprojekti.suoritustapa.perusteenosa', {
        perusteenOsaId: osa._tutkinnonOsa,
        perusteenOsanTyyppi: 'tutkinnonosa'
      });
    };
  });


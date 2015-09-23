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

/**
 * Elementti seuraa vieritystä jos ohitetaan yläreuna.
 * Jos ikkunan koko on pienempi kuin DISABLE_WIDTH, seuraus disabloidaan.
 * Alkuperäinen sijainti tarkastetaan parent-elementistä.
 */
angular.module('eperusteApp')
  .directive('followScroll', function ($window) {
    var TOPMARGIN = 5;
    var DISABLE_WIDTH = 992;
    return {
      restrict: 'A',
      link: function (scope, element) {
        var window = angular.element($window),
             parent = element.parent();

        scope.updatePosition = function () {
          if (window.innerWidth() <= DISABLE_WIDTH) {
            return;
          }
          var parentTop = parent.offset().top;
          var windowTop = window.scrollTop();
          if (windowTop > parentTop) {
            element.css({
              position: 'relative',
              top: windowTop - parentTop + TOPMARGIN
            });
          } else {
            element.css('top', 0);
          }
        };

        var updatepos = function() {
          scope.updatePosition();
        };
        window.on('scroll resize', updatepos);
        scope.$on('$destroy', function() {
          window.off('scroll resize', updatepos);
        });
      }
    };
  });

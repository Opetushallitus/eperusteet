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
  .factory('palvelinhaunIlmoitusKanava', ['$rootScope', function($rootScope) {
      // ilmoitussanomat
      var _HAKU_ALOITETTU_ = '_HAKU_ALOITETTU_';
      var _HAKU_LOPETETTU_ = '_HAKU_LOPETETTU_';

      // Lähetä haku aloitettu sanoma
      var hakuAloitettu = function() {
        $rootScope.$broadcast(_HAKU_ALOITETTU_);
      };
      // Lähetä haku lopetettu sanoma
      var hakuLopetettu = function() {
        $rootScope.$broadcast(_HAKU_LOPETETTU_);
      };
      // Tilaa haku aloitettu sanoma
      var kunHakuAloitettu = function($scope, käsittelijä) {
        $scope.$on(_HAKU_ALOITETTU_, function(/*event*/) {
          käsittelijä();
        });
      };
      // Tilaa haku lopetettu sanoma
      var kunHakuLopetettu = function($scope, käsittelijä) {
        $scope.$on(_HAKU_LOPETETTU_, function(/*event*/) {
          käsittelijä();
        });
      };

      return {
        hakuAloitettu: hakuAloitettu,
        hakuLopetettu: hakuLopetettu,
        kunHakuAloitettu: kunHakuAloitettu,
        kunHakuLopetettu: kunHakuLopetettu
      };
    }]);


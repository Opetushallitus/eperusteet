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
  .config(function($stateProvider) {
    $stateProvider
      .state('root', {
        url: '/:lang',
        template: '<div ui-view></div>',
        abstract: true,
        resolve: {
          lokalisointi: function($stateParams, Lokalisointi) {
            return Lokalisointi.valitseKieli($stateParams.lang);
          }
        },
        onEnter: ['YleinenData', '$stateParams', function (YleinenData, $stateParams) {
          YleinenData.vaihdaKieli($stateParams.lang);
        }]
      })
      .state('root.aloitussivu', {
        url: '',
        templateUrl: 'views/aloitussivu.html'
      });
  });

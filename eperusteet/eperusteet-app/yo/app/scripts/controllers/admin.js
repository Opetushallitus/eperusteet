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
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('root.admin', {
        url: '/admin',
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl',
      });
  })
  .controller('AdminCtrl', function($scope, PerusteProjektit, Algoritmit, PerusteprojektiTila, Notifikaatiot) {
    PerusteProjektit.hae({}, function(res) {
      $scope.perusteprojektit = res;
    });

    $scope.palautaPerusteprojekti = function(pp) {
      PerusteprojektiTila.save({ id: pp.id, tila: 'laadinta' }, {}, function(vastaus) {
        if (vastaus.vaihtoOk) { pp.tila = 'laadinta'; }
        else { Notifikaatiot.varoitus('tilan-vaihto-epäonnistui'); }
      }, Notifikaatiot.serverCb);
    };

    $scope.rajaaSisaltoa = function(pp) {
      return _.isEmpty($scope.rajaus) ||
            Algoritmit.match($scope.rajaus, pp.nimi) ||
            Algoritmit.match($scope.rajaus, 'tila-' + pp.tila) ||
            Algoritmit.match($scope.rajaus, pp.diaarinumero);
    };
  });

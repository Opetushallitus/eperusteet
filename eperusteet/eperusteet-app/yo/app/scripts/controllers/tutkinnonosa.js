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
/* global _*/

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('esitys.tutkinnonosa', {
        url: '/:perusteenId/tutkinnonosa/:tutkinnonOsaId',
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl',
        naviRest: [':tutkinnonOsaId'],
        naviConfig: {
          append: true,
        }
      });
  })
  .controller('TutkinnonosaCtrl', function ($q, $scope, $rootScope, $stateParams, $state,
    YleinenData, Navigaatiopolku, PerusteenOsat, Perusteet) {
    $scope.tutkinnonOsa = {};

    $scope.nakyvilla = {
      arviointi: true,
      tavoitteet: true,
      ammattitaitovaatimukset: true,
      ammattitaidonOsoittamistavat: true,
      osaamisala: true
    };

    $scope.revisiotiedot = null;
    $scope.revisio = null;

    var perusteHakuPromise = (function() {
      if ($stateParams.perusteenId) {
        return Perusteet.get({perusteenId: $stateParams.perusteenId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    var tutkinnonOsaHakuPromise = (function() {
      if ($stateParams.tutkinnonOsaId) {
        return PerusteenOsat.get({osanId: $stateParams.tutkinnonOsaId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    $q.all([perusteHakuPromise, tutkinnonOsaHakuPromise]).then(function(vastaus) {
      $scope.peruste = vastaus[0];
      Navigaatiopolku.asetaElementit({ peruste: $scope.peruste.nimi });

      $scope.tutkinnonOsa = vastaus[1];
      Navigaatiopolku.asetaElementit({ tutkinnonOsaId: $scope.tutkinnonOsa.nimi });

      // Data haettu, päivitetään navigaatiopolku
      $rootScope.$broadcast('paivitaNavigaatiopolku');

    }, function(virhe) {
      console.log('VIRHE: ' + virhe);
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      $state.go('selaus.ammatillinenperuskoulutus');
    });

    $scope.siirryMuokkaustilaan = function() {
      $state.go('muokkaus.vanha', {
        perusteenOsanTyyppi: 'tutkinnonosa',
        perusteenId: $stateParams.tutkinnonOsaId
      });
    };

    $scope.haeRevisiot = function() {
      if($scope.revisiotiedot === null) {
        $scope.revisiotiedot = PerusteenOsat.versiot({osanId: $scope.tutkinnonOsa.id});
      }
    };

    $scope.getRevision = function(revisio) {
      PerusteenOsat.getVersio({osanId: $scope.tutkinnonOsa.id, versioId: revisio.number}).$promise.then(function(response) {
        console.log(response);
        $scope.tutkinnonOsa = response;

        if(revisio.number === _.chain($scope.revisiotiedot).sortBy('date').last().value().number) {
          $scope.revisio = null;
        } else {
          console.log('set revision id');
          $scope.revisio = revisio;
        }

      }, function(error) {
        console.log(error);
      });
    };

    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };
  });

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
    YleinenData, Navigaatiopolku, PerusteenOsat, Perusteet, palvelinhaunIlmoitusKanava) {

    $scope.tutkinnonOsa = {};

    $scope.nakyvilla = {ammattitaitovaatimukset: true};

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
      console.log('VIRHE: ' + virhe.status);
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
        console.log('fetch revisions');
        $scope.revisiotiedot = PerusteenOsat.revisions({osanId: $scope.tutkinnonOsa.id});
      }
    };

    $scope.getRevision = function(revisio) {
      PerusteenOsat.getRevision({osanId: $scope.tutkinnonOsa.id, revisionId: revisio.number}).$promise.then(function(response) {
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

    var hakuAloitettuKäsittelijä = function() {
      $scope.hakuMenossa = true;
    };

    var hakuLopetettuKäsittelijä = function() {
      $scope.hakuMenossa = false;
    };
    palvelinhaunIlmoitusKanava.kunHakuAloitettu($scope, hakuAloitettuKäsittelijä);
    palvelinhaunIlmoitusKanava.kunHakuLopetettu($scope, hakuLopetettuKäsittelijä);
  });

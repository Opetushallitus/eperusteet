'use strict';


angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/selaus/:konteksti/:perusteId/tutkinnonosa/:tutkinnonOsaId', {
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl',
        navigaationimiId: 'tutkinnonOsa'
      });
  })
  .controller('TutkinnonosaCtrl', function ($q, $scope, $routeParams, $location,
    YleinenData, PerusteenOsat, Perusteet, $rootScope) {

    $scope.kontekstit = YleinenData.kontekstit;
    $scope.tutkinnonOsa = {};
    $scope.arviointi = {};
    $scope.arviointiasteikot = {};
    $scope.kontekstit = YleinenData.kontekstit;


    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }
  
    var perusteHakuPromise = (function() {
      if ($routeParams.perusteId) {
        return Perusteet.get({perusteenId: $routeParams.perusteId}).$promise;
      } else {
        return $q.reject();
      }
    }());
    
    var tutkinnonOsaHakuPromise = (function() {
      if ($routeParams.tutkinnonOsaId) {
        return PerusteenOsat.get({osanId: $routeParams.tutkinnonOsaId}).$promise;
      } else {
        return $q.reject();
      }
    }());
    
    $q.all([perusteHakuPromise, tutkinnonOsaHakuPromise]).then(function(vastaus) {

      var peruste = vastaus[0];

      $scope.peruste = peruste;
      YleinenData.navigaatiopolkuElementit.peruste = peruste.nimi;


      var tutkinnonOsa = vastaus[1];
      $scope.tutkinnonOsa = tutkinnonOsa;
      if (tutkinnonOsa.arviointi !== undefined) {
        $scope.arviointi = tutkinnonOsa.arviointi;
        YleinenData.haeArviointiasteikot();
        YleinenData.navigaatiopolkuElementit.tutkinnonOsa = {'fi': 'testi', 'sv': '[testi]'};
      }

      // Data haettu, päivitetään navigaatiopolku
      $rootScope.$broadcast('paivitaNavigaatiopolku');

    }, function(virhe) {
      console.log('VIRHE: ' + virhe.status);
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      $location.path('/selaus/' + $scope.konteksti);
    });

    $scope.$on('arviointiasteikot', function() {
      $scope.arviointiasteikot = YleinenData.arviointiasteikot;
    });

    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };

  });

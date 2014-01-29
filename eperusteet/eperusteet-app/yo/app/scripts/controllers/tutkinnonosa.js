'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/selaus/:konteksti/:perusteId/tutkinnonosa/:tutkinnonOsaId', {
        templateUrl: 'views/tutkinnonosa.html',
        controller: 'TutkinnonosaCtrl'
      });
  })
  .controller('TutkinnonosaCtrl', function ($scope, $routeParams, $location,
    YleinenData, PerusteenOsat, Perusteet) {

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

    if ($routeParams.perusteId) {
      $scope.perusteId = $routeParams.perusteId;
      Perusteet.get({perusteenId: $routeParams.perusteId}, function(peruste) {
        $scope.peruste = peruste;

      }, function(virhe) {
        console.log(virhe.status);
      });
    }

    PerusteenOsat.get({osanId: $routeParams.tutkinnonOsaId}, function(tulos) {
        if (tulos.arviointi !== undefined) {
          $scope.arviointi = tulos.arviointi;
          $scope.tutkinnonOsa = tulos;
          YleinenData.haeArviointiasteikot();
        }
    }, function(virhe) {
      console.log(virhe.status);
      if (virhe.status === 404) {
        //virhe.data
      }
    });

    $scope.$on('arviointiasteikot', function() {
      $scope.arviointiasteikot = YleinenData.arviointiasteikot;
    });

    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };

  });
